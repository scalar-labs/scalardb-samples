package sample.order.model;

import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.api.Scan.Ordering;
import com.scalar.db.api.TransactionCrudOperable;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Order {

  private static final String NAMESPACE = "order_service";
  private static final String TABLE = "orders";
  private static final String COL_ORDER_ID = "order_id";
  private static final String COL_CUSTOMER_ID = "customer_id";
  private static final String COL_TIMESTAMP = "timestamp";

  public final String id;
  public final int customerId;
  public final long timestamp;

  public Order(String id, int customerId, long timestamp) {
    this.id = id;
    this.customerId = customerId;
    this.timestamp = timestamp;
  }

  public static void put(
      TransactionCrudOperable transaction, String id, int customerId, long timestamp)
      throws CrudException {
    transaction.put(
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLE)
            .partitionKey(Key.ofInt(COL_CUSTOMER_ID, customerId))
            .clusteringKey(Key.ofBigInt(COL_TIMESTAMP, timestamp))
            .textValue(COL_ORDER_ID, id)
            .build());
  }

  public static Optional<Order> getById(TransactionCrudOperable transaction, String id)
      throws CrudException {
    return transaction
        .get(
            Get.newBuilder()
                .namespace(NAMESPACE)
                .table(TABLE)
                .indexKey(Key.ofText(COL_ORDER_ID, id))
                .build())
        .map(Order::resultToOrder);
  }

  public static List<Order> getByCustomerId(TransactionCrudOperable transaction, int customerId)
      throws CrudException {
    return transaction
        .scan(
            Scan.newBuilder()
                .namespace(NAMESPACE)
                .table(TABLE)
                .partitionKey(Key.ofInt(COL_CUSTOMER_ID, customerId))
                .ordering(Ordering.desc(COL_TIMESTAMP))
                .build())
        .stream()
        .map(Order::resultToOrder)
        .collect(Collectors.toList());
  }

  private static Order resultToOrder(Result result) {
    return new Order(
        result.getText(COL_ORDER_ID),
        result.getInt(COL_CUSTOMER_ID),
        result.getBigInt(COL_TIMESTAMP));
  }
}
