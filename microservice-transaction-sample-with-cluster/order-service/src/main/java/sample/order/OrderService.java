package sample.order;

import com.google.common.util.concurrent.Uninterruptibles;
import com.scalar.db.api.BranchTransaction;
import com.scalar.db.api.GlobalTransaction;
import com.scalar.db.api.GlobalTransactionManager;
import com.scalar.db.exception.transaction.RollbackException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import com.scalar.db.service.TransactionFactory;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
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

/**
 * Order Service. This service is the <em>initiator</em> of the microservice transactions in this
 * sample: it begins the transaction, shares its ID with Customer Service, and drives the final
 * commit or rollback.
 *
 * <p>The same code runs unchanged against both ScalarDB Cluster deployment patterns (Shared Cluster
 * and Separated Clusters with a Transaction Coordinator). Only the configuration file differs.
 */
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

  private static final int MAX_ATTEMPTS = 3;
  private static final int RETRY_INTERVAL_MILLIS = 100;

  // The manager for the Microservice Transaction API. Its Java type is `GlobalTransactionManager`.
  private final GlobalTransactionManager manager;

  // For the gRPC connection to Customer Service
  private final ManagedChannel channel;
  private final CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceStub;

  /** Work this service does on its own branch of the transaction. */
  private interface BranchOperations<I> {
    I apply(BranchTransaction branch) throws Exception;
  }

  /**
   * Work that happens after this service's branch has ended: calling the other services with the
   * transaction ID, and assembling the response.
   */
  private interface AfterBranchOperations<I, T> {
    T apply(I intermediate, String transactionId);
  }

  public OrderService(String configFile) throws TransactionException, IOException {
    TransactionFactory factory = TransactionFactory.create(configFile);
    manager = factory.getGlobalTransactionManager();

    channel = NettyChannelBuilder.forAddress("customer-service", 10010).usePlaintext().build();
    customerServiceStub = CustomerServiceGrpc.newBlockingStub(channel);

    loadInitialData();
  }

  private void loadInitialData() throws TransactionException {
    GlobalTransaction global = manager.begin();
    BranchTransaction branch = null;
    try {
      branch = manager.beginBranch(global.getId());
      loadItemIfNotExists(branch, 1, "Apple", 1000);
      loadItemIfNotExists(branch, 2, "Orange", 2000);
      loadItemIfNotExists(branch, 3, "Grape", 2500);
      loadItemIfNotExists(branch, 4, "Mango", 5000);
      loadItemIfNotExists(branch, 5, "Melon", 3000);
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

  private void loadItemIfNotExists(BranchTransaction branch, int id, String name, int price)
      throws Exception {
    if (!Item.get(branch, id).isPresent()) {
      Item.insert(branch, id, name, price);
    }
  }

  /** Places an order. It's a microservice transaction that spans this service and Customer Service. */
  @Override
  public void placeOrder(
      PlaceOrderRequest request, StreamObserver<PlaceOrderResponse> responseObserver) {
    execTransaction(
        "Placing an order",
        /* readOnly= */ false,
        // This service's own work, on its own branch.
        branch -> {
          String orderId = UUID.randomUUID().toString();
          Order.insert(branch, orderId, request.getCustomerId(), System.currentTimeMillis());

          int amount = 0;
          for (ItemOrder itemOrder : request.getItemOrderList()) {
            Statement.insert(branch, orderId, itemOrder.getItemId(), itemOrder.getCount());

            Optional<Item> item = Item.get(branch, itemOrder.getItemId());
            if (!item.isPresent()) {
              throw Status.NOT_FOUND.withDescription("Item not found").asRuntimeException();
            }
            amount += item.get().price * itemOrder.getCount();
          }
          return new PlacedOrder(orderId, amount);
        },
        // After the branch has ended: bring Customer Service into the same transaction.
        (placedOrder, transactionId) -> {
          callPaymentEndpoint(transactionId, request.getCustomerId(), placedOrder.amount);
          return PlaceOrderResponse.newBuilder().setOrderId(placedOrder.orderId).build();
        },
        responseObserver);
  }

  /** Retrieves order information by order ID. */
  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
    execTransaction(
        "Getting an order",
        // This transaction only reads, so it is begun read-only. The implementation may optimize
        // for that.
        /* readOnly= */ true,
        branch -> {
          Optional<Order> order = Order.getById(branch, request.getOrderId());
          if (!order.isPresent()) {
            throw Status.NOT_FOUND.withDescription("Order not found").asRuntimeException();
          }
          return buildOrder(branch, order.get());
        },
        (order, transactionId) ->
            GetOrderResponse.newBuilder()
                .setOrder(withCustomerName(order, transactionId))
                .build(),
        responseObserver);
  }

  /** Retrieves order information by customer ID. */
  @Override
  public void getOrders(
      GetOrdersRequest request, StreamObserver<GetOrdersResponse> responseObserver) {
    execTransaction(
        "Getting orders",
        /* readOnly= */ true,
        branch -> {
          List<sample.rpc.Order.Builder> orders = new ArrayList<>();
          for (Order order : Order.getByCustomerId(branch, request.getCustomerId())) {
            orders.add(buildOrder(branch, order));
          }
          return orders;
        },
        (orders, transactionId) -> {
          GetOrdersResponse.Builder builder = GetOrdersResponse.newBuilder();
          for (sample.rpc.Order.Builder order : orders) {
            builder.addOrder(withCustomerName(order, transactionId));
          }
          return builder.build();
        },
        responseObserver);
  }

  /**
   * Runs a microservice transaction: begin, do this service's own work on a branch, end that
   * branch, let the other services do their work in the same transaction, then commit.
   *
   * <p>Note the two phases. All of this service's CRUD happens before the branch is ended, so the
   * branch is released before the remote call. Keeping the branch open across the remote call would
   * also be correct — the contract only requires that every branch is ended exactly once before the
   * transaction is committed or rolled back — but ending it as soon as this service is done is the
   * idiom the API is designed around.
   */
  private <I, T> void execTransaction(
      String funcName,
      boolean readOnly,
      BranchOperations<I> branchOperations,
      AfterBranchOperations<I, T> afterBranchOperations,
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
        // Begin the microservice transaction. `global` drives the outcome; it holds no data.
        global = readOnly ? manager.beginReadOnly() : manager.begin();

        // Join it with this service's own branch, and do this service's work on it.
        branch = manager.beginBranch(global.getId());
        I intermediate = branchOperations.apply(branch);

        // Declare that this branch's work succeeded. Every branch must be ended exactly once,
        // whatever its outcome.
        branch.end(BranchTransaction.Status.SUCCESS);
        branch = null;

        // Hand the transaction ID to the other services. They join the same transaction with their
        // own branches and end them; they do not commit or roll back.
        T result = afterBranchOperations.apply(intermediate, global.getId());

        // Commit the transaction as a whole. The Transaction Coordinator (Separated Clusters) or
        // the cluster itself (Shared Cluster) drives two-phase commit on our behalf, so this
        // service never implements prepare/validate/commit.
        global.commit();

        responseObserver.onNext(result);
        responseObserver.onCompleted();
        return;
      } catch (UnknownTransactionStatusException e) {
        // The transaction's outcome is unknown: it may or may not have been committed. Retrying
        // could duplicate a committed transaction, so do not retry. Determining the actual status
        // is the application's responsibility.
        endBranchWithFailure(branch);
        respondWithError(funcName, e, responseObserver);
        return;
      } catch (StatusRuntimeException e) {
        endBranchWithFailure(branch);
        rollback(global);

        if (e.getStatus().getCode() == Status.Code.NOT_FOUND
            || e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
          // Deterministic business failures, such as an unknown item or an exceeded credit limit.
          // Retrying them would fail the same way.
          responseObserver.onError(e);
          return;
        }
        lastException = e;
      } catch (Exception e) {
        endBranchWithFailure(branch);
        rollback(global);
        // The cause may be a transient conflict, which is worth retrying, or something
        // nontransient, which is not. Since we cannot always tell them apart, the number of
        // attempts is bounded.
        lastException = e;
      }
    }
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
    // The other services' branches are rolled back with the transaction. They do not expose a
    // rollback endpoint, which is the point of this API.
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

  private sample.rpc.Order.Builder buildOrder(BranchTransaction branch, Order order)
      throws Exception {
    sample.rpc.Order.Builder orderBuilder =
        sample.rpc.Order.newBuilder()
            .setOrderId(order.id)
            .setCustomerId(order.customerId)
            .setTimestamp(order.timestamp);

    int total = 0;
    for (Statement statement : Statement.getByOrderId(branch, order.id)) {
      Optional<Item> item = Item.get(branch, statement.itemId);
      if (!item.isPresent()) {
        throw Status.NOT_FOUND.withDescription("Item not found").asRuntimeException();
      }
      int itemTotal = item.get().price * statement.count;
      orderBuilder.addStatement(
          sample.rpc.Statement.newBuilder()
              .setItemId(statement.itemId)
              .setItemName(item.get().name)
              .setPrice(item.get().price)
              .setCount(statement.count)
              .setTotal(itemTotal));
      total += itemTotal;
    }
    return orderBuilder.setTotal(total);
  }

  private sample.rpc.Order withCustomerName(
      sample.rpc.Order.Builder orderBuilder, String transactionId) {
    GetCustomerInfoResponse customerInfo =
        customerServiceStub.getCustomerInfo(
            GetCustomerInfoRequest.newBuilder()
                .setTransactionId(transactionId)
                .setCustomerId(orderBuilder.getCustomerId())
                .build());
    return orderBuilder.setCustomerName(customerInfo.getName()).build();
  }

  private void callPaymentEndpoint(String transactionId, int customerId, int amount) {
    customerServiceStub.payment(
        PaymentRequest.newBuilder()
            .setTransactionId(transactionId)
            .setCustomerId(customerId)
            .setAmount(amount)
            .build());
  }

  /** An order this service has written, and the amount Customer Service must charge for it. */
  private static class PlacedOrder {
    final String orderId;
    final int amount;

    PlacedOrder(String orderId, int amount) {
      this.orderId = orderId;
      this.amount = amount;
    }
  }

  @Override
  public void close() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.warn("Failed to shut down the channel", e);
    }
    manager.close();
  }
}
