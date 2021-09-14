package example.customer;

import com.google.protobuf.Empty;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.api.TwoPhaseCommitTransactionManager;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.exception.transaction.AbortException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.service.TransactionFactory;
import example.customer.model.Customer;
import example.rpc.CommitRequest;
import example.rpc.CustomerServiceGrpc;
import example.rpc.GetCustomerInfoRequest;
import example.rpc.GetCustomerInfoResponse;
import example.rpc.JoinRequest;
import example.rpc.PaymentRequest;
import example.rpc.PrepareRequest;
import example.rpc.RepaymentRequest;
import example.rpc.RollbackRequest;
import example.rpc.ValidateRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase
    implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

  // For normal transactions
  private final DistributedTransactionManager transactionManager;

  // For two-phase commit transactions
  private final TwoPhaseCommitTransactionManager twoPhaseCommitTransactionManager;

  public CustomerService(DatabaseConfig config) throws TransactionException {
    // Initialize the transaction managers
    TransactionFactory factory = new TransactionFactory(config);
    transactionManager = factory.getTransactionManager();
    twoPhaseCommitTransactionManager = factory.getTwoPhaseCommitTransactionManager();

    loadInitialData();
  }

  private void loadInitialData() throws TransactionException {
    DistributedTransaction transaction = null;
    try {
      transaction = transactionManager.start();
      loadCustomerIfNotExists(transaction, 1, "Yamada Taro", 10000, 0);
      loadCustomerIfNotExists(transaction, 2, "Yamada Hanako", 10000, 0);
      loadCustomerIfNotExists(transaction, 3, "Suzuki Ichiro", 10000, 0);
      transaction.commit();
    } catch (TransactionException e) {
      LOGGER.error("loading initial data failed", e);
      abortTransaction(transaction);
      throw e;
    }
  }

  private void loadCustomerIfNotExists(
      DistributedTransaction transaction, int id, String name, int creditLimit, int creditTotal)
      throws CrudException {
    Optional<Customer> customer = Customer.get(transaction, id);
    if (!customer.isPresent()) {
      Customer.put(transaction, id, name, creditLimit, creditTotal);
    }
  }

  @Override
  public void getCustomerInfo(
      GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {
    DistributedTransaction transaction = null;
    try {
      transaction = transactionManager.start();
      Optional<Customer> result = Customer.get(transaction, request.getCustomerId());
      transaction.commit();

      if (!result.isPresent()) {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
        return;
      }

      responseObserver.onNext(
          GetCustomerInfoResponse.newBuilder()
              .setId(result.get().id)
              .setName(result.get().name)
              .setCreditLimit(result.get().creditLimit)
              .setCreditTotal(result.get().creditTotal)
              .build());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "getting customer info failed";
      LOGGER.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void repayment(RepaymentRequest request, StreamObserver<Empty> responseObserver) {
    DistributedTransaction transaction = null;
    try {
      transaction = transactionManager.start();

      Optional<Customer> result = Customer.get(transaction, request.getCustomerId());

      if (!result.isPresent()) {
        abortTransaction(transaction);
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
        return;
      }

      int updatedCreditLimit = result.get().creditTotal - request.getAmount();

      // Check if over repayment or not
      if (updatedCreditLimit < 0) {
        abortTransaction(transaction);
        responseObserver.onError(
            Status.FAILED_PRECONDITION.withDescription("Over repayment").asRuntimeException());
        return;
      }

      Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditLimit);

      transaction.commit();

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "repayment failed";
      LOGGER.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  private void abortTransaction(@Nullable DistributedTransaction transaction) {
    if (transaction == null) {
      return;
    }
    try {
      transaction.abort();
    } catch (AbortException e) {
      LOGGER.warn("abort failed", e);
    }
  }

  @Override
  public void join(JoinRequest request, StreamObserver<Empty> responseObserver) {
    try {
      twoPhaseCommitTransactionManager.join(request.getTransactionId());
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "join failed";
      LOGGER.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void payment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
    try {
      TwoPhaseCommitTransaction transaction =
          twoPhaseCommitTransactionManager.resume(request.getTransactionId());

      Optional<Customer> result = Customer.get(transaction, request.getCustomerId());

      if (!result.isPresent()) {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
        return;
      }

      int updatedCreditTotal = result.get().creditTotal + request.getAmount();

      // Check if the updated credit total exceeds the credit limit
      if (updatedCreditTotal > result.get().creditLimit) {
        responseObserver.onError(
            Status.FAILED_PRECONDITION
                .withDescription("Credit limit exceeded")
                .asRuntimeException());
        return;
      }

      Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditTotal);

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "payment failed";
      LOGGER.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void prepare(PrepareRequest request, StreamObserver<Empty> responseObserver) {
    try {
      TwoPhaseCommitTransaction transaction =
          twoPhaseCommitTransactionManager.resume(request.getTransactionId());
      transaction.prepare();
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "prepare failed";
      LOGGER.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void validate(ValidateRequest request, StreamObserver<Empty> responseObserver) {
    try {
      TwoPhaseCommitTransaction transaction =
          twoPhaseCommitTransactionManager.resume(request.getTransactionId());
      transaction.validate();
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "validate failed";
      LOGGER.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void commit(CommitRequest request, StreamObserver<Empty> responseObserver) {
    try {
      TwoPhaseCommitTransaction transaction =
          twoPhaseCommitTransactionManager.resume(request.getTransactionId());
      transaction.commit();
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "commit failed";
      LOGGER.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void rollback(RollbackRequest request, StreamObserver<Empty> responseObserver) {
    try {
      TwoPhaseCommitTransaction transaction =
          twoPhaseCommitTransactionManager.resume(request.getTransactionId());
      transaction.rollback();
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "rollback failed";
      LOGGER.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void close() {
    transactionManager.close();
    twoPhaseCommitTransactionManager.close();
  }
}
