package sample.customer;

import com.google.protobuf.Empty;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.api.TwoPhaseCommitTransactionManager;
import com.scalar.db.exception.transaction.AbortException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.service.TransactionFactory;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.customer.model.Customer;
import sample.rpc.CommitRequest;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.GetCustomerInfoRequest;
import sample.rpc.GetCustomerInfoResponse;
import sample.rpc.PaymentRequest;
import sample.rpc.PrepareRequest;
import sample.rpc.RepaymentRequest;
import sample.rpc.RollbackRequest;
import sample.rpc.ValidateRequest;

public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase
    implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  // For normal transactions
  private final DistributedTransactionManager transactionManager;

  // For two-phase commit transactions
  private final TwoPhaseCommitTransactionManager twoPhaseCommitTransactionManager;

  public CustomerService(String configFile) throws TransactionException, IOException {
    // Initialize the transaction managers
    TransactionFactory factory = TransactionFactory.create(configFile);
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
      logger.error("loading initial data failed", e);
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
      // Start a transaction
      transaction = transactionManager.start();

      // Retrieve the customer info for the specified customer ID
      Optional<Customer> result = Customer.get(transaction, request.getCustomerId());

      if (!result.isPresent()) {
        abortTransaction(transaction);
        // If the customer info the specified customer ID doesn't exist, throw an exception
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
        return;
      }

      // Commit the transaction (even when the transaction is read-only, we need to commit)
      transaction.commit();

      // Return the customer info
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
      logger.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    }
  }

  @Override
  public void repayment(RepaymentRequest request, StreamObserver<Empty> responseObserver) {
    DistributedTransaction transaction = null;
    try {
      // Start a transaction
      transaction = transactionManager.start();

      // Retrieve the customer info for the specified customer ID
      Optional<Customer> result = Customer.get(transaction, request.getCustomerId());
      if (!result.isPresent()) {
        abortTransaction(transaction);
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
        return;
      }

      int updatedCreditTotal = result.get().creditTotal - request.getAmount();

      // Check if over repayment or not
      if (updatedCreditTotal < 0) {
        abortTransaction(transaction);
        responseObserver.onError(
            Status.FAILED_PRECONDITION.withDescription("Over repayment").asRuntimeException());
        return;
      }

      // Reduce credit_total for the customer
      Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditTotal);

      // Commit the transaction (even when the transaction is read-only, we need to commit)
      transaction.commit();

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "repayment failed";
      logger.error(message, e);
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
      logger.warn("abort failed", e);
    }
  }

  @Override
  public void payment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
    TwoPhaseCommitTransaction transaction = null;
    try {
      // Join the transaction that the order service started
      transaction = twoPhaseCommitTransactionManager.join(request.getTransactionId());

      // Retrieve the customer info for the customer ID
      Optional<Customer> result = Customer.get(transaction, request.getCustomerId());
      if (!result.isPresent()) {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException());
        return;
      }

      int updatedCreditTotal = result.get().creditTotal + request.getAmount();

      // Check if the credit total exceeds the credit limit after payment
      if (updatedCreditTotal > result.get().creditLimit) {
        responseObserver.onError(
            Status.FAILED_PRECONDITION
                .withDescription("Credit limit exceeded")
                .asRuntimeException());
        return;
      }

      // Update credit_total for the customer
      Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditTotal);

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "payment failed";
      logger.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } finally {
      // Suspend the transaction
      try {
        twoPhaseCommitTransactionManager.suspend(transaction);
      } catch (TransactionException e) {
        logger.error("suspend failed", e);
      }
    }
  }

  @Override
  public void prepare(PrepareRequest request, StreamObserver<Empty> responseObserver) {
    TwoPhaseCommitTransaction transaction = null;
    try {
      // Resume the suspended transaction
      transaction = twoPhaseCommitTransactionManager.resume(request.getTransactionId());

      // Prepare the transaction
      transaction.prepare();

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "prepare failed";
      logger.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } finally {
      // Suspend the transaction
      try {
        twoPhaseCommitTransactionManager.suspend(transaction);
      } catch (TransactionException e) {
        logger.error("suspend failed", e);
      }
    }
  }

  @Override
  public void validate(ValidateRequest request, StreamObserver<Empty> responseObserver) {
    TwoPhaseCommitTransaction transaction = null;
    try {
      // Resume the suspended transaction
      transaction = twoPhaseCommitTransactionManager.resume(request.getTransactionId());

      // Validate the transaction
      transaction.validate();

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "validate failed";
      logger.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } finally {
      // Suspend the transaction
      try {
        twoPhaseCommitTransactionManager.suspend(transaction);
      } catch (TransactionException e) {
        logger.error("suspend failed", e);
      }
    }
  }

  @Override
  public void commit(CommitRequest request, StreamObserver<Empty> responseObserver) {
    TwoPhaseCommitTransaction transaction = null;
    try {
      // Resume the suspended transaction
      transaction = twoPhaseCommitTransactionManager.resume(request.getTransactionId());

      // Commit the transaction
      transaction.commit();

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "commit failed";
      logger.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());

      try {
        // Suspend the transaction
        twoPhaseCommitTransactionManager.suspend(transaction);
      } catch (TransactionException ex) {
        logger.error("suspend failed", ex);
      }
    }
  }

  @Override
  public void rollback(RollbackRequest request, StreamObserver<Empty> responseObserver) {
    try {
      // Resume the suspended transaction
      TwoPhaseCommitTransaction transaction =
          twoPhaseCommitTransactionManager.resume(request.getTransactionId());

      // Rollback the transaction
      transaction.rollback();

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "rollback failed";
      logger.error(message, e);
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
