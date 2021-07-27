package example.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.io.IntValue;
import com.scalar.db.io.Key;
import com.scalar.db.io.TextValue;
import com.scalar.db.service.StorageModule;
import com.scalar.db.service.StorageService;

import java.io.IOException;
import java.util.Optional;

public class ElectronicMoneyWithStorage extends ElectronicMoney {
  protected static final String NAMESPACE = "emoney_storage";
  private final StorageService service;

  public ElectronicMoneyWithStorage() throws IOException {
    Injector injector = Guice.createInjector(new StorageModule(dbConfig));
    service = injector.getInstance(StorageService.class);
  }

  @Override
  public void charge(String id, int amount) throws ExecutionException {
    // Retrieve the current balance for id
    Get get = new Get(new Key(new TextValue(ID, id))).forNamespace(NAMESPACE).forTable(TABLENAME);
    Optional<Result> result;
    try {
      result = service.get(get);
    } catch (ExecutionException e) {
      throw new ExecutionException("reading data from database failed");
    }

    // Calculate the balance
    int balance = amount;
    if (result.isPresent()) {
      int current = result.get().getValue(BALANCE).get().getAsInt();
      balance += current;
    }

    // Update the balance
    Put put = new Put(new Key(new TextValue(ID, id))).withValue(BALANCE, balance)
        .forNamespace(NAMESPACE).forTable(TABLENAME);
    try {
      service.put(put);
    } catch (ExecutionException e) {
      throw new ExecutionException("put data to database failed");
    }
  }

  @Override
  public void pay(String fromId, String toId, int amount) throws ExecutionException {
    // Retrieve the current balances for ids
    Get fromGet = new Get(new Key(new TextValue(ID, fromId))).forNamespace(NAMESPACE).forTable(TABLENAME);
    Get toGet = new Get(new Key(new TextValue(ID, toId))).forNamespace(NAMESPACE).forTable(TABLENAME);
    Optional<Result> fromResult;
    Optional<Result> toResult;
    fromResult = service.get(fromGet);
    toResult = service.get(toGet);

    // Calculate the balances (it assumes that both accounts exist)
    int newFromBalance = fromResult.get().getValue(BALANCE).get().getAsInt() - amount;
    int newToBalance = toResult.get().getValue(BALANCE).get().getAsInt() + amount;
    if (newFromBalance < 0) {
      throw new RuntimeException(fromId + " doesn't have enough balance.");
    }

    // Update the balances
    Put fromPut =
        new Put(new Key(new TextValue(ID, fromId)))
            .withValue(BALANCE, newFromBalance).forNamespace(NAMESPACE).forTable(TABLENAME);
    Put toPut =
        new Put(new Key(new TextValue(ID, toId)))
            .withValue(BALANCE, newToBalance).forNamespace(NAMESPACE).forTable(TABLENAME);
    service.put(fromPut);
    service.put(toPut);
  }

  @Override
  public void close() {
    service.close();
  }
}
