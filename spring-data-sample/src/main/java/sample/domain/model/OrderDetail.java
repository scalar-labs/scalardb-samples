package sample.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OrderDetail {
  @JsonProperty("order_id")
  public final String orderId;

  @JsonProperty("timestamp")
  public final long timestamp;

  @JsonProperty("customer_id")
  public final int customerId;

  @JsonProperty("customer_name")
  public final String customerName;

  @JsonProperty("statements")
  public final List<StatementDetail> statements;

  @JsonProperty("total")
  public final int total;

  public OrderDetail(
      String orderId,
      int customerId,
      String customerName,
      long timestamp,
      List<StatementDetail> statements,
      int total) {
    this.orderId = orderId;
    this.customerId = customerId;
    this.customerName = customerName;
    this.timestamp = timestamp;
    this.statements = statements;
    this.total = total;
  }
}
