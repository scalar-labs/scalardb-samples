package example.order.model;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.Optional;

public class Item {

  private static final String NAMESPACE = "order_service";
  private static final String TABLE = "items";
  private static final String COL_ID = "id";
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

  public static void put(DistributedTransaction transaction, int id, String name, int price)
      throws CrudException {
    transaction.put(
        new Put(new Key(COL_ID, id))
            .withValue(COL_NAME, name)
            .withValue(COL_PRICE, price)
            .forNamespace(NAMESPACE)
            .forTable(TABLE));
  }

  public static Optional<Item> get(DistributedTransaction transaction, int id)
      throws CrudException {
    Get get = new Get(new Key(COL_ID, id)).forNamespace(NAMESPACE).forTable(TABLE);
    Optional<Result> result = transaction.get(get);
    return getInternal(result);
  }

  public static Optional<Item> get(TwoPhaseCommitTransaction transaction, int id)
      throws CrudException {
    Get get = new Get(new Key(COL_ID, id)).forNamespace(NAMESPACE).forTable(TABLE);
    Optional<Result> result = transaction.get(get);
    return getInternal(result);
  }

  private static Optional<Item> getInternal(Optional<Result> result) {
    return result.map(
        r ->
            new Item(
                r.getValue(COL_ID).get().getAsInt(),
                r.getValue(COL_NAME).get().getAsString().get(),
                r.getValue(COL_PRICE).get().getAsInt()));
  }
}
