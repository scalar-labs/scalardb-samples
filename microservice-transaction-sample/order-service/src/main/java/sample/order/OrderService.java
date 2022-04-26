package sample.order;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.api.TwoPhaseCommitTransactionManager;
import com.scalar.db.exception.transaction.AbortException;
import com.scalar.db.exception.transaction.CommitException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.PreparationException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import com.scalar.db.exception.transaction.ValidationException;
import com.scalar.db.service.TransactionFactory;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.order.model.Item;
import sample.order.model.Order;
import sample.order.model.Statement;
import sample.rpc.CommitRequest;
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
import sample.rpc.PrepareRequest;
import sample.rpc.RollbackRequest;
import sample.rpc.ValidateRequest;

public class OrderService extends OrderServiceGrpc.OrderServiceImplBase implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

  // For normal transactions
  private final DistributedTransactionManager transactionManager;

  // For two-phase commit transactions
  private final TwoPhaseCommitTransactionManager twoPhaseCommitTransactionManager;

  // For gRPC connection to Customer service
  private final ManagedChannel channel;
  private final CustomerServiceGrpc.CustomerServiceBlockingStub stub;

  public OrderService(String configFile) throws TransactionException, IOException {
    // Initialize the transaction managers
    TransactionFactory factory = TransactionFactory.create(configFile);
    transactionManager = factory.getTransactionManager();
    twoPhaseCommitTransactionManager = factory.getTwoPhaseCommitTransactionManager();

    // Initialize the gRPC connection to Customer service
    channel = NettyChannelBuilder.forAddress("customer-service", 10010).usePlaintext().build();
    stub = CustomerServiceGrpc.newBlockingStub(channel);

    loadInitialData();
  }

  private void loadInitialData() throws TransactionException {
    DistributedTransaction transaction = null;
    try {
      transaction = transactionManager.start();
      loadItemIfNotExists(transaction, 1, "Apple", 1000);
      loadItemIfNotExists(transaction, 2, "Orange", 2000);
      loadItemIfNotExists(transaction, 3, "Grape", 2500);
      loadItemIfNotExists(transaction, 4, "Mango", 5000);
      loadItemIfNotExists(transaction, 5, "Melon", 3000);
      transaction.commit();
    } catch (TransactionException e) {
      logger.error("loading initial data failed", e);
      abortTransaction(transaction);
      throw e;
    }
  }

  private void loadItemIfNotExists(
      DistributedTransaction transaction, int id, String name, int price) throws CrudException {
    Optional<Item> item = Item.get(transaction, id);
    if (!item.isPresent()) {
      Item.put(transaction, id, name, price);
    }
  }

  /** Place an order. It's a transaction that spans OrderService and CustomerService */
  @Override
  public void placeOrder(
      PlaceOrderRequest request, StreamObserver<PlaceOrderResponse> responseObserver) {
    TwoPhaseCommitTransaction transaction = null;
    try {
      String orderId = UUID.randomUUID().toString();

      // Start a two-phase commit transaction
      transaction = twoPhaseCommitTransactionManager.start();

      // Put the order info into the orders table
      Order.put(transaction, orderId, request.getCustomerId(), System.currentTimeMillis());

      int amount = 0;
      for (ItemOrder itemOrder : request.getItemOrderList()) {
        // Put the order statement into the statements table
        Statement.put(transaction, orderId, itemOrder.getItemId(), itemOrder.getCount());

        // Retrieve the item info from the items table
        Optional<Item> item = Item.get(transaction, itemOrder.getItemId());
        if (!item.isPresent()) {
          responseObserver.onError(
              Status.NOT_FOUND.withDescription("Item not found").asRuntimeException());
          return;
        }

        // Calculate the total amount
        amount += item.get().price * itemOrder.getCount();
      }

      // Call the payment endpoint of Customer service
      callPaymentEndpoint(transaction.getId(), request.getCustomerId(), amount);

      // Prepare the transaction
      transaction.prepare();
      callPrepareEndpoint(transaction.getId());

      // Validate the transaction. Depending on the concurrency control protocol, you need to call
      // validate(). Currently, you need to call it when you use the Consensus Commit transaction
      // manager and EXTRA_READ serializable strategy in SERIALIZABLE isolation level. In other
      // cases, validate() does nothing.
      transaction.validate();
      callValidateEndpoint(transaction.getId());

      // Commit the transaction
      transaction.commit();
      callCommitEndpoint(transaction.getId());

      // Return the order id
      responseObserver.onNext(PlaceOrderResponse.newBuilder().setOrderId(orderId).build());
      responseObserver.onCompleted();
    } catch (UnknownTransactionStatusException e) {
      String message =
          "the transaction status is unknown. need to check the status manually and handle it properly";
      logger.error(message, e);

      // Rollback the transaction
      if (transaction != null) {
        try {
          transaction.rollback();
          callRollbackEndpoint(transaction.getId());
        } catch (Exception ex) {
          logger.warn("rollback failed", ex);
        }
      }

      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (TransactionException e) {
      String message = "placing order failed";
      logger.error(message, e);

      // Rollback the transaction
      if (transaction != null) {
        try {
          transaction.rollback();
          callRollbackEndpoint(transaction.getId());
        } catch (Exception ex) {
          logger.warn("rollback failed", ex);
        }
      }

      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (StatusRuntimeException e) {
      logger.error("placing order failed", e);

      // Rollback the transaction
      if (transaction != null) {
        try {
          transaction.rollback();
          callRollbackEndpoint(transaction.getId());
        } catch (Exception ex) {
          logger.warn("rollback failed", ex);
        }
      }

      responseObserver.onError(e);
    }
  }

  private void callPaymentEndpoint(String transactionId, int customerId, int amount) {
    stub.payment(
        PaymentRequest.newBuilder()
            .setTransactionId(transactionId)
            .setCustomerId(customerId)
            .setAmount(amount)
            .build());
  }

  private void callPrepareEndpoint(String transactionId) throws PreparationException {
    stub.prepare(PrepareRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void callValidateEndpoint(String transactionId) throws ValidationException {
    stub.validate(ValidateRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void callCommitEndpoint(String transactionId)
      throws CommitException, UnknownTransactionStatusException {
    stub.commit(CommitRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void callRollbackEndpoint(String transactionId) {
    stub.rollback(RollbackRequest.newBuilder().setTransactionId(transactionId).build());
  }

  /** Get Order information by order ID */
  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
    DistributedTransaction transaction = null;
    try {
      // Start a transaction
      transaction = transactionManager.start();

      // Retrieve the order info for the specified order ID
      Optional<Order> order = Order.getById(transaction, request.getOrderId());
      if (!order.isPresent()) {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Order not found").asRuntimeException());
        return;
      }

      // Make an order protobuf to return
      sample.rpc.Order rpcOrder = getOrder(transaction, order.get());

      // Commit the transaction (even when the transaction is read-only, we need to commit)
      transaction.commit();

      responseObserver.onNext(GetOrderResponse.newBuilder().setOrder(rpcOrder).build());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "getting order failed";
      logger.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (StatusRuntimeException e) {
      logger.error("getting order failed", e);
      abortTransaction(transaction);
      responseObserver.onError(e);
    }
  }

  /** Get Order information by customer ID */
  @Override
  public void getOrders(
      GetOrdersRequest request, StreamObserver<GetOrdersResponse> responseObserver) {
    DistributedTransaction transaction = null;
    try {
      // Start a transaction
      transaction = transactionManager.start();

      // Retrieve the order info for the specified customer ID
      List<Order> orders = Order.getByCustomerId(transaction, request.getCustomerId());

      GetOrdersResponse.Builder builder = GetOrdersResponse.newBuilder();
      for (Order order : orders) {
        // Make an order protobuf to return
        sample.rpc.Order rpcOrder = getOrder(transaction, order);
        builder.addOrder(rpcOrder);
      }

      // Commit the transaction (even when the transaction is read-only, we need to commit)
      transaction.commit();

      responseObserver.onNext(builder.build());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "getting order failed";
      logger.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (StatusRuntimeException e) {
      logger.error("getting order failed", e);
      abortTransaction(transaction);
      responseObserver.onError(e);
    }
  }

  private sample.rpc.Order getOrder(DistributedTransaction transaction, Order order)
      throws CrudException {
    // Get the customer name from Customer service
    String customerName = getCustomerName(order.customerId);

    sample.rpc.Order.Builder orderBuilder =
        sample.rpc.Order.newBuilder()
            .setOrderId(order.id)
            .setCustomerId(order.customerId)
            .setCustomerName(customerName)
            .setTimestamp(order.timestamp);

    int total = 0;

    // Retrieve the order statements for the order ID from the statements table
    List<Statement> statements = Statement.getByOrderId(transaction, order.id);

    // Make statements
    for (Statement statement : statements) {
      sample.rpc.Statement.Builder statementBuilder = sample.rpc.Statement.newBuilder();
      statementBuilder.setItemId(statement.itemId);

      // Retrieve the item data from the items table
      Optional<Item> item = Item.get(transaction, statement.itemId);
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

  private String getCustomerName(int customerId) {
    GetCustomerInfoResponse customerInfo =
        stub.getCustomerInfo(GetCustomerInfoRequest.newBuilder().setCustomerId(customerId).build());
    return customerInfo.getName();
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
  public void close() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.warn("failed to shutdown the channel", e);
    }

    transactionManager.close();
    twoPhaseCommitTransactionManager.close();
  }
}
