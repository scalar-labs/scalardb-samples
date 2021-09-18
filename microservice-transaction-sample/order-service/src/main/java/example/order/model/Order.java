package example.order.model;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.api.Scan.Ordering;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Order {

  private static final String NAMESPACE = "order_service";
  private static final String TABLE = "orders";
  private static final String COL_ID = "id";
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
      TwoPhaseCommitTransaction transaction, String id, int customerId, long timestamp)
      throws CrudException {
    transaction.put(
        new Put(new Key(COL_CUSTOMER_ID, customerId), new Key(COL_TIMESTAMP, timestamp))
            .withValue(COL_ID, id)
            .forNamespace(NAMESPACE)
            .forTable(TABLE));
  }

  public static Optional<Order> getById(DistributedTransaction transaction, String id)
      throws CrudException {
    Get get = new Get(new Key(COL_ID, id)).forNamespace(NAMESPACE).forTable(TABLE);
    Optional<Result> result = transaction.get(get);
    return result.map(Order::resultToOrder);
  }

  public static List<Order> getByCustomerId(DistributedTransaction transaction, int customerId)
      throws CrudException {
    Scan scan =
        new Scan(new Key(COL_CUSTOMER_ID, customerId))
            .withOrdering(new Ordering(COL_TIMESTAMP, Ordering.Order.DESC))
            .forNamespace(NAMESPACE)
            .forTable(TABLE);
    return transaction.scan(scan).stream().map(Order::resultToOrder).collect(Collectors.toList());
  }

  private static Order resultToOrder(Result result) {
    return new Order(
        result.getValue(COL_ID).get().getAsString().get(),
        result.getValue(COL_CUSTOMER_ID).get().getAsInt(),
        result.getValue(COL_TIMESTAMP).get().getAsLong());
  }
}
