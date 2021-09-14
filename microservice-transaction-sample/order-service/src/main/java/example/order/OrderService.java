package example.order;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.api.TwoPhaseCommitTransactionManager;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.exception.transaction.AbortException;
import com.scalar.db.exception.transaction.CommitException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.PreparationException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import com.scalar.db.exception.transaction.ValidationException;
import com.scalar.db.service.TransactionFactory;
import example.order.model.Item;
import example.order.model.Order;
import example.order.model.Statement;
import example.rpc.CommitRequest;
import example.rpc.CustomerServiceGrpc;
import example.rpc.GetCustomerInfoRequest;
import example.rpc.GetCustomerInfoResponse;
import example.rpc.GetOrderRequest;
import example.rpc.GetOrderResponse;
import example.rpc.GetOrdersRequest;
import example.rpc.GetOrdersResponse;
import example.rpc.ItemOrder;
import example.rpc.JoinRequest;
import example.rpc.OrderServiceGrpc;
import example.rpc.PaymentRequest;
import example.rpc.PlaceOrderRequest;
import example.rpc.PlaceOrderResponse;
import example.rpc.PrepareRequest;
import example.rpc.RollbackRequest;
import example.rpc.ValidateRequest;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService extends OrderServiceGrpc.OrderServiceImplBase implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

  // For normal transactions
  private final DistributedTransactionManager transactionManager;

  // For two-phase commit transactions
  private final TwoPhaseCommitTransactionManager twoPhaseCommitTransactionManager;

  // For gRPC connection to Customer service
  private final ManagedChannel channel;
  private final CustomerServiceGrpc.CustomerServiceBlockingStub stub;

  public OrderService(DatabaseConfig config) throws TransactionException {
    // Initialize the transaction managers
    TransactionFactory factory = new TransactionFactory(config);
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
      LOGGER.error("loading initial data failed", e);
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

      // Customer Service joins the transaction
      join(transaction.getId());

      // CRUD operations
      Order.put(transaction, orderId, request.getCustomerId(), System.currentTimeMillis());

      int amount = 0;
      for (ItemOrder itemOrder : request.getItemOrderList()) {
        Statement.put(transaction, orderId, itemOrder.getItemId(), itemOrder.getCount());

        Optional<Item> item = Item.get(transaction, itemOrder.getItemId());
        if (!item.isPresent()) {
          responseObserver.onError(
              Status.NOT_FOUND.withDescription("Item not found").asRuntimeException());
          return;
        }
        amount += item.get().price * itemOrder.getCount();
      }

      payment(transaction.getId(), request.getCustomerId(), amount);

      // Prepare the transaction
      prepare(transaction);

      // Validate the transaction. Depending on the concurrency control protocol, you need to call
      // validate(). Currently, you need to call it when you use the Consensus Commit transaction
      // manager and EXTRA_READ serializable strategy in SERIALIZABLE isolation level. In other
      // cases, validate() does nothing.
      validate(transaction);

      // Commit the transaction
      commit(transaction);

      responseObserver.onNext(PlaceOrderResponse.newBuilder().setOrderId(orderId).build());
      responseObserver.onCompleted();
    } catch (UnknownTransactionStatusException e) {
      String message =
          "the transaction status is unknown. need to check the status manually and handle it properly";
      LOGGER.error(message, e);

      // Rollback the transaction
      rollback(transaction);

      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (TransactionException e) {
      String message = "placing order failed";
      LOGGER.error(message, e);

      // Rollback the transaction
      rollback(transaction);

      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (StatusRuntimeException e) {
      // Rollback the transaction
      rollback(transaction);

      LOGGER.error("placing order failed", e);
      responseObserver.onError(e);
    }
  }

  private void payment(String transactionId, int customerId, int amount) {
    stub.payment(
        PaymentRequest.newBuilder()
            .setTransactionId(transactionId)
            .setCustomerId(customerId)
            .setAmount(amount)
            .build());
  }

  private void join(String transactionId) {
    stub.join(JoinRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void prepare(TwoPhaseCommitTransaction transaction) throws PreparationException {
    transaction.prepare();
    stub.prepare(PrepareRequest.newBuilder().setTransactionId(transaction.getId()).build());
  }

  private void validate(TwoPhaseCommitTransaction transaction) throws ValidationException {
    transaction.validate();
    stub.validate(ValidateRequest.newBuilder().setTransactionId(transaction.getId()).build());
  }

  private void commit(TwoPhaseCommitTransaction transaction)
      throws CommitException, UnknownTransactionStatusException {
    transaction.commit();
    stub.commit(CommitRequest.newBuilder().setTransactionId(transaction.getId()).build());
  }

  private void rollback(@Nullable TwoPhaseCommitTransaction transaction) {
    if (transaction == null) {
      return;
    }
    try {
      transaction.rollback();
      stub.rollback(RollbackRequest.newBuilder().setTransactionId(transaction.getId()).build());
    } catch (Exception e) {
      String message = "rollback failed";
      LOGGER.warn(message, e);
    }
  }

  /** Get Order information by order ID */
  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
    DistributedTransaction transaction = null;
    try {
      transaction = transactionManager.start();

      Optional<Order> order = Order.getById(transaction, request.getOrderId());
      if (!order.isPresent()) {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Order not found").asRuntimeException());
        return;
      }

      example.rpc.Order rpcOrder = getOrder(transaction, order.get());

      responseObserver.onNext(GetOrderResponse.newBuilder().setOrder(rpcOrder).build());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "getting order failed";
      LOGGER.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (StatusRuntimeException e) {
      LOGGER.error("getting order failed", e);
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
      transaction = transactionManager.start();

      List<Order> orders = Order.getByCustomerId(transaction, request.getCustomerId());

      GetOrdersResponse.Builder builder = GetOrdersResponse.newBuilder();
      for (Order order : orders) {
        example.rpc.Order rpcOrder = getOrder(transaction, order);
        builder.addOrder(rpcOrder);
      }

      responseObserver.onNext(builder.build());
      responseObserver.onCompleted();
    } catch (TransactionException e) {
      String message = "getting order failed";
      LOGGER.error(message, e);
      abortTransaction(transaction);
      responseObserver.onError(
          Status.INTERNAL.withDescription(message).withCause(e).asRuntimeException());
    } catch (StatusRuntimeException e) {
      LOGGER.error("getting order failed", e);
      abortTransaction(transaction);
      responseObserver.onError(e);
    }
  }

  private example.rpc.Order getOrder(DistributedTransaction transaction, Order order)
      throws CrudException {
    String customerName = getCustomerName(order.customerId);

    example.rpc.Order.Builder orderBuilder =
        example.rpc.Order.newBuilder()
            .setOrderId(order.id)
            .setCustomerId(order.customerId)
            .setCustomerName(customerName)
            .setTimestamp(order.timestamp);

    int total = 0;
    List<Statement> statements = Statement.getByOrderId(transaction, order.id);
    for (Statement statement : statements) {
      example.rpc.Statement.Builder statementBuilder = example.rpc.Statement.newBuilder();
      statementBuilder.setItemId(statement.itemId);

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
      LOGGER.warn("abort failed", e);
    }
  }

  @Override
  public void close() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.warn("failed to shutdown the channel", e);
    }

    transactionManager.close();
    twoPhaseCommitTransactionManager.close();
  }
}
