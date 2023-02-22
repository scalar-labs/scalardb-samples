package sample.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("items")
public class Item {
  @Id
  @JsonProperty("item_id")
  public final int itemId;

  @JsonProperty("name")
  public final String name;

  @JsonProperty("price")
  public final int price;

  public Item(int itemId, String name, int price) {
    this.itemId = itemId;
    this.name = name;
    this.price = price;
  }
}
