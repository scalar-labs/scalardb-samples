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
    customerRepository.executeOneshotOperations(() -> {
      customerRepository.insertIfNotExists(new Customer(1, "Yamada Taro", 10000, 0));
      customerRepository.insertIfNotExists(new Customer(2, "Yamada Hanako", 10000, 0));
      customerRepository.insertIfNotExists(new Customer(3, "Suzuki Ichiro", 10000, 0));
      return null;
    });
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Override
  public void getCustomerInfo(
      GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {
    String funcName = "Getting customer info";
    // This function processing operations can be used in both normal transaction and two-phase
    // interface transaction.
    Supplier<GetCustomerInfoResponse> operations = () -> {
      Customer customer = getCustomer(responseObserver, request.getCustomerId());

      return GetCustomerInfoResponse.newBuilder()
          .setId(customer.customerId)
          .setName(customer.name)
          .setCreditLimit(customer.creditLimit)
          .setCreditTotal(customer.creditTotal)
          .build();
    };

    if (request.hasTransactionId()) {
      execAndReturnResponse(funcName,
          () -> customerRepository.joinTransactionOnParticipant(request.getTransactionId(), operations),
          responseObserver);
    } else {
      execAndReturnResponse(funcName,
          () -> customerRepository.executeOneshotOperations(operations),
          responseObserver);
    }
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Override
  public void repayment(RepaymentRequest request, StreamObserver<Empty> responseObserver) {
    execAndReturnResponse("Repayment", () ->
        customerRepository.executeOneshotOperations(() -> {
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
            }
        ), responseObserver);
  }

  // @Retryable shouldn't be used here as this is used as a participant API and
  // will be retried by the coordinator service if needed
  @Override
  public void payment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
    execAndReturnResponse("Payment", () ->
        customerRepository.joinTransactionOnParticipant(request.getTransactionId(), () -> {
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
        }), responseObserver);
  }

  // @Retryable shouldn't be put as this is used as a participant API and
  // will be retried by the coordinator service if needed
  @Override
  public void prepare(PrepareRequest request, StreamObserver<Empty> responseObserver) {
    execAndReturnResponse("Prepare", () -> {
      customerRepository.prepareTransactionOnParticipant(request.getTransactionId());
      return Empty.getDefaultInstance();
    }, responseObserver);
  }

  // @Retryable shouldn't be put as this is used as a participant API and
  // will be retried by the coordinator service if needed
  @Override
  public void validate(ValidateRequest request, StreamObserver<Empty> responseObserver) {
    execAndReturnResponse("Validate", () -> {
      customerRepository.validateTransactionOnParticipant(request.getTransactionId());
      return Empty.getDefaultInstance();
    }, responseObserver);
  }

  // @Retryable shouldn't be put as this is used as a participant API and
  // will be retried by the coordinator service if needed
  @Override
  public void commit(CommitRequest request, StreamObserver<Empty> responseObserver) {
    execAndReturnResponse("Commit", () -> {
      customerRepository.commitTransactionOnParticipant(request.getTransactionId());
      return Empty.getDefaultInstance();
    }, responseObserver);
  }

  // @Retryable shouldn't be put as this is used as a participant API and
  // will be retried by the coordinator service if needed
  @Override
  public void rollback(RollbackRequest request, StreamObserver<Empty> responseObserver) {
    execAndReturnResponse("Rollback", () -> {
      customerRepository.rollbackTransactionOnParticipant(request.getTransactionId());
      return Empty.getDefaultInstance();
    }, responseObserver);
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

  private <T> void execAndReturnResponse(String funcName, Supplier<T> operations,
      StreamObserver<T> responseObserver) {
    try {
      T result = operations.get();

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
