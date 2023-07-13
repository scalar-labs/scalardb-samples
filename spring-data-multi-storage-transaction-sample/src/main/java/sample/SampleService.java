package sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.db.sql.springdata.EnableScalarDbRepositories;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.domain.model.Customer;
import sample.domain.model.Item;
import sample.domain.model.ItemOrder;
import sample.domain.model.Order;
import sample.domain.model.OrderDetail;
import sample.domain.model.Statement;
import sample.domain.model.StatementDetail;
import sample.domain.repository.CustomerRepository;
import sample.domain.repository.ItemRepository;
import sample.domain.repository.OrderRepository;
import sample.domain.repository.StatementRepository;

@EnableScalarDbRepositories
@Service
public class SampleService {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private CustomerRepository customerRepository;
  @Autowired private ItemRepository itemRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private StatementRepository statementRepository;

  @Transactional
  public void loadInitialData() {
    customerRepository.insertIfNotExists(new Customer(1, "Yamada Taro", 10000, 0));
    customerRepository.insertIfNotExists(new Customer(2, "Yamada Hanako", 10000, 0));
    customerRepository.insertIfNotExists(new Customer(3, "Suzuki Ichiro", 10000, 0));
    itemRepository.insertIfNotExists(new Item(1, "Apple", 1000));
    itemRepository.insertIfNotExists(new Item(2, "Orange", 2000));
    itemRepository.insertIfNotExists(new Item(3, "Grape", 2500));
    itemRepository.insertIfNotExists(new Item(4, "Mango", 5000));
    itemRepository.insertIfNotExists(new Item(5, "Melon", 3000));
  }

  private String asJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Transactional
  public String getCustomerInfo(int customerId) {
    try {
      // Retrieve the customer info for the specified customer ID from the customers table.
      // Return the customer info as a JSON format.
      return objectMapper.writeValueAsString(customerRepository.getById(customerId));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Transactional
  public String placeOrder(int customerId, List<ItemOrder> itemOrders) {
    String orderId = UUID.randomUUID().toString();
    Order order = new Order(orderId, customerId, System.currentTimeMillis());
    // Put the order info into the orders table
    orderRepository.insert(order);

    AtomicInteger amount = new AtomicInteger();
    itemOrders.forEach(
        itemOrder -> {
          int itemId = itemOrder.itemId;
          int count = itemOrder.count;
          // Retrieve the item info from the items table
          Item item = itemRepository.getById(itemId);
          int cost = item.price * count;
          // Put the order statement into the statements table
          statementRepository.insert(new Statement(itemId, orderId, count));
          // Calculate the total amount
          amount.addAndGet(cost);
        });

    Customer customer = customerRepository.getById(customerId);
    int creditLimit = customer.creditLimit;
    int creditTotal = customer.creditTotal;
    int updatedCreditTotal = creditTotal + amount.get();
    // Check if the credit total exceeds the credit limit after payment
    if (updatedCreditTotal > creditLimit) {
      throw new RuntimeException(
          String.format(
              "Credit limit exceeded. limit:%d, total:%d", creditLimit, updatedCreditTotal));
    }
    // Update credit_total for the customer
    customerRepository.update(customer.withCreditTotal(updatedCreditTotal));

    return asJson(order);
  }

  private OrderDetail getOrderDetail(String orderId) {
    // Retrieve the order info for the order ID from the orders table
    Order order = orderRepository.getById(orderId);
    int customerId = order.customerId;
    // Retrieve the customer info for the specified customer ID from the customers table
    Customer customer = customerRepository.getById(customerId);

    AtomicInteger total = new AtomicInteger();
    List<StatementDetail> statementDetails = new ArrayList<>();
    // Retrieve the order statements for the order ID from the statements table
    statementRepository
        .findAllByOrderId(orderId)
        .forEach(
            statement -> {
              // Retrieve the item data from the items table
              Item item = itemRepository.getById(statement.itemId);
              int cost = item.price * statement.count;
              statementDetails.add(
                  new StatementDetail(item.itemId, item.name, item.price, statement.count, cost));
              total.addAndGet(cost);
            });

    return new OrderDetail(
        orderId, customerId, customer.name, order.timestamp, statementDetails, total.get());
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Transactional
  public String getOrderByOrderId(String orderId) {
    // Get an order JSON for the specified order ID.
    // Return the order info as a JSON format.
    return asJson(getOrderDetail(orderId));
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Transactional
  public String getOrdersByCustomerId(int customerId) {
    // Retrieve the order info for the customer ID from the orders table.
    // Return the order info as a JSON format.
    return asJson(
        orderRepository.findAllByCustomerIdOrderByTimestampDesc(customerId).stream()
            .map(order -> getOrderDetail(order.orderId))
            .collect(Collectors.toList()));
  }

  @Retryable(
      include = TransientDataAccessException.class,
      maxAttempts = 8,
      backoff = @Backoff(delay = 1000, maxDelay = 8000, multiplier = 2))
  @Transactional
  public void repayment(int customerId, int amount) {
    Customer customer = customerRepository.getById(customerId);

    int updatedCreditTotal = customer.creditTotal - amount;

    // Check if over repayment or not
    if (updatedCreditTotal < 0) {
      throw new RuntimeException(
          String.format(
              "Over repayment. creditTotal:%d, payment:%d", customer.creditTotal, amount));
    }

    // Reduce credit_total for the customer
    customerRepository.update(customer.withCreditTotal(updatedCreditTotal));
  }
}
