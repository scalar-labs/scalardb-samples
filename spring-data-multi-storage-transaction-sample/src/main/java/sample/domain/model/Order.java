package sample.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "order", value = "orders")
public class Order {
  @Id
  @JsonProperty("order_id")
  public final String orderId;

  @JsonProperty("customer_id")
  public final int customerId;

  @JsonProperty("timestamp")
  public final long timestamp;

  public Order(String orderId, int customerId, long timestamp) {
    this.orderId = orderId;
    this.customerId = customerId;
    this.timestamp = timestamp;
  }
}
