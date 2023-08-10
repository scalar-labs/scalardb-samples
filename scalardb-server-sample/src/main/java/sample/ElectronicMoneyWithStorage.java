package sample;

import com.scalar.db.api.DistributedStorage;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.io.Key;
import com.scalar.db.service.StorageFactory;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ElectronicMoneyWithStorage {

  private static final String SCALARDB_PROPERTIES =
      System.getProperty("user.dir") + File.separator + "scalardb-client.properties";
  private static final String NAMESPACE = "emoney";
  private static final String TABLENAME = "account";
  private static final String ID = "id";
  private static final String BALANCE = "balance";

  private final DistributedStorage storage;

  public ElectronicMoneyWithStorage() throws IOException {
    StorageFactory factory = StorageFactory.create(SCALARDB_PROPERTIES);
    storage = factory.getStorage();
  }

  public void charge(String id, int amount) throws ExecutionException {
    // Retrieve the current balance for id
    Get get =
        Get.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, id))
            .build();
    Optional<Result> result = storage.get(get);

    // Calculate the balance
    int balance = amount;
    if (result.isPresent()) {
      int current = result.get().getInt(BALANCE);
      balance += current;
    }

    // Update the balance
    Put put =
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, id))
            .intValue(BALANCE, balance)
            .build();
    storage.put(put);
  }

  public void pay(String fromId, String toId, int amount) throws ExecutionException {
    // Retrieve the current balances for ids
    Get fromGet =
        Get.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, fromId))
            .build();
    Get toGet =
        Get.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, toId))
            .build();
    Optional<Result> fromResult = storage.get(fromGet);
    Optional<Result> toResult = storage.get(toGet);

    // Calculate the balances (it assumes that both accounts exist)
    int newFromBalance = fromResult.get().getInt(BALANCE) - amount;
    int newToBalance = toResult.get().getInt(BALANCE) + amount;
    if (newFromBalance < 0) {
      throw new RuntimeException(fromId + " doesn't have enough balance");
    }

    // Update the balances
    Put fromPut =
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, fromId))
            .intValue(BALANCE, newFromBalance)
            .build();
    Put toPut =
        Put.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, toId))
            .intValue(BALANCE, newToBalance)
            .build();
    storage.put(fromPut);
    storage.put(toPut);
  }

  public int getBalance(String id) throws ExecutionException {
    // Retrieve the current balances for id
    Get get =
        Get.newBuilder()
            .namespace(NAMESPACE)
            .table(TABLENAME)
            .partitionKey(Key.ofText(ID, id))
            .build();
    Optional<Result> result = storage.get(get);

    int balance = -1;
    if (result.isPresent()) {
      balance = result.get().getInt(BALANCE);
    }
    return balance;
  }

  public void close() {
    storage.close();
  }

  public static void main(String[] args) throws Exception {
    String action = null;
    int amount = 0;
    String to = null;
    String from = null;
    String id = null;

    for (int i = 0; i < args.length; ++i) {
      if ("-action".equals(args[i])) {
        action = args[++i];
      } else if ("-amount".equals(args[i])) {
        amount = Integer.parseInt(args[++i]);
      } else if ("-to".equals(args[i])) {
        to = args[++i];
      } else if ("-from".equals(args[i])) {
        from = args[++i];
      } else if ("-id".equals(args[i])) {
        id = args[++i];
      } else if ("-help".equals(args[i])) {
        printUsageAndExit();
        return;
      }
    }

    if (action == null) {
      printUsageAndExit();
      return;
    }

    ElectronicMoneyWithStorage eMoney = new ElectronicMoneyWithStorage();

    if (action.equalsIgnoreCase("charge")) {
      if (to == null || amount < 0) {
        printUsageAndExit();
        return;
      }
      eMoney.charge(to, amount);
    } else if (action.equalsIgnoreCase("pay")) {
      if (to == null || amount < 0 || from == null) {
        printUsageAndExit();
        return;
      }
      eMoney.pay(from, to, amount);
    } else if (action.equalsIgnoreCase("getBalance")) {
      if (id == null) {
        printUsageAndExit();
        return;
      }
      int balance = eMoney.getBalance(id);
      System.out.println("The balance for " + id + " is " + balance);
    }
    eMoney.close();
  }

  private static void printUsageAndExit() {
    System.err.println(
        "ElectronicMoneyMain -action charge/pay/getBalance [-amount number (needed for charge and pay)] [-to id (needed for charge and pay)] [-from id (needed for pay)] [-id id (needed for getBalance)]");
    System.exit(1);
  }
}
