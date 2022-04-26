package sample.order.model;

import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.TransactionCrudOperable;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.Optional;

public class Item {

  private static final String NAMESPACE = "order_service";
  private static final String TABLE = "items";
  private static final String COL_ITEM_ID = "item_id";
  private static final String COL_NAME = "name";
  private static final String COL_PRICE = "price";

  public final int id;
  public final String name;
  public final int price;

  public Item(int id, String name, int price) {
    this.id = id;
    this.name = name;
    this.price = price;
  }

  public static void put(TransactionCrudOperable transaction, int id, String name, int price)
      throws CrudException {
    transaction.put(
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLE)
            .partitionKey(Key.ofInt(COL_ITEM_ID, id))
            .textValue(COL_NAME, name)
            .intValue(COL_PRICE, price)
            .build());
  }

  public static Optional<Item> get(TransactionCrudOperable transaction, int id)
      throws CrudException {
    return transaction
        .get(
            Get.newBuilder()
                .namespace(NAMESPACE)
                .table(TABLE)
                .partitionKey(Key.ofInt(COL_ITEM_ID, id))
                .build())
        .map(r -> new Item(r.getInt(COL_ITEM_ID), r.getText(COL_NAME), r.getInt(COL_PRICE)));
  }
}
