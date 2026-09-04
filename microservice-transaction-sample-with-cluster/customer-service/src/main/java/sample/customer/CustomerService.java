package sample.customer;

import com.google.common.util.concurrent.Uninterruptibles;
import com.scalar.db.api.BranchTransaction;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.GlobalTransactionManager;
import com.scalar.db.api.TransactionCrudOperable;
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
 * Customer Service. Every endpoint here does its work in one of two ways, and the request decides
 * which:
 *
 * <ul>
 *   <li><b>As part of a microservice transaction</b> — the request carries a transaction ID, so
 *       another service began a transaction that spans services. This service joins it with its own
 *       branch through the Microservice Transaction API, does its work, and ends the branch. It
 *       never commits or rolls back; that is the initiator's responsibility.
 *   <li><b>Within this service alone</b> — the request carries no transaction ID, so the work
 *       touches only this service's database. It runs as an ordinary transaction through the
 *       Transaction API and commits on its own.
 * </ul>
 *
 * <p>The Microservice Transaction API is for transactions that span services. A transaction that
 * stays inside one service has nothing to coordinate across processes, so it uses the Transaction
 * API instead.
 *
 * <p>The same code runs unchanged against both ScalarDB Cluster deployment patterns. Only the
 * configuration file differs.
 */
public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase
    implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  private static final int MAX_ATTEMPTS = 3;
  private static final int RETRY_INTERVAL_MILLIS = 100;

  // The Transaction API, for work that stays within this service.
  private final DistributedTransactionManager transactionManager;

  // The Microservice Transaction API, for this service's branches of transactions that span
  // services.
  private final GlobalTransactionManager globalTransactionManager;

  /**
   * Work this service does. It takes the common type of a branch and an ordinary transaction, so
   * the same work can run either way.
   */
  private interface Operations<T> {
    T apply(TransactionCrudOperable transaction) throws Exception;
  }

  public CustomerService(String configFile) throws TransactionException, IOException {
    TransactionFactory factory = TransactionFactory.create(configFile);
    transactionManager = factory.getTransactionManager();
    globalTransactionManager = factory.getGlobalTransactionManager();

    loadInitialData();
  }

  private void loadInitialData() throws TransactionException {
    DistributedTransaction transaction = transactionManager.begin();
    try {
      if (!Customer.get(transaction, 1).isPresent()) {
        Customer.insert(transaction, 1, "Yamada Taro", 10000, 0);
      }
      if (!Customer.get(transaction, 2).isPresent()) {
        Customer.insert(transaction, 2, "Yamada Hanako", 10000, 0);
      }
      if (!Customer.get(transaction, 3).isPresent()) {
        Customer.insert(transaction, 3, "Suzuki Ichiro", 10000, 0);
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("Loading initial data failed", e);
      rollback(transaction);
      if (e instanceof TransactionException) {
        throw (TransactionException) e;
      }
      throw new IllegalStateException("Loading initial data failed", e);
    }
  }

  /**
   * Retrieves customer information. Joins the caller's microservice transaction when the request
   * carries a transaction ID, and runs within this service alone otherwise.
   */
  @Override
  public void getCustomerInfo(
      GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {
    String funcName = "Getting customer info";

    Operations<GetCustomerInfoResponse> operations =
        transaction -> {
          Customer customer = getCustomer(transaction, request.getCustomerId());
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
      execLocally(funcName, /* readOnly= */ true, operations, responseObserver);
    }
  }

  /**
   * Credit card payment. Always part of a microservice transaction: Order Service begins the
   * transaction and passes its ID here.
   */
  @Override
  public void payment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
    execAsParticipant(
        "Payment",
        request.getTransactionId(),
        transaction -> {
          Customer customer = getCustomer(transaction, request.getCustomerId());
          int updatedCreditTotal = customer.creditTotal + request.getAmount();

          if (updatedCreditTotal > customer.creditLimit) {
            // A deterministic business failure. FAILED_PRECONDITION tells the initiator not to
            // retry: another attempt would fail the same way.
            throw Status.FAILED_PRECONDITION
                .withDescription("Credit limit exceeded")
                .asRuntimeException();
          }

          Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditTotal);
          return PaymentResponse.getDefaultInstance();
        },
        responseObserver);
  }

  /** Credit card repayment. Always within this service alone: it involves no other service. */
  @Override
  public void repayment(
      RepaymentRequest request, StreamObserver<RepaymentResponse> responseObserver) {
    execLocally(
        "Repayment",
        /* readOnly= */ false,
        transaction -> {
          Customer customer = getCustomer(transaction, request.getCustomerId());
          int updatedCreditTotal = customer.creditTotal - request.getAmount();

          if (updatedCreditTotal < 0) {
            throw Status.FAILED_PRECONDITION.withDescription("Over-repayment").asRuntimeException();
          }

          Customer.updateCreditTotal(transaction, request.getCustomerId(), updatedCreditTotal);
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
      Operations<T> operations,
      StreamObserver<T> responseObserver) {
    BranchTransaction branch = null;
    T result;
    try {
      branch = globalTransactionManager.beginBranch(transactionId);
      result = operations.apply(branch);

      branch.end(BranchTransaction.Status.SUCCESS);
    } catch (Exception e) {
      // Ending the branch is this process's obligation, and it holds whether the work succeeded or
      // failed. The initiator cannot discharge it on our behalf.
      endBranchWithFailure(branch);
      respondWithError(funcName, e, responseObserver);
      return;
    }

    // Delivered after the branch has ended, so a failure to deliver can never be mistaken for a
    // failure of the branch.
    respond(funcName, result, responseObserver);
  }

  /**
   * Runs work that touches only this service's database as an ordinary transaction, and commits it.
   * This service owns the whole transaction here, so it is also the one that retries.
   */
  private <T> void execLocally(
      String funcName,
      boolean readOnly,
      Operations<T> operations,
      StreamObserver<T> responseObserver) {
    int attempt = 0;
    Exception lastException = null;

    T result;
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

      DistributedTransaction transaction = null;
      try {
        transaction = readOnly ? transactionManager.beginReadOnly() : transactionManager.begin();
        result = operations.apply(transaction);
        transaction.commit();

        // The transaction is over. Leave the retry loop before delivering the response: once the
        // commit has succeeded there is nothing left to retry, and a failure to deliver must never
        // start a second transaction and apply the credit update twice.
        break;
      } catch (UnknownTransactionStatusException e) {
        // The outcome is unknown, so retrying could duplicate a committed transaction. Determining
        // the actual status is the application's responsibility.
        respondWithError(funcName, e, responseObserver);
        return;
      } catch (StatusRuntimeException e) {
        rollback(transaction);

        if (e.getStatus().getCode() == Status.Code.NOT_FOUND
            || e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
          responseObserver.onError(e);
          return;
        }
        lastException = e;
      } catch (Exception e) {
        rollback(transaction);
        lastException = e;
      }
    }

    respond(funcName, result, responseObserver);
  }

  private Customer getCustomer(TransactionCrudOperable transaction, int customerId)
      throws Exception {
    Optional<Customer> customer = Customer.get(transaction, customerId);
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

  private void rollback(@Nullable DistributedTransaction transaction) {
    if (transaction == null) {
      return;
    }
    try {
      transaction.rollback();
    } catch (RollbackException e) {
      logger.warn("Rolling back the transaction failed", e);
    }
  }

  /**
   * Delivers a successful response. Called only once this service's part of the transaction is
   * over, so a failure here is never retried: there is nothing left to undo, and the caller has
   * simply stopped listening.
   */
  private <T> void respond(String funcName, T result, StreamObserver<T> responseObserver) {
    try {
      responseObserver.onNext(result);
      responseObserver.onCompleted();
    } catch (RuntimeException e) {
      logger.warn("Delivering the response failed after {} had already succeeded", funcName, e);
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
    transactionManager.close();
    globalTransactionManager.close();
  }
}
