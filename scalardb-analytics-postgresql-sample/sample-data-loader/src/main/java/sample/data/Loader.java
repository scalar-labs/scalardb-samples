package sample.data;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Put;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import com.scalar.db.service.TransactionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class Loader implements AutoCloseable {

  private static final String[] CUSTOMER_COLUMNS = {
    "c_custkey",
    "c_name",
    "c_address",
    "c_nationkey",
    "c_phone",
    "c_acctbal",
    "c_mktsegment",
    "c_comment"
  };

  private static final String[] ORDERS_COLUMNS = {
    "o_orderkey",
    "o_custkey",
    "o_orderstatus",
    "o_totalprice",
    "o_orderdate",
    "o_orderpriority",
    "o_clerk",
    "o_shippriority",
    "o_comment"
  };

  private static final String[] LINEITEM_COLUMNS = {
    "l_orderkey",
    "l_partkey",
    "l_suppkey",
    "l_linenumber",
    "l_quantity",
    "l_extendedprice",
    "l_discount",
    "l_tax",
    "l_returnflag",
    "l_linestatus",
    "l_shipdate",
    "l_commitdate",
    "l_receiptdate",
    "l_shipinstruct",
    "l_shipmode",
    "l_comment"
  };

  private final DistributedTransactionManager manager;

  public Loader() throws IOException {
    TransactionFactory factory = TransactionFactory.create("/etc/scalardb.properties");
    manager = factory.getTransactionManager();
  }

  public void close() {
    manager.close();
  }

  public void load() throws TransactionException, IOException {
    loadData(this.manager, "/data/customer.csv", CUSTOMER_COLUMNS, this::buildPutCustomer);

    loadData(this.manager, "/data/orders.csv", ORDERS_COLUMNS, this::buildPutOrders);

    loadData(this.manager, "/data/lineitem.csv", LINEITEM_COLUMNS, this::buildPutLineitem);
  }

  private Put buildPutCustomer(CSVRecord record) {
    return Put.newBuilder()
        .namespace("dynamons")
        .table("customer")
        .partitionKey(Key.ofInt("c_custkey", intCol(record, "c_custkey")))
        .textValue("c_name", stringCol(record, "c_name"))
        .textValue("c_address", stringCol(record, "c_address"))
        .intValue("c_nationkey", intCol(record, "c_nationkey"))
        .textValue("c_phone", stringCol(record, "c_phone"))
        .doubleValue("c_acctbal", doubleCol(record, "c_acctbal"))
        .textValue("c_mktsegment", stringCol(record, "c_mktsegment"))
        .textValue("c_comment", stringCol(record, "c_comment"))
        .build();
  }

  private Put buildPutOrders(CSVRecord record) {
    return Put.newBuilder()
        .namespace("postgresns")
        .table("orders")
        .partitionKey(Key.ofInt("o_orderkey", intCol(record, "o_orderkey")))
        .intValue("o_custkey", intCol(record, "o_custkey"))
        .textValue("o_orderstatus", stringCol(record, "o_orderstatus"))
        .doubleValue("o_totalprice", doubleCol(record, "o_totalprice"))
        .textValue("o_orderdate", stringCol(record, "o_orderdate"))
        .textValue("o_orderpriority", stringCol(record, "o_orderpriority"))
        .textValue("o_clerk", stringCol(record, "o_clerk"))
        .intValue("o_shippriority", intCol(record, "o_shippriority"))
        .textValue("o_comment", stringCol(record, "o_comment"))
        .build();
  }

  private Put buildPutLineitem(CSVRecord record) {
    return Put.newBuilder()
        .namespace("cassandrans")
        .table("lineitem")
        .partitionKey(
            Key.of(
                "l_orderkey",
                intCol(record, "l_orderkey"),
                "l_linenumber",
                intCol(record, "l_linenumber")))
        .intValue("l_partkey", intCol(record, "l_partkey"))
        .intValue("l_suppkey", intCol(record, "l_suppkey"))
        .intValue("l_quantity", intCol(record, "l_quantity"))
        .doubleValue("l_extendedprice", doubleCol(record, "l_extendedprice"))
        .doubleValue("l_discount", doubleCol(record, "l_discount"))
        .doubleValue("l_tax", doubleCol(record, "l_tax"))
        .textValue("l_returnflag", stringCol(record, "l_returnflag"))
        .textValue("l_linestatus", stringCol(record, "l_linestatus"))
        .textValue("l_shipdate", stringCol(record, "l_shipdate"))
        .textValue("l_commitdate", stringCol(record, "l_commitdate"))
        .textValue("l_receiptdate", stringCol(record, "l_receiptdate"))
        .textValue("l_shipinstruct", stringCol(record, "l_shipinstruct"))
        .textValue("l_shipmode", stringCol(record, "l_shipmode"))
        .textValue("l_comment", stringCol(record, "l_comment"))
        .build();
  }

  private void loadData(
      DistributedTransactionManager manager,
      String path,
      String[] columnHeader,
      Function<CSVRecord, Put> putFunction)
      throws TransactionException, IOException {
    DistributedTransaction transaction = null;
    try (BufferedReader reader = Files.newBufferedReader(Path.of(path))) {
      Iterable<CSVRecord> records =
          CSVFormat.Builder.create().setHeader(columnHeader).build().parse(reader);
      transaction = manager.start();
      for (CSVRecord record : records) {
        Put put = putFunction.apply(record);
        transaction.put(put);
      }
      transaction.commit();
    } catch (TransactionException e) {
      if (transaction != null) {
        transaction.abort();
      }
      throw e;
    }
  }

  private String stringCol(CSVRecord record, String column) {
    return record.get(column);
  }

  private int intCol(CSVRecord record, String column) {
    return Integer.parseInt(record.get(column));
  }

  private double doubleCol(CSVRecord record, String column) {
    return Double.parseDouble(record.get(column));
  }
}
