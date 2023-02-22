package sample.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("statements")
public class Statement {
  // This model is actually accessed via a multi-column index, but Spring Data doesn't support it
  // while @Id is always required. So, this @Id annotation is a dummy
  @Id
  @JsonProperty("item_id")
  public final int itemId;

  @JsonProperty("order_id")
  public final String orderId;

  @JsonProperty("count")
  public final int count;

  public Statement(int itemId, String orderId, int count) {
    this.itemId = itemId;
    this.orderId = orderId;
    this.count = count;
  }
}
