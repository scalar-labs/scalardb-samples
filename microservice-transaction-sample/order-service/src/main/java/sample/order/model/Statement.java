package sample.order.model;

import com.scalar.db.api.Put;
import com.scalar.db.api.Scan;
import com.scalar.db.api.TransactionCrudOperable;
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

  public static void put(TransactionCrudOperable transaction, String orderId, int itemId, int count)
      throws CrudException {
    transaction.put(
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLE)
            .partitionKey(Key.ofText(COL_ORDER_ID, orderId))
            .clusteringKey(Key.ofInt(COL_ITEM_ID, itemId))
            .intValue(COL_COUNT, count)
            .build());
  }

  public static List<Statement> getByOrderId(TransactionCrudOperable transaction, String orderId)
      throws CrudException {
    return transaction
        .scan(
            Scan.newBuilder()
                .namespace(NAMESPACE)
                .table(TABLE)
                .partitionKey(Key.ofText(COL_ORDER_ID, orderId))
                .build())
        .stream()
        .map(
            r -> new Statement(r.getText(COL_ORDER_ID), r.getInt(COL_ITEM_ID), r.getInt(COL_COUNT)))
        .collect(Collectors.toList());
  }
}
