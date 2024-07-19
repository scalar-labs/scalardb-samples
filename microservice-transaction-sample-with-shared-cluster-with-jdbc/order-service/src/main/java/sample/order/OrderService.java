package sample.order;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.order.model.Item;
import sample.order.model.Order;
import sample.order.model.Statement;
import sample.rpc.CustomerServiceGrpc;
import sample.rpc.GetCustomerInfoRequest;
import sample.rpc.GetCustomerInfoResponse;
import sample.rpc.GetOrderRequest;
import sample.rpc.GetOrderResponse;
import sample.rpc.GetOrdersRequest;
import sample.rpc.GetOrdersResponse;
import sample.rpc.ItemOrder;
import sample.rpc.OrderServiceGrpc;
import sample.rpc.PaymentRequest;
import sample.rpc.PlaceOrderRequest;
import sample.rpc.PlaceOrderResponse;

public class OrderService extends OrderServiceGrpc.OrderServiceImplBase implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

  // For gRPC connection to Customer service
  private final ManagedChannel channel;

  // The properties loaded from the configuration file
  private final Properties properties;

  private final CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceStub;

  private interface TransactionFunction<T> {
    T apply(Connection connection, String transactionId) throws SQLException;
  }

  public OrderService(String configFile) throws SQLException, IOException {
    // Initialize the gRPC connection to Customer service
    channel = NettyChannelBuilder.forAddress("customer-service", 10010).usePlaintext().build();
    customerServiceStub = CustomerServiceGrpc.newBlockingStub(channel);

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
      loadItemIfNotExists(connection, 1, "Apple", 1000);
      loadItemIfNotExists(connection, 2, "Orange", 2000);
      loadItemIfNotExists(connection, 3, "Grape", 2500);
      loadItemIfNotExists(connection, 4, "Mango", 5000);
      loadItemIfNotExists(connection, 5, "Melon", 3000);

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

  private void loadItemIfNotExists(Connection connection, int id, String name, int price)
      throws SQLException {
    Optional<Item> item = Item.get(connection, id);
    if (!item.isPresent()) {
      Item.put(connection, id, name, price);
    }
  }

  // Place an order. It's a transaction that spans OrderService and CustomerService
  @Override
  public void placeOrder(
      PlaceOrderRequest request, StreamObserver<PlaceOrderResponse> responseObserver) {
    execOperationsAsCoordinator(
        "Placing an order",
        (connection, transactionId) -> {
          String orderId = UUID.randomUUID().toString();

          // Put the order info into the orders table
          Order.put(connection, orderId, request.getCustomerId(), System.currentTimeMillis());

          int amount = 0;
          for (ItemOrder itemOrder : request.getItemOrderList()) {
            // Put the order statement into the statements table
            Statement.put(connection, orderId, itemOrder.getItemId(), itemOrder.getCount());

            // Retrieve the item info from the items table
            Optional<Item> item = Item.get(connection, itemOrder.getItemId());
            if (!item.isPresent()) {
              throw Status.NOT_FOUND.withDescription("Item not found").asRuntimeException();
            }

            // Calculate the total amount
            amount += item.get().price * itemOrder.getCount();
          }

          // Call the payment endpoint of Customer service
          callPaymentEndpoint(transactionId, request.getCustomerId(), amount);

          return PlaceOrderResponse.newBuilder().setOrderId(orderId).build();
        },
        responseObserver);
  }

  private void callPaymentEndpoint(String transactionId, int customerId, int amount) {
    customerServiceStub.payment(
        PaymentRequest.newBuilder()
            .setTransactionId(transactionId)
            .setCustomerId(customerId)
            .setAmount(amount)
            .build());
  }

  // Get Order information by order ID
  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
    execOperationsAsCoordinator(
        "Getting an order",
        (connection, transactionId) -> {
          // Retrieve the order info for the specified order ID
          Optional<Order> order = Order.getById(connection, request.getOrderId());
          if (!order.isPresent()) {
            throw Status.NOT_FOUND.withDescription("Order not found").asRuntimeException();
          }

          // Get the customer name from the Customer service
          String customerName = getCustomerName(transactionId, order.get().customerId);

          // Make an order protobuf to return
          sample.rpc.Order rpcOrder = getOrder(connection, order.get(), customerName);

          return GetOrderResponse.newBuilder().setOrder(rpcOrder).build();
        },
        responseObserver);
  }

  // Get Order information by customer ID
  @Override
  public void getOrders(
      GetOrdersRequest request, StreamObserver<GetOrdersResponse> responseObserver) {
    execOperationsAsCoordinator(
        "Getting orders",
        (connection, transactionId) -> {
          // Retrieve the order info for the specified customer ID
          List<Order> orders = Order.getByCustomerId(connection, request.getCustomerId());

          // Get the customer name from the Customer service
          String customerName = getCustomerName(transactionId, request.getCustomerId());

          GetOrdersResponse.Builder builder = GetOrdersResponse.newBuilder();
          for (Order order : orders) {
            // Make an order protobuf to return
            sample.rpc.Order rpcOrder = getOrder(connection, order, customerName);
            builder.addOrder(rpcOrder);
          }

          return builder.build();
        },
        responseObserver);
  }

  private sample.rpc.Order getOrder(Connection connection, Order order, String customerName)
      throws SQLException {
    sample.rpc.Order.Builder orderBuilder =
        sample.rpc.Order.newBuilder()
            .setOrderId(order.id)
            .setCustomerId(order.customerId)
            .setCustomerName(customerName)
            .setTimestamp(order.timestamp);

    int total = 0;

    // Retrieve the order statements for the order ID from the statements table
    List<Statement> statements = Statement.getByOrderId(connection, order.id);

    // Make statements
    for (Statement statement : statements) {
      sample.rpc.Statement.Builder statementBuilder = sample.rpc.Statement.newBuilder();
      statementBuilder.setItemId(statement.itemId);

      // Retrieve the item data from the items table
      Optional<Item> item = Item.get(connection, statement.itemId);
      if (!item.isPresent()) {
        throw Status.NOT_FOUND.withDescription("Item not found").asRuntimeException();
      }
      statementBuilder.setItemName(item.get().name);
      statementBuilder.setPrice(item.get().price);
      statementBuilder.setCount(statement.count);

      int itemTotal = item.get().price * statement.count;
      statementBuilder.setTotal(itemTotal);

      orderBuilder.addStatement(statementBuilder);

      total += itemTotal;
    }

    return orderBuilder.setTotal(total).build();
  }

  private String getCustomerName(String transactionId, int customerId) {
    GetCustomerInfoResponse customerInfo =
        customerServiceStub.getCustomerInfo(
            GetCustomerInfoRequest.newBuilder()
                .setTransactionId(transactionId)
                .setCustomerId(customerId)
                .build());
    return customerInfo.getName();
  }

  private <T> void execOperationsAsCoordinator(
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

          if (lastException instanceof StatusRuntimeException) {
            responseObserver.onError(lastException);
          } else {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription(message)
                    .withCause(lastException)
                    .asRuntimeException());
          }
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

        // Begin a transaction
        String transactionId;
        try (java.sql.Statement statement = connection.createStatement()) {
          statement.execute("BEGIN");
          ResultSet resultSet = statement.getResultSet();

          // Get the transaction ID from the result set of the `BEGIN` statement
          resultSet.next();
          transactionId = resultSet.getString(1);
        }

        // Execute operations
        T result = operations.apply(connection, transactionId);

        // Commit the transaction
        connection.commit();

        // Return the response
        responseObserver.onNext(result);
        responseObserver.onCompleted();
      } catch (StatusRuntimeException e) {
        if (e.getStatus().getCode() == Status.Code.NOT_FOUND
            || e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
          // For `NOT_FOUND` and `FAILED_PRECONDITION` errors, you cannot retry the transaction

          // Rollback the transaction
          rollbackTransaction(connection);

          responseObserver.onError(e);
          return;
        } else {
          // For other gRPC errors, you can try retrying the transaction

          // Rollback the transaction
          rollbackTransaction(connection);

          lastException = e;
        }
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
  public void close() {
    // Shutdown the gRPC channel to Customer service
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.warn("Failed to shutdown the channel", e);
    }
  }
}
