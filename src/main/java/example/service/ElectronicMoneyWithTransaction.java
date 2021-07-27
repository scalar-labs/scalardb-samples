package example.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.CommitConflictException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.transaction.UnknownTransactionStatusException;
import com.scalar.db.io.Key;
import com.scalar.db.io.TextValue;
import com.scalar.db.service.TransactionModule;
import com.scalar.db.service.TransactionService;

import java.io.IOException;
import java.util.Optional;

public class ElectronicMoneyWithTransaction extends ElectronicMoney {
  protected static final String NAMESPACE = "emoney_transaction";
  private final TransactionService service;

  public ElectronicMoneyWithTransaction() throws IOException {
    Injector injector = Guice.createInjector(new TransactionModule(dbConfig));
    service = injector.getInstance(TransactionService.class);
  }

  @Override
  public void charge(String id, int amount) throws TransactionException {
    // Start a transaction
    DistributedTransaction tx = service.start();

    // Retrieve the current balance for id
    Get get = new Get(new Key(new TextValue(ID, id))).forNamespace(NAMESPACE).forTable(TABLENAME);
    Optional<Result> result;
    try {
      result = tx.get(get);
    } catch (CrudException e) {
      tx.abort();
      throw new TransactionException("read data from database failed.", e);
    }

    // Calculate the balance
    int balance = amount;
    if (result.isPresent()) {
      int current = result.get().getValue(BALANCE).get().getAsInt();
      balance += current;
    }

    // Update the balance
    Put put =
        new Put(new Key(new TextValue(ID, id)))
            .withValue(BALANCE, balance)
            .forNamespace(NAMESPACE)
            .forTable(TABLENAME);
    try {
      tx.put(put);
    } catch (CrudException e) {
      tx.abort();
      throw new TransactionException("put data to database failed.", e);
    }

    try {
      // Commit the transaction (records are automatically recovered in case of failure)
      tx.commit();
    } catch (CommitConflictException | UnknownTransactionStatusException e) {
      tx.abort();
      throw new TransactionException("commit transaction failed.", e);
    }
  }

  @Override
  public void pay(String fromId, String toId, int amount) throws TransactionException {
    // Start a transaction
    DistributedTransaction tx = service.start();

    // Retrieve the current balances for ids
    Get fromGet =
        new Get(new Key(new TextValue(ID, fromId))).forNamespace(NAMESPACE).forTable(TABLENAME);
    Get toGet =
        new Get(new Key(new TextValue(ID, toId))).forNamespace(NAMESPACE).forTable(TABLENAME);
    Optional<Result> fromResult;
    Optional<Result> toResult;
    try {
      fromResult = tx.get(fromGet);
      toResult = tx.get(toGet);
    } catch (CrudException e) {
      tx.abort();
      throw new TransactionException("read data from database failed.", e);
    }

    // Calculate the balances (it assumes that both accounts exist)
    int newFromBalance = fromResult.get().getValue(BALANCE).get().getAsInt() - amount;
    int newToBalance = toResult.get().getValue(BALANCE).get().getAsInt() + amount;
    if (newFromBalance < 0) {
      tx.abort();
      throw new RuntimeException(fromId + " doesn't have enough balance.");
    }

    // Update the balances
    Put fromPut =
        new Put(new Key(new TextValue(ID, fromId)))
            .withValue(BALANCE, newFromBalance)
            .forNamespace(NAMESPACE)
            .forTable(TABLENAME);
    Put toPut =
        new Put(new Key(new TextValue(ID, toId)))
            .withValue(BALANCE, newToBalance)
            .forNamespace(NAMESPACE)
            .forTable(TABLENAME);
    try {
      tx.put(fromPut);
      tx.put(toPut);
    } catch (CrudException e) {
      tx.abort();
      throw new TransactionException("put data to database failed.", e);
    }

    try {
      // Commit the transaction (records are automatically recovered in case of failure)
      tx.commit();
    } catch (CommitConflictException | UnknownTransactionStatusException e) {
      tx.abort();
      throw new TransactionException("commit transaction failed.", e);
    }
  }

  @Override
  public void close() {
    service.close();
  }
}
