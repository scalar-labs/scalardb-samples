package sample.data;

import com.scalar.db.exception.transaction.TransactionException;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException, TransactionException {
    try (Loader loader = new Loader()) {
      loader.load();
    }
  }
}
