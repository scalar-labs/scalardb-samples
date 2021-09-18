package example.order.model;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Put;
import com.scalar.db.api.Scan;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.List;
import java.util.stream.Collectors;

public class Statement {

  private static final String NAMESPACE = "order_service";
  private static final String TABLE = "statements";
  private static final String COL_ORDER_ID = "order_id";
  private static final String COL_ITEM_ID = "item_id";
  private static final String COL_COUNT = "count";

  public final String orderId;
  public final int itemId;
  public final int count;

  public Statement(String orderId, int itemId, int count) {
    this.orderId = orderId;
    this.itemId = itemId;
    this.count = count;
  }

  public static void put(
      TwoPhaseCommitTransaction transaction, String orderId, int itemId, int count)
      throws CrudException {
    transaction.put(
        new Put(new Key(COL_ORDER_ID, orderId), new Key(COL_ITEM_ID, itemId))
            .withValue(COL_COUNT, count)
            .forNamespace(NAMESPACE)
            .forTable(TABLE));
  }

  public static List<Statement> getByOrderId(DistributedTransaction transaction, String orderId)
      throws CrudException {
    Scan scan = new Scan(new Key(COL_ORDER_ID, orderId)).forNamespace(NAMESPACE).forTable(TABLE);
    return transaction.scan(scan).stream()
        .map(
            r ->
                new Statement(
                    r.getValue(COL_ORDER_ID).get().getAsString().get(),
                    r.getValue(COL_ITEM_ID).get().getAsInt(),
                    r.getValue(COL_COUNT).get().getAsInt()))
        .collect(Collectors.toList());
  }
}
