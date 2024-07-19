package sample.customer;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Properties;
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

public class CustomerService extends CustomerServiceGrpc.CustomerServiceImplBase
    implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  // The properties loaded from the configuration file
  private final Properties properties;

  private interface TransactionFunction<T> {
    T apply(Connection t) throws SQLException;
  }

  public CustomerService(String configFile) throws SQLException, IOException {
    // Load the configuration
    properties = new Properties();
    properties.load(Files.newBufferedReader(Paths.get(configFile)));

    // Set the default auto commit to false
    properties.setProperty("scalar.db.sql.jdbc.default_auto_commit", "false");

    loadInitialData();
  }

  private void loadInitialData() throws SQLException {
    Connection connection = null;
    try {
      // Get a connection
      connection = getConnection();

      // Load initial data
      loadCustomerIfNotExists(connection, 1, "Yamada Taro", 10000, 0);
      loadCustomerIfNotExists(connection, 2, "Yamada Hanako", 10000, 0);
      loadCustomerIfNotExists(connection, 3, "Suzuki Ichiro", 10000, 0);

      // Commit the transaction
      connection.commit();
    } catch (SQLException e) {
      logger.error("Loading initial data failed", e);

      // Rollback the transaction
      rollbackTransaction(connection);

      throw e;
    } finally {
      closeConnection(connection);
    }
  }

  private void loadCustomerIfNotExists(
      Connection connection, int id, String name, int creditLimit, int creditTotal)
      throws SQLException {
    Optional<Customer> customer = Customer.get(connection, id);
    if (!customer.isPresent()) {
      Customer.put(connection, id, name, creditLimit, creditTotal);
    }
  }

  // Get customer information. This function processing operations can be used in both a normal
  // transaction and a global transaction.
  @Override
  public void getCustomerInfo(
      GetCustomerInfoRequest request, StreamObserver<GetCustomerInfoResponse> responseObserver) {
    String funcName = "Getting customer info";

    TransactionFunction<GetCustomerInfoResponse> operations =
        connection -> {
          // Retrieve the customer info for the specified customer ID
          Optional<Customer> result = Customer.get(connection, request.getCustomerId());

          if (!result.isPresent()) {
            // If the customer info for the specified customer ID doesn't exist, throw an exception
            throw Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException();
          }

          // Return the customer info
          return GetCustomerInfoResponse.newBuilder()
              .setId(result.get().id)
              .setName(result.get().name)
              .setCreditLimit(result.get().creditLimit)
              .setCreditTotal(result.get().creditTotal)
              .build();
        };

    if (request.hasTransactionId()) {
      // For a global transaction, execute the operations as a participant
      execOperationsAsParticipant(
          funcName, request.getTransactionId(), operations, responseObserver);
    } else {
      // For a normal transaction, execute the operations
      execOperations(funcName, operations, responseObserver);
    }
  }

  // Credit card payment. It's for a global transaction that spans OrderService and CustomerService.
  @Override
  public void payment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
    execOperationsAsParticipant(
        "Payment",
        request.getTransactionId(),
        connection -> {
          // Retrieve the customer info for the specified customer ID
          Optional<Customer> result = Customer.get(connection, request.getCustomerId());
          if (!result.isPresent()) {
            throw Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException();
          }

          int updatedCreditTotal = result.get().creditTotal + request.getAmount();

          // Check if the credit total exceeds the credit limit after payment
          if (updatedCreditTotal > result.get().creditLimit) {
            throw Status.FAILED_PRECONDITION
                .withDescription("Credit limit exceeded")
                .asRuntimeException();
          }

          // Update credit_total for the customer
          Customer.updateCreditTotal(connection, request.getCustomerId(), updatedCreditTotal);

          return PaymentResponse.getDefaultInstance();
        },
        responseObserver);
  }

  // Credit card repayment.
  @Override
  public void repayment(
      RepaymentRequest request, StreamObserver<RepaymentResponse> responseObserver) {
    execOperations(
        "Repayment",
        connection -> {
          // Retrieve the customer info for the specified customer ID
          Optional<Customer> result = Customer.get(connection, request.getCustomerId());
          if (!result.isPresent()) {
            // If the customer info for the specified customer ID doesn't exist, throw an exception
            throw Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException();
          }

          int updatedCreditTotal = result.get().creditTotal - request.getAmount();

          // Check if over repayment or not
          if (updatedCreditTotal < 0) {
            throw Status.FAILED_PRECONDITION.withDescription("Over repayment").asRuntimeException();
          }

          // Reduce credit_total for the customer
          Customer.updateCreditTotal(connection, request.getCustomerId(), updatedCreditTotal);

          return RepaymentResponse.getDefaultInstance();
        },
        responseObserver);
  }

  private <T> void execOperations(
      String funcName, TransactionFunction<T> operations, StreamObserver<T> responseObserver) {
    int retryCount = 0;
    Exception lastException = null;

    while (true) {
      if (retryCount++ > 0) {
        // Retry the transaction three times maximum.

        if (retryCount >= 3) {
          // If the transaction failed three times, return an error.

          String message = funcName + " failed";
          logger.error(message, lastException);

          responseObserver.onError(
              Status.INTERNAL
                  .withDescription(message)
                  .withCause(lastException)
                  .asRuntimeException());
          return;
        }

        logger.warn("Retrying the transaction after 100 milliseconds: {}", funcName, lastException);

        // Sleep 100 milliseconds before retrying the transaction.
        Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
      }

      Connection connection = null;
      try {
        // Get a connection
        connection = getConnection();

        // Execute operations
        T response = operations.apply(connection);

        // Commit the transaction (even when the transaction is read-only, we need to commit)
        connection.commit();

        // Return the response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      } catch (StatusRuntimeException e) {
        // For `StatusRuntimeException`, you cannot retry the transaction
        logger.error("{} failed", funcName, e);

        // Rollback the transaction
        rollbackTransaction(connection);

        responseObserver.onError(e);
      } catch (SQLException e) {
        if (e.getErrorCode() == 301) {
          // The error code 301 indicates that you catch `UnknownTransactionStatusException`.
          // If you catch `UnknownTransactionStatusException`, it indicates that the status of the
          // transaction, whether it has succeeded or not, is unknown. In such a case, you need to
          // check if the transaction is committed successfully or not and retry it if it failed.
          // How to identify a transaction status is delegated to users

          String message = funcName + " failed";
          logger.error(message, lastException);

          responseObserver.onError(
              Status.INTERNAL
                  .withDescription(message)
                  .withCause(lastException)
                  .asRuntimeException());
          return;
        } else {
          // For other cases, you can try retrying the transaction

          // Rollback the transaction
          rollbackTransaction(connection);

          // The cause of the exception can be `TransactionRetryableException` or the other
          // exceptions. For `TransactionRetryableException`, you can basically retry the
          // transaction. However, for the other exceptions, the transaction may still fail if the
          // cause of the exception is nontransient. For such a case, you need to limit the number
          // of retries and give up retrying
          lastException = e;
        }
      } finally {
        closeConnection(connection);
      }
    }
  }

  private <T> void execOperationsAsParticipant(
      String funcName,
      String transactionId,
      TransactionFunction<T> operations,
      StreamObserver<T> responseObserver) {
    Connection connection = null;
    try {
      // Get a connection
      connection = getConnection();

      // Join the transaction
      try (Statement statement = connection.createStatement()) {
        statement.execute("JOIN '" + transactionId + "'");
      }

      // Execute operations
      T response = operations.apply(connection);

      // Return the response
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (StatusRuntimeException e) {
      logger.error("{} failed", funcName, e);
      responseObserver.onError(e);
    } catch (SQLException e) {
      String message = funcName + " failed";
      logger.error(message, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } finally {
      closeConnection(connection);
    }
  }

  private Connection getConnection() throws SQLException {
    // Get a connection with the properties loaded from the configuration file
    return DriverManager.getConnection("jdbc:scalardb:", properties);
  }

  private void rollbackTransaction(@Nullable Connection connection) {
    if (connection == null) {
      return;
    }
    try {
      connection.rollback();
    } catch (SQLException e) {
      logger.warn("Rollback failed", e);
    }
  }

  private void closeConnection(Connection connection) {
    if (connection == null) {
      return;
    }
    try {
      connection.close();
    } catch (SQLException e) {
      logger.warn("Failed to close the connection", e);
    }
  }

  @Override
  public void close() {}
}
