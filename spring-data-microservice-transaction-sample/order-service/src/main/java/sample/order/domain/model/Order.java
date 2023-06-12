package sample.order.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
public class Order {

  @Id
  public final String orderId;
  public final int customerId;
  public final long timestamp;

  public Order(String orderId, int customerId, long timestamp) {
    this.orderId = orderId;
    this.customerId = customerId;
    this.timestamp = timestamp;
  }
}
