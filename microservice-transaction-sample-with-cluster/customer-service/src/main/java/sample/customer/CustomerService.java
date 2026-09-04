package sample.customer;

import com.google.common.util.concurrent.Uninterruptibles;
import com.scalar.db.api.BranchTransaction;
import com.scalar.db.api.GlobalTransaction;
import com.scalar.db.api.GlobalTransactionManager;
import com.scalar.db.exception.transaction.RollbackException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import com.scalar.db.service.TransactionFactory;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.customer.model.Customer;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.GetCustomerInfoRequest;
import sample.rpc.GetCustomerInfoResponse;
import sample.rpc.PaymentRequest;
import sample.rpc.PaymentResponse;
import sample.rpc.RepaymentRequest;
import sample.rpc.RepaymentResponse;

/**
 * Customer Service. Every endpoint here can run in either of two roles, and the request decides
 * which:
 *
 * <ul>
 *   <li><b>Participant</b> — the request carries a transaction ID, so this service joins that
 *       microservice transaction with its own branch, does its work, and ends the branch. It never
 *       commits or rolls back; that is the initiator's responsibility.
 *   <li><b>Initiator</b> — the request carries no transaction ID, so this service begins its own
 *       transaction and drives it to completion, exactly as Order Service does.
 * </ul>
 *
 * <p>The same code runs unchanged against both ScalarDB Cluster deployment patterns. Only the
 * configuration file differs.
 */
public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase
    implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  private static final int MAX_ATTEMPTS = 3;
  private static final int RETRY_INTERVAL_MILLIS = 100;

  // The manager for the Microservice Transaction API. Its Java type is `GlobalTransactionManager`.
  private final GlobalTransactionManager manager;

  private interface BranchOperations<T> {
    T apply(BranchTransaction branch) throws Exception;
  }

  public CustomerService(String configFile) throws TransactionException, IOException {
    TransactionFactory factory = TransactionFactory.create(configFile);
    manager = factory.getGlobalTransactionManager();

    loadInitialData();
  }

  private void loadInitialData() throws TransactionException {
    GlobalTransaction global = manager.begin();
    BranchTransaction branch = null;
    try {
      branch = manager.beginBranch(global.getId());
      if (!Customer.get(branch, 1).isPresent()) {
        Customer.insert(branch, 1, "Yamada Taro", 10000, 0);
      }
      if (!Customer.get(branch, 2).isPresent()) {
        Customer.insert(branch, 2, "Yamada Hanako", 10000, 0);
      }
      if (!Customer.get(branch, 3).isPresent()) {
        Customer.insert(branch, 3, "Suzuki Ichiro", 10000, 0);
      }
      branch.end(BranchTransaction.Status.SUCCESS);
      branch = null;

      global.commit();
    } catch (Exception e) {
      logger.error("Loading initial data failed", e);
      endBranchWithFailure(branch);
      rollback(global);
      if (e instanceof TransactionException) {
        throw (TransactionException) e;
      }
      throw new IllegalStateException("Loading initial data failed", e);
    }
  }

  /**
   * Retrieves customer information. Runs as a participant when the request carries a transaction
   * ID, and as an initiator otherwise.
   */
  @Override
  public void getCustomerInfo(
      GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {
    String funcName = "Getting customer info";

    BranchOperations<GetCustomerInfoResponse> operations =
        branch -> {
          Customer customer = getCustomer(branch, request.getCustomerId());
          return GetCustomerInfoResponse.newBuilder()
              .setId(customer.id)
              .setName(customer.name)
              .setCreditLimit(customer.creditLimit)
              .setCreditTotal(customer.creditTotal)
              .build();
        };

    if (request.hasTransactionId()) {
      execAsParticipant(funcName, request.getTransactionId(), operations, responseObserver);
    } else {
      execAsInitiator(funcName, /* readOnly= */ true, operations, responseObserver);
    }
  }

  /**
   * Credit card payment. Always a participant: Order Service begins the transaction and passes its
   * ID here.
   */
  @Override
  public void payment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
    execAsParticipant(
        "Payment",
        request.getTransactionId(),
        branch -> {
          Customer customer = getCustomer(branch, request.getCustomerId());
          int updatedCreditTotal = customer.creditTotal + request.getAmount();

          if (updatedCreditTotal > customer.creditLimit) {
            // A deterministic business failure. FAILED_PRECONDITION tells the initiator not to
            // retry: another attempt would fail the same way.
            throw Status.FAILED_PRECONDITION
                .withDescription("Credit limit exceeded")
                .asRuntimeException();
          }

          Customer.updateCreditTotal(branch, request.getCustomerId(), updatedCreditTotal);
          return PaymentResponse.getDefaultInstance();
        },
        responseObserver);
  }

  /** Credit card repayment. Always an initiator: it involves no other service. */
  @Override
  public void repayment(
      RepaymentRequest request, StreamObserver<RepaymentResponse> responseObserver) {
    execAsInitiator(
        "Repayment",
        /* readOnly= */ false,
        branch -> {
          Customer customer = getCustomer(branch, request.getCustomerId());
          int updatedCreditTotal = customer.creditTotal - request.getAmount();

          if (updatedCreditTotal < 0) {
            throw Status.FAILED_PRECONDITION.withDescription("Over-repayment").asRuntimeException();
          }

          Customer.updateCreditTotal(branch, request.getCustomerId(), updatedCreditTotal);
          return RepaymentResponse.getDefaultInstance();
        },
        responseObserver);
  }

  /**
   * Joins a microservice transaction that another service began, does this service's work on a
   * branch of it, and ends that branch.
   *
   * <p>There is no retry loop here. Retrying is the initiator's decision, because only the
   * initiator can restart the transaction as a whole. This method also never commits or rolls back.
   */
  private <T> void execAsParticipant(
      String funcName,
      String transactionId,
      BranchOperations<T> operations,
      StreamObserver<T> responseObserver) {
    BranchTransaction branch = null;
    try {
      branch = manager.beginBranch(transactionId);
      T result = operations.apply(branch);

      branch.end(BranchTransaction.Status.SUCCESS);

      responseObserver.onNext(result);
      responseObserver.onCompleted();
    } catch (Exception e) {
      // Ending the branch is this process's obligation, and it holds whether the work succeeded or
      // failed. The initiator cannot discharge it on our behalf.
      endBranchWithFailure(branch);
      respondWithError(funcName, e, responseObserver);
    }
  }

  /** Begins a microservice transaction, does the work on a branch of it, and commits. */
  private <T> void execAsInitiator(
      String funcName,
      boolean readOnly,
      BranchOperations<T> operations,
      StreamObserver<T> responseObserver) {
    int attempt = 0;
    Exception lastException = null;

    while (true) {
      if (attempt++ > 0) {
        if (attempt > MAX_ATTEMPTS) {
          respondWithError(funcName, lastException, responseObserver);
          return;
        }
        logger.warn(
            "Retrying the transaction after {} milliseconds: {}",
            RETRY_INTERVAL_MILLIS,
            funcName,
            lastException);
        Uninterruptibles.sleepUninterruptibly(RETRY_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
      }

      GlobalTransaction global = null;
      BranchTransaction branch = null;
      try {
        global = readOnly ? manager.beginReadOnly() : manager.begin();
        branch = manager.beginBranch(global.getId());
        T result = operations.apply(branch);

        branch.end(BranchTransaction.Status.SUCCESS);
        branch = null;

        global.commit();

        responseObserver.onNext(result);
        responseObserver.onCompleted();
        return;
      } catch (UnknownTransactionStatusException e) {
        // The outcome is unknown, so retrying could duplicate a committed transaction. Determining
        // the actual status is the application's responsibility.
        endBranchWithFailure(branch);
        respondWithError(funcName, e, responseObserver);
        return;
      } catch (StatusRuntimeException e) {
        endBranchWithFailure(branch);
        rollback(global);

        if (e.getStatus().getCode() == Status.Code.NOT_FOUND
            || e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
          responseObserver.onError(e);
          return;
        }
        lastException = e;
      } catch (Exception e) {
        endBranchWithFailure(branch);
        rollback(global);
        lastException = e;
      }
    }
  }

  private Customer getCustomer(BranchTransaction branch, int customerId) throws Exception {
    Optional<Customer> customer = Customer.get(branch, customerId);
    if (!customer.isPresent()) {
      throw Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException();
    }
    return customer.get();
  }

  /**
   * Ends a branch declaring that its work failed. Safe to call from a catch block: ending a branch
   * that was already ended is a no-op, and it does not mask the original failure.
   */
  private void endBranchWithFailure(@Nullable BranchTransaction branch) {
    if (branch == null) {
      return;
    }
    try {
      branch.end(BranchTransaction.Status.FAILURE);
    } catch (Exception e) {
      logger.warn("Ending the branch failed", e);
    }
  }

  private void rollback(@Nullable GlobalTransaction global) {
    if (global == null) {
      return;
    }
    try {
      global.rollback();
    } catch (RollbackException e) {
      logger.warn("Rolling back the transaction failed", e);
    }
  }

  private <T> void respondWithError(
      String funcName, Exception exception, StreamObserver<T> responseObserver) {
    String message = funcName + " failed";
    logger.error(message, exception);
    if (exception instanceof StatusRuntimeException) {
      responseObserver.onError(exception);
    } else {
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(exception).asRuntimeException());
    }
  }

  @Override
  public void close() {
    manager.close();
  }
}
