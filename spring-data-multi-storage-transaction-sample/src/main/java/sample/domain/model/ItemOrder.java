package sample.domain.model;

public class ItemOrder {
  public final int itemId;
  public final int count;

  public ItemOrder(int itemId, int count) {
    this.itemId = itemId;
    this.count = count;
  }
}
