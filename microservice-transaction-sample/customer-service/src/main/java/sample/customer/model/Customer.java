package sample.customer.model;

import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.TransactionCrudOperable;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.Optional;

public class Customer {
  private static final String NAMESPACE = "customer_service";
  private static final String TABLE = "customers";
  private static final String COL_CUSTOMER_ID = "customer_id";
  private static final String COL_NAME = "name";
  private static final String COL_CREDIT_LIMIT = "credit_limit";
  private static final String COL_CREDIT_TOTAL = "credit_total";

  public final int id;
  public final String name;
  public final int creditLimit;
  public final int creditTotal;

  public Customer(int id, String name, int creditLimit, int creditTotal) {
    this.id = id;
    this.name = name;
    this.creditLimit = creditLimit;
    this.creditTotal = creditTotal;
  }

  public static void put(
      TransactionCrudOperable transaction, int id, String name, int creditLimit, int creditTotal)
      throws CrudException {
    transaction.put(
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLE)
            .partitionKey(Key.ofInt(COL_CUSTOMER_ID, id))
            .textValue(COL_NAME, name)
            .intValue(COL_CREDIT_LIMIT, creditLimit)
            .intValue(COL_CREDIT_TOTAL, creditTotal)
            .build());
  }

  public static void updateCreditTotal(TransactionCrudOperable transaction, int id, int creditTotal)
      throws CrudException {
    transaction.put(
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLE)
            .partitionKey(Key.ofInt(COL_CUSTOMER_ID, id))
            .intValue(COL_CREDIT_TOTAL, creditTotal)
            .build());
  }

  public static Optional<Customer> get(TransactionCrudOperable transaction, int id)
      throws CrudException {
    return transaction
        .get(
            Get.newBuilder()
                .namespace(NAMESPACE)
                .table(TABLE)
                .partitionKey(Key.ofInt(COL_CUSTOMER_ID, id))
                .build())
        .map(
            r ->
                new Customer(
                    r.getInt(COL_CUSTOMER_ID),
                    r.getText(COL_NAME),
                    r.getInt(COL_CREDIT_LIMIT),
                    r.getInt(COL_CREDIT_TOTAL)));
  }
}
