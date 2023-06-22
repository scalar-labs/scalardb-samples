package sample.customer;

import com.google.protobuf.Empty;
import com.scalar.db.sql.springdata.exception.ScalarDbNonTransientException;
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
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
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
    // `customerRepository` is set up after the constructor of CustomerService is performed by
    // Spring Framework. So, loading initial data should be executed outside the constructor.
    loadInitialData();
  }

  private void loadInitialData() {
    customerRepository.execOneshotOperation(() -> {
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
      Customer customer = getCustomer(responseObserver, request.getCustomerId());

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
    execNormalOperation(responseObserver, "Repayment", () -> {
      Customer customer = getCustomer(responseObserver, request.getCustomerId());

      int updatedCreditTotal = customer.creditTotal - request.getAmount();
      // Check if over repayment or not
      if (updatedCreditTotal < 0) {
        String message = String.format(
            "Over repayment. creditTotal:%d, payment:%d", customer.creditTotal,
            request.getAmount());
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
      Customer customer = getCustomer(responseObserver, request.getCustomerId());

      int updatedCreditTotal = customer.creditTotal + request.getAmount();
      // Check if the credit total exceeds the credit limit after payment
      if (updatedCreditTotal > customer.creditLimit) {
        String message = String.format(
            "Credit limit exceeded. creditTotal:%d, payment:%d", customer.creditTotal,
            request.getAmount());
        responseObserver.onError(
            Status.FAILED_PRECONDITION.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }

      // Increase credit_total for the customer
      customerRepository.update(customer.withCreditTotal(updatedCreditTotal));

      return Empty.getDefaultInstance();
    });
  }

  @Override
  public void prepare(PrepareRequest request, StreamObserver<Empty> responseObserver) {
    execTwoPcOperation(request.getTransactionId(), false, responseObserver, "Prepare", () -> {
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

  private Customer getCustomer(StreamObserver<?> responseObserver, int customerId) {
    // Retrieve the customer info for the specified customer ID from the customers table.
    Optional<Customer> customerOpt = customerRepository.findById(customerId);
    if (!customerOpt.isPresent()) {
      String message = "Customer not found: " + customerId;
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(message).asRuntimeException());
      throw new ScalarDbNonTransientException(message);
    }
    return customerOpt.get();
  }

  private <T> void execNormalOperation(StreamObserver<T> responseObserver, String funcName,
      Supplier<T> task) {
    execAndReturnResponse(responseObserver, funcName,
        // BEGIN is called before the execution of this passed CRUD operations `task`,
        // and then PREPARE, VALIDATE and COMMIT will be executed after the CRUD operations
        () -> customerRepository.execOneshotOperation(task)
    );
  }

  private <T> void execTwoPcOperation(String txId, boolean isJoin,
      StreamObserver<T> responseObserver, String funcName, Supplier<T> task) {
    execAndReturnResponse(responseObserver, funcName,
        () -> customerRepository.execBatchOperations(() -> {
              if (isJoin) {
                // Join the transaction
                customerRepository.join(txId);
              } else {
                // Resume the transaction
                customerRepository.resume(txId);
              }

              // Prepare, validate and commit are supposed to be invoked later
              T result = task.get();

              customerRepository.suspend();

              return result;
            }
        ));
  }

  @Nullable
  private StatusRuntimeException extractStatusRuntimeException(Throwable e) {
    Throwable current = e;
    while (current != null) {
      if (current instanceof StatusRuntimeException) {
        return (StatusRuntimeException) current;
      }
      current = current.getCause();
    }
    return null;
  }

  private <T> void execAndReturnResponse(StreamObserver<T> responseObserver, String funcName,
      Supplier<T> task) {
    try {
      T result = task.get();

      responseObserver.onNext(result);
      responseObserver.onCompleted();
    } catch (Exception e) {
      StatusRuntimeException sre = extractStatusRuntimeException(e);
      if (sre != null) {
        responseObserver.onError(e);
      } else {
        String message = funcName + " failed";
        logger.error(message, e);
        responseObserver.onError(
            Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
      }
    }
  }

  @Override
  public void close() {
  }
}
