package sample.customer;

import com.google.protobuf.Empty;
import com.scalar.db.sql.springdata.exception.ScalarDbNonTransientException;
import com.scalar.db.sql.springdata.exception.ScalarDbTransientException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
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
import sample.rpc.CommitRequest;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.GetCustomerInfoRequest;
import sample.rpc.GetCustomerInfoResponse;
import sample.rpc.PaymentRequest;
import sample.rpc.PrepareRequest;
import sample.rpc.RepaymentRequest;
import sample.rpc.RollbackRequest;
import sample.rpc.ValidateRequest;

@Service
@Retryable(
    include = TransientDataAccessException.class,
    maxAttempts = 8,
    backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase implements
    Closeable {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  @Autowired
  private CustomerRepository customerRepository;

  public void init() {
    loadInitialData();
  }

  private void loadInitialData() {
    execNormalOperation(null, "Loading initial data", () -> {
      customerRepository.insertIfNotExists(new Customer(1, "Yamada Taro", 10000, 0));
      customerRepository.insertIfNotExists(new Customer(2, "Yamada Hanako", 10000, 0));
      customerRepository.insertIfNotExists(new Customer(3, "Suzuki Ichiro", 10000, 0));
      return null;
    });
  }

  @Override
  public void getCustomerInfo(
    GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {
    execNormalOperation(responseObserver, "Getting customer info", () -> {
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

  @Override
  public void payment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), true, responseObserver, "Payment", () -> {
      // Retrieve the customer info for the customer ID
      Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
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

      return Empty.getDefaultInstance();
    });
  }

  @Override
  public void prepare(PrepareRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Payment", () -> {
      // Prepare the transaction
      customerRepository.prepare();
      return Empty.getDefaultInstance();
    });
  }

  @Override
  public void validate(ValidateRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Validate", () -> {
      // Validate the transaction
      customerRepository.validate();
      return Empty.getDefaultInstance();
    });
  }

  @Override
  public void commit(CommitRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Commit", () -> {
      // Commit the transaction
      customerRepository.commit();
      return Empty.getDefaultInstance();
    });
  }

  @Override
  public void rollback(RollbackRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Rollback", () -> {
      // Rollback the transaction
      customerRepository.rollback();
      return Empty.getDefaultInstance();
    });
  }

  private String logFailure(String funcName, Exception e) {
    String message = funcName + " failed";
    logger.error(message, e);
    return message;
  }

  private String handleError(@Nullable StreamObserver<?> responseObserver, String funcName, Exception e) {
    String message = logFailure(funcName, e);
    if (responseObserver != null) {
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
    return message;
  }

  private <T> void execNormalOperation(StreamObserver<T> responseObserver, String funcName, Supplier<T> task) {
    execOperation(responseObserver, funcName,
        // BEGIN is called before the execution of a passed task,
        // and then PREPARE, VALIDATE, COMMIT will be executed
        () -> customerRepository.execOneshotOperation(task)

        // This doesn't work. It seems each method call is executed in a transaction.
        /*
        () -> {
          customerRepository.begin();
          T result = task.get();
          customerRepository.prepare();
          customerRepository.validate();
          customerRepository.commit();
          return result;
        }
         */
    );
  }

  private <T> void execTwoPcOperation(String txId, boolean isJoin, StreamObserver<T> responseObserver, String funcName, Supplier<T> task) {
    execOperation(responseObserver, funcName, () -> customerRepository.execBatchOperations(() -> {
          if (isJoin) {
            // Join the transaction
            customerRepository.join(txId);
          } else {
            // Resume the transaction
            customerRepository.resume(txId);
          }

          // Prepare, validate and commit are supposed to be invoked later
          return task.get();
        }
    ));
  }

  private <T> void execOperation(@Nullable StreamObserver<T> responseObserver, String funcName, Supplier<T> task) {
    try {
      T result = task.get();

      if (responseObserver != null) {
        responseObserver.onNext(result);
        responseObserver.onCompleted();
      }
    } catch (ScalarDbNonTransientException e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbNonTransientException(message, e, e.getTransactionId());
    } catch (ScalarDbTransientException e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbTransientException(message, e, e.getTransactionId());
    } catch (NonTransientDataAccessException e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbNonTransientException(message, e, null);
    } catch (StatusRuntimeException e) {
      String message = logFailure(funcName, e);
      if (responseObserver != null) {
        responseObserver.onError(e);
      }
      throw new ScalarDbNonTransientException(message, e, null);
    } catch (Exception e) {
      String message = handleError(responseObserver, funcName, e);
      throw new ScalarDbTransientException(message, e, null);
    }
  }

  @Override
  public void close() {
  }
}
