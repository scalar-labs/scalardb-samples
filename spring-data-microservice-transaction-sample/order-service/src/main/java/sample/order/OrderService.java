package sample.order;

import com.scalar.db.sql.springdata.exception.ScalarDbNonTransientException;
import com.scalar.db.sql.springdata.twopc.RemotePrepareCommitPhaseOperations;
import com.scalar.db.sql.springdata.twopc.TwoPcResult;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import sample.order.domain.model.Item;
import sample.order.domain.model.Order;
import sample.order.domain.model.Statement;
import sample.order.domain.repository.ItemRepository;
import sample.order.domain.repository.OrderRepository;
import sample.order.domain.repository.StatementRepository;
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

@Service
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase implements Closeable {

  private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

  // For gRPC connection to Customer service
  private final ManagedChannel channel;
  private final CustomerServiceGrpc.CustomerServiceBlockingStub stub;

  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private StatementRepository statementRepository;

  public OrderService() {
    // Initialize the gRPC connection to Customer service
    channel = NettyChannelBuilder.forAddress("customer-service", 10010).usePlaintext().build();
    stub = CustomerServiceGrpc.newBlockingStub(channel);
  }

  public void init() {
    // `itemRepository` is set up after the constructor of CustomerService is performed by
    // Spring Framework. So, loading initial data should be executed outside the constructor.
    loadInitialData();
  }

  private void loadInitialData() {
    orderRepository.executeOneshotOperations(() -> {
      itemRepository.insertIfNotExists(new Item(1, "Apple", 1000));
      itemRepository.insertIfNotExists(new Item(2, "Orange", 2000));
      itemRepository.insertIfNotExists(new Item(3, "Grape", 2500));
      itemRepository.insertIfNotExists(new Item(4, "Mango", 5000));
      itemRepository.insertIfNotExists(new Item(5, "Melon", 3000));
      return null;
    });
  }

  /**
   * Place an order. It's a transaction that spans OrderService and CustomerService
   */
  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Override
  public void placeOrder(
      PlaceOrderRequest request, StreamObserver<PlaceOrderResponse> responseObserver) {

    execAndReturnResponse("Placing an order", () -> {
      // Start a two-phase commit interface transaction
      TwoPcResult<PlaceOrderResponse> result = orderRepository.executeTwoPcTransaction(txId -> {
            String orderId = UUID.randomUUID().toString();
            Order order = new Order(orderId, request.getCustomerId(), System.currentTimeMillis());

            // Put the order info into the orders table
            orderRepository.insert(order);

            AtomicInteger amount = new AtomicInteger();
            for (ItemOrder itemOrder : request.getItemOrderList()) {
              int itemId = itemOrder.getItemId();
              int count = itemOrder.getCount();
              // Retrieve the item info from the items table
              Optional<Item> itemOpt = itemRepository.findById(itemId);
              if (!itemOpt.isPresent()) {
                String message = "Item not found: " + itemId;
                responseObserver.onError(
                    Status.NOT_FOUND.withDescription(message).asRuntimeException());
                throw new ScalarDbNonTransientException(message);
              }
              Item item = itemOpt.get();

              int cost = item.price * count;
              // Put the order statement into the statements table
              statementRepository.insert(new Statement(itemId, orderId, count));
              // Calculate the total amount
              amount.addAndGet(cost);
            }

            // Call the payment endpoint of Customer service
            callPaymentEndpoint(txId, request.getCustomerId(), amount.get());

            return PlaceOrderResponse.newBuilder().setOrderId(orderId).build();
          },
          Collections.singletonList(
              RemotePrepareCommitPhaseOperations.createSerializable(
                  this::callPrepareEndpoint,
                  this::callValidateEndpoint,
                  this::callCommitEndpoint,
                  this::callRollbackEndpoint
              )
          )
      );

      return result.executionPhaseReturnValue();
    }, responseObserver);
  }

  private void callPaymentEndpoint(String transactionId, int customerId, int amount) {
    stub.payment(
        PaymentRequest.newBuilder()
            .setTransactionId(transactionId)
            .setCustomerId(customerId)
            .setAmount(amount)
            .build());
  }

  private void callPrepareEndpoint(String transactionId) {
    stub.prepare(PrepareRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void callValidateEndpoint(String transactionId) {
    stub.validate(ValidateRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void callCommitEndpoint(String transactionId) {
    stub.commit(CommitRequest.newBuilder().setTransactionId(transactionId).build());
  }

  private void callRollbackEndpoint(String transactionId) {
    stub.rollback(RollbackRequest.newBuilder().setTransactionId(transactionId).build());
  }

  /**
   * Get Order information by order ID
   */
  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
    execAndReturnResponse("Getting an order", () -> {
      // Start a two-phase commit interface transaction
      TwoPcResult<GetOrderResponse> result = orderRepository.executeTwoPcTransaction(txId -> {
            // Retrieve the order info for the specified order ID
            Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
            if (!orderOpt.isPresent()) {
              String message = "Order not found: " + request.getOrderId();
              responseObserver.onError(
                  Status.NOT_FOUND.withDescription(message).asRuntimeException());
              throw new ScalarDbNonTransientException(message);
            }

            // Get the customer name from the Customer service
            String customerName = getCustomerName(txId, orderOpt.get().customerId);

            // Make an order protobuf to return
            sample.rpc.Order order = getOrderResult(txId, responseObserver, orderOpt.get(), customerName);
            return GetOrderResponse.newBuilder().setOrder(order).build();
          },
          Collections.singletonList(
              RemotePrepareCommitPhaseOperations.createSerializable(
                  this::callPrepareEndpoint,
                  this::callValidateEndpoint,
                  this::callCommitEndpoint,
                  this::callRollbackEndpoint
              )
          ));

      return result.executionPhaseReturnValue();
    }, responseObserver);
  }

  /**
   * Get Order information by customer ID
   */
  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Override
  public void getOrders(
      GetOrdersRequest request, StreamObserver<GetOrdersResponse> responseObserver) {
    execAndReturnResponse("Getting orders", () -> {
      // Start a two-phase commit interface transaction
      TwoPcResult<GetOrdersResponse> result = orderRepository.executeTwoPcTransaction(txId -> {
            // Get the customer name from the Customer service
            String customerName = getCustomerName(txId, request.getCustomerId());

            // Retrieve the order info for the specified order ID
            GetOrdersResponse.Builder builder = GetOrdersResponse.newBuilder();
            for (Order order : orderRepository.findAllByCustomerIdOrderByTimestampDesc(
                request.getCustomerId())) {
              // Make an order protobuf to return
              builder.addOrder(getOrderResult(txId, responseObserver, order, customerName));
            }
            return builder.build();
          },
          Collections.singletonList(
              RemotePrepareCommitPhaseOperations.createSerializable(
                  this::callPrepareEndpoint,
                  this::callValidateEndpoint,
                  this::callCommitEndpoint,
                  this::callRollbackEndpoint
              )
          ));

      return result.executionPhaseReturnValue();
    }, responseObserver);
}

  private sample.rpc.Order getOrderResult(String transactionId, StreamObserver<?> responseObserver,
      Order order, String customerName) {
    sample.rpc.Order.Builder orderBuilder =
        sample.rpc.Order.newBuilder()
            .setOrderId(order.orderId)
            .setCustomerId(order.customerId)
            .setCustomerName(customerName)
            .setTimestamp(order.timestamp);

    int total = 0;

    // Retrieve the order statements for the order ID from the statements table
    List<Statement> statements = statementRepository.findAllByOrderId(order.orderId);

    // Make statements
    for (Statement statement : statements) {
      sample.rpc.Statement.Builder statementBuilder = sample.rpc.Statement.newBuilder();
      statementBuilder.setItemId(statement.itemId);

      // Retrieve the item data from the items table
      Optional<Item> itemOpt = itemRepository.findById(statement.itemId);
      if (!itemOpt.isPresent()) {
        String message = "Item not found: " + statement.itemId;
        responseObserver.onError(
            Status.NOT_FOUND.withDescription(message).asRuntimeException());
        throw new ScalarDbNonTransientException(message);
      }
      Item item = itemOpt.get();
      statementBuilder.setItemName(item.name);
      statementBuilder.setPrice(item.price);
      statementBuilder.setCount(statement.count);

      int itemTotal = item.price * statement.count;
      statementBuilder.setTotal(itemTotal);

      orderBuilder.addStatement(statementBuilder);

      total += itemTotal;
    }

    return orderBuilder.setTotal(total).build();
  }

  private String getCustomerName(String transactionId, int customerId) {
    GetCustomerInfoResponse customerInfo =
        stub.getCustomerInfo(
            GetCustomerInfoRequest.newBuilder()
                .setTransactionId(transactionId)
                .setCustomerId(customerId).build());
    return customerInfo.getName();
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
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Failed to shutdown the channel", e);
    }
  }
}
