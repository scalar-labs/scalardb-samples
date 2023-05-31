package sample.customer;

import com.google.protobuf.Empty;
import com.scalar.db.sql.springdata.EnableScalarDbRepositories;
import com.scalar.db.sql.springdata.exception.ScalarDbNonTransientException;
import com.scalar.db.sql.springdata.exception.ScalarDbTransientException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.customer.domain.model.Customer;
import sample.customer.domain.repository.CustomerRepository;
import sample.customer.domain.repository.CustomerTwoPcRepository;
import sample.rpc.CommitRequest;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.GetCustomerInfoRequest;
import sample.rpc.GetCustomerInfoResponse;
import sample.rpc.PaymentRequest;
import sample.rpc.PrepareRequest;
import sample.rpc.RepaymentRequest;
import sample.rpc.RollbackRequest;
import sample.rpc.ValidateRequest;

@EnableScalarDbRepositories
@Service
@Retryable(
    include = TransientDataAccessException.class,
    maxAttempts = 8,
    backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  @Autowired
  private CustomerRepository customerRepository;
  @Autowired
  private CustomerTwoPcRepository customerTwoPcRepository;

  @Transactional
  public void init() {
    customerRepository.insertIfNotExists(new Customer(1, "Yamada Taro", 10000, 0));
    customerRepository.insertIfNotExists(new Customer(2, "Yamada Hanako", 10000, 0));
    customerRepository.insertIfNotExists(new Customer(3, "Suzuki Ichiro", 10000, 0));
  }

  private String handleError(StreamObserver<?> responseObserver, String funcName, Exception e) {
      String message = funcName + " failed";
      logger.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
      return message;
  }

  private <T> void execOperation(StreamObserver<T> responseObserver, String funcName, Supplier<T> task) {
    try {
      T result = task.get();

      responseObserver.onNext(result);
      responseObserver.onCompleted();
    } catch (ScalarDbNonTransientException e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbNonTransientException(message, e, e.getTransactionId());
    } catch (ScalarDbTransientException e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbTransientException(message, e, e.getTransactionId());
    } catch (NonTransientDataAccessException e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbNonTransientException(message, e, null);
    } catch (Exception e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbTransientException(message, e, null);
    }
  }

  @Transactional
  public void getCustomerInfo(
    GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {

    execOperation(responseObserver, "Getting customer info", () -> {
      // Retrieve the customer info for the specified customer ID from the customers table.
      Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
      if (!customerOpt.isPresent()) {
        String message = "Customer not found: " + request.getCustomerId();
        responseObserver.onError(
            Status.NOT_FOUND.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }

      Customer customer = customerOpt.get();

      return GetCustomerInfoResponse.newBuilder()
              .setId(customer.customerId)
              .setName(customer.name)
              .setCreditLimit(customer.creditLimit)
              .setCreditTotal(customer.creditTotal)
              .build();
    });
  }

  @Override
  public void repayment(RepaymentRequest request, StreamObserver<Empty> responseObserver) {
    execOperation(responseObserver, "Repayment", () -> {
      // Retrieve the customer info for the specified customer ID
      Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
      if (!customerOpt.isPresent()) {
        String message = "Customer not found: " + request.getCustomerId();
        responseObserver.onError(
            Status.NOT_FOUND.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }

      Customer customer = customerOpt.get();

      int updatedCreditTotal = customer.creditTotal - request.getAmount();
      // Check if over repayment or not
      if (updatedCreditTotal < 0) {
        String message = String.format(
            "Over repayment. creditTotal:%d, payment:%d", customer.creditTotal, request.getAmount());
        responseObserver.onError(
            Status.FAILED_PRECONDITION.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }

      // Reduce credit_total for the customer
      customerRepository.update(customer.withCreditTotal(updatedCreditTotal));

      return Empty.getDefaultInstance();
    });
  }

  private void execTwoPcOperation(String txId, boolean isJoin, StreamObserver<Empty> responseObserver, String funcName, Runnable task) {
    execOperation(responseObserver, funcName, () -> {
      if (isJoin) {
        // Join the transaction
        customerTwoPcRepository.join(txId);
      }
      else {
        // Resume the transaction
        customerTwoPcRepository.resume(txId);
      }

      task.run();

      return Empty.getDefaultInstance();
    });
  }

  @Override
  public void payment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), true, responseObserver, "Payment", () -> {
      // Retrieve the customer info for the customer ID
      Optional<Customer> customerOpt = customerTwoPcRepository.findById(request.getCustomerId());
      if (!customerOpt.isPresent()) {
        String message = "Customer not found: " + request.getCustomerId();
        responseObserver.onError(
            Status.NOT_FOUND.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }

      Customer customer = customerOpt.get();

      int updatedCreditTotal = customer.creditTotal - request.getAmount();

      // Check if the credit total exceeds the credit limit after payment
      if (updatedCreditTotal > customer.creditLimit) {
        String message = String.format(
            "Over repayment. creditTotal:%d, payment:%d", customer.creditTotal, request.getAmount());
        responseObserver.onError(
            Status.FAILED_PRECONDITION.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }
      // Reduce credit_total for the customer
      customerRepository.update(customer.withCreditTotal(updatedCreditTotal));
    });
  }

  @Override
  public void prepare(PrepareRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Payment", () -> {
      // Prepare the transaction
      customerTwoPcRepository.prepare();
    });
  }

  @Override
  public void validate(ValidateRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Validate", () -> {
      // Validate the transaction
      customerTwoPcRepository.validate();
    });
  }

  @Override
  public void commit(CommitRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Commit", () -> {
      // Commit the transaction
      customerTwoPcRepository.commit();
    });
  }

  @Override
  public void rollback(RollbackRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Rollback", () -> {
      // Rollback the transaction
      customerTwoPcRepository.rollback();
    });
  }
}