package sample.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatementDetail {
  @JsonProperty("item_id")
  public final int itemId;

  @JsonProperty("item_name")
  public final String itemName;

  @JsonProperty("price")
  public final int price;

  @JsonProperty("count")
  public final int count;

  @JsonProperty("total")
  public final int total;

  public StatementDetail(int itemId, String itemName, int price, int count, int total) {
    this.itemId = itemId;
    this.itemName = itemName;
    this.price = price;
    this.count = count;
    this.total = total;
  }
}
