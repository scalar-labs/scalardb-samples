package example.customer.model;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.TwoPhaseCommitTransaction;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;
import java.util.Optional;

public class Customer {
  private static final String NAMESPACE = "customer_service";
  private static final String TABLE = "customers";
  private static final String COL_ID = "id";
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
      DistributedTransaction transaction, int id, String name, int creditLimit, int creditTotal)
      throws CrudException {
    transaction.put(
        new Put(new Key(COL_ID, id))
            .withValue(COL_NAME, name)
            .withValue(COL_CREDIT_LIMIT, creditLimit)
            .withValue(COL_CREDIT_TOTAL, creditTotal)
            .forNamespace(NAMESPACE)
            .forTable(TABLE));
  }

  public static void updateCreditTotal(
      TwoPhaseCommitTransaction transaction, int id, int creditTotal) throws CrudException {
    transaction.put(
        new Put(new Key(COL_ID, id))
            .withValue(COL_CREDIT_TOTAL, creditTotal)
            .forNamespace(NAMESPACE)
            .forTable(TABLE));
  }

  public static void updateCreditTotal(DistributedTransaction transaction, int id, int creditTotal)
      throws CrudException {
    transaction.put(
        new Put(new Key(COL_ID, id))
            .withValue(COL_CREDIT_TOTAL, creditTotal)
            .forNamespace(NAMESPACE)
            .forTable(TABLE));
  }

  public static Optional<Customer> get(DistributedTransaction transaction, int id)
      throws CrudException {
    Get get = new Get(new Key(COL_ID, id)).forNamespace(NAMESPACE).forTable(TABLE);
    Optional<Result> result = transaction.get(get);
    return getInternal(result);
  }

  public static Optional<Customer> get(TwoPhaseCommitTransaction transaction, int id)
      throws CrudException {
    Get get = new Get(new Key(COL_ID, id)).forNamespace(NAMESPACE).forTable(TABLE);
    Optional<Result> result = transaction.get(get);
    return getInternal(result);
  }

  private static Optional<Customer> getInternal(Optional<Result> result) {
    return result.map(
        r ->
            new Customer(
                r.getValue(COL_ID).get().getAsInt(),
                r.getValue(COL_NAME).get().getAsString().get(),
                r.getValue(COL_CREDIT_LIMIT).get().getAsInt(),
                r.getValue(COL_CREDIT_TOTAL).get().getAsInt()));
  }
}
