package example.service;

import com.scalar.db.config.DatabaseConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class ElectronicMoney {
  private static final String SCALARDB_PROPERTIES =
      System.getProperty("user.dir") + File.separator + "scalardb-client.properties";
  protected static final String TABLENAME = "account";
  protected static final String ID = "id";
  protected static final String BALANCE = "balance";
  protected DatabaseConfig dbConfig;

  public ElectronicMoney() throws IOException {
    dbConfig = new DatabaseConfig(new FileInputStream(SCALARDB_PROPERTIES));
  }

  public abstract void charge(String id, int amount) throws Exception;

  public abstract void pay(String fromId, String toId, int amount) throws Exception;

  public abstract void close();
}
