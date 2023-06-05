package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sample {

  public void loadInitialData() throws SQLException {
    try (Connection connection = getConnection()) {
      try {
        loadCustomerIfNotExists(connection, 1, "Yamada Taro", 10000, 0);
        loadCustomerIfNotExists(connection, 2, "Yamada Hanako", 10000, 0);
        loadCustomerIfNotExists(connection, 3, "Suzuki Ichiro", 10000, 0);
        loadItemIfNotExists(connection, 1, "Apple", 1000);
        loadItemIfNotExists(connection, 2, "Orange", 2000);
        loadItemIfNotExists(connection, 3, "Grape", 2500);
        loadItemIfNotExists(connection, 4, "Mango", 5000);
        loadItemIfNotExists(connection, 5, "Melon", 3000);

        // Commit the transaction
        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  private void loadCustomerIfNotExists(
      Connection connection, int customerId, String name, int creditLimit, int creditTotal)
      throws SQLException {
    try (PreparedStatement preparedStatementForSelect =
            connection.prepareStatement("SELECT * FROM sample.customers WHERE customer_id = ?");
        PreparedStatement preparedStatementForInsert =
            connection.prepareStatement(
                "INSERT INTO sample.customers (customer_id, name, credit_limit, credit_total) VALUES (?, ?, ?, ?)")) {

      preparedStatementForSelect.setInt(1, customerId);
      try (ResultSet resultSet = preparedStatementForSelect.executeQuery()) {
        if (resultSet.next()) {
          // If the customer info for the specified customer ID already exists, do nothing
          return;
        }
      }

      preparedStatementForInsert.setInt(1, customerId);
      preparedStatementForInsert.setString(2, name);
      preparedStatementForInsert.setInt(3, creditLimit);
      preparedStatementForInsert.setInt(4, creditTotal);
      preparedStatementForInsert.executeUpdate();
    }
  }

  private void loadItemIfNotExists(Connection connection, int itemId, String name, int price)
      throws SQLException {
    try (PreparedStatement preparedStatementForSelect =
            connection.prepareStatement("SELECT * FROM sample.items WHERE item_id = ?");
        PreparedStatement preparedStatementForInsert =
            connection.prepareStatement(
                "INSERT INTO sample.items (item_id, name, price) VALUES (?, ?, ?)")) {

      preparedStatementForSelect.setInt(1, itemId);
      try (ResultSet resultSet = preparedStatementForSelect.executeQuery()) {
        if (resultSet.next()) {
          // If the item info for the specified item ID already exists, do nothing
          return;
        }
      }

      preparedStatementForInsert.setInt(1, itemId);
      preparedStatementForInsert.setString(2, name);
      preparedStatementForInsert.setInt(3, price);
      preparedStatementForInsert.executeUpdate();
    }
  }

  public String getCustomerInfo(int customerId) throws SQLException {
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement =
          connection.prepareStatement("SELECT * FROM sample.customers WHERE customer_id = ?")) {
        preparedStatement.setInt(1, customerId);

        String name;
        int creditLimit;
        int creditTotal;
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (!resultSet.next()) {
            // If the customer info the specified customer ID doesn't exist, throw an exception
            throw new RuntimeException("Customer not found");
          }

          name = resultSet.getString("name");
          creditLimit = resultSet.getInt("credit_limit");
          creditTotal = resultSet.getInt("credit_total");
        }

        // Commit the transaction
        connection.commit();

        // Return the customer info as a JSON format
        return String.format(
            "{\"id\": %d, \"name\": \"%s\", \"credit_limit\": %d, \"credit_total\": %d}",
            customerId, name, creditLimit, creditTotal);
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  public String placeOrder(int customerId, int[] itemIds, int[] itemCounts) throws SQLException {
    assert itemIds.length == itemCounts.length;

    try (Connection connection = getConnection()) {
      try {
        String orderId = UUID.randomUUID().toString();

        // Put the order info into the orders table
        try (PreparedStatement preparedStatement =
            connection.prepareStatement(
                "INSERT INTO sample.orders (customer_id, order_id, timestamp) VALUES (?, ?, ?)")) {
          preparedStatement.setInt(1, customerId);
          preparedStatement.setString(2, orderId);
          preparedStatement.setLong(3, System.currentTimeMillis());
          preparedStatement.executeUpdate();
        }

        int amount = 0;
        for (int i = 0; i < itemIds.length; i++) {
          int itemId = itemIds[i];
          int count = itemCounts[i];

          // Put the order statement into the statements table
          try (PreparedStatement preparedStatement =
              connection.prepareStatement(
                  "INSERT INTO sample.statements (order_id, item_id, count) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, orderId);
            preparedStatement.setInt(2, itemId);
            preparedStatement.setInt(3, count);
            preparedStatement.executeUpdate();
          }

          // Retrieve the item info from the items table
          try (PreparedStatement preparedStatement =
              connection.prepareStatement("SELECT * FROM sample.items WHERE item_id = ?")) {
            preparedStatement.setInt(1, itemId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
              if (!resultSet.next()) {
                // If the item info for the specified item ID doesn't exist, throw an exception
                throw new RuntimeException("Item not found");
              }

              // Calculate the total amount
              amount += resultSet.getInt("price") * count;
            }
          }
        }

        // Check if the credit total exceeds the credit limit after payment
        int updatedCreditTotal;
        try (PreparedStatement preparedStatement =
            connection.prepareStatement("SELECT * FROM sample.customers WHERE customer_id = ?")) {
          preparedStatement.setInt(1, customerId);

          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
              // If the customer info for the specified customer ID doesn't exist, throw an
              // exception
              throw new RuntimeException("Customer not found");
            }

            int creditLimit = resultSet.getInt("credit_limit");
            int creditTotal = resultSet.getInt("credit_total");
            updatedCreditTotal = creditTotal + amount;

            if (updatedCreditTotal > creditLimit) {
              throw new RuntimeException("Credit limit exceeded");
            }
          }
        }

        // Update credit_total for the customer
        try (PreparedStatement preparedStatement =
            connection.prepareStatement(
                "UPDATE sample.customers SET credit_total = ? WHERE customer_id = ?")) {
          preparedStatement.setInt(1, updatedCreditTotal);
          preparedStatement.setInt(2, customerId);
          preparedStatement.executeUpdate();
        }

        // Commit the transaction
        connection.commit();

        // Return the order id
        return String.format("{\"order_id\": \"%s\"}", orderId);
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  private String getOrderJson(Connection connection, String orderId) throws SQLException {
    // Retrieve the order info for the order ID from the orders table
    int customerId;
    long timestamp;
    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT * FROM sample.orders WHERE order_id = ?")) {
      preparedStatement.setString(1, orderId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          // If the order info for the specified order ID doesn't exist, throw an exception
          throw new RuntimeException("Order not found");
        }

        customerId = resultSet.getInt("customer_id");
        timestamp = resultSet.getLong("timestamp");
      }
    }

    // Retrieve the customer info for the specified customer ID from the customers table
    String customerName;
    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT * FROM sample.customers WHERE customer_id = ?")) {
      preparedStatement.setInt(1, customerId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          // If the customer info for the specified customer ID doesn't exist, throw an exception
          throw new RuntimeException("Customer not found");
        }

        customerName = resultSet.getString("name");
      }
    }

    List<String> statementJsons = new ArrayList<>();
    int total = 0;

    // Retrieve the order statements for the order ID from the statements table
    try (PreparedStatement preparedStatementForStatements =
        connection.prepareStatement("SELECT * FROM sample.statements WHERE order_id = ?")) {
      preparedStatementForStatements.setString(1, orderId);

      try (ResultSet resultSetForStatements = preparedStatementForStatements.executeQuery()) {
        while (resultSetForStatements.next()) {
          int itemId = resultSetForStatements.getInt("item_id");

          // Retrieve the item data from the items table
          try (PreparedStatement preparedStatementForItems =
              connection.prepareStatement("SELECT * FROM sample.items WHERE item_id = ?")) {
            preparedStatementForItems.setInt(1, itemId);

            try (ResultSet resultSetForItems = preparedStatementForItems.executeQuery()) {
              if (!resultSetForItems.next()) {
                throw new RuntimeException("Item not found");
              }

              int price = resultSetForItems.getInt("price");
              int count = resultSetForStatements.getInt("count");

              // Make the statements JSON
              statementJsons.add(
                  String.format(
                      "{\"item_id\": %d, \"name\": \"%s\", \"price\": %d, \"count\": %d}",
                      itemId, resultSetForItems.getString("name"), price, count));

              // Calculate the total amount
              total += price * count;
            }
          }
        }
      }

      // Return the order info as a JSON format
      return String.format(
          "{\"order_id\": \"%s\",\"timestamp\": %d,\"customer_id\": %d,\"customer_name\": \"%s\",\"statement\": [%s],\"total\": %d}",
          orderId, timestamp, customerId, customerName, String.join(",", statementJsons), total);
    }
  }

  public String getOrderByOrderId(String orderId) throws SQLException {
    try (Connection connection = getConnection()) {
      try {
        // Get an order JSON for the specified order ID
        String orderJson = getOrderJson(connection, orderId);

        // Commit the transaction
        connection.commit();

        // Return the order info as a JSON format
        return String.format("{\"order\": %s}", orderJson);
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  public String getOrdersByCustomerId(int customerId) throws SQLException {
    try (Connection connection = getConnection()) {
      // Retrieve the order info for the customer ID from the orders table
      try (PreparedStatement preparedStatement =
          connection.prepareStatement("SELECT * FROM sample.orders WHERE customer_id = ?")) {
        preparedStatement.setInt(1, customerId);

        List<String> orderJsons = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          // Make order JSONs for the orders of the customer
          while (resultSet.next()) {
            orderJsons.add(getOrderJson(connection, resultSet.getString("order_id")));
          }
        }

        // Commit the transaction
        connection.commit();

        // Return the order info as a JSON format
        return String.format("{\"order\": [%s]}", String.join(",", orderJsons));
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  public void repayment(int customerId, int amount) throws SQLException {
    try (Connection connection = getConnection()) {
      try {
        // Retrieve the customer info for the specified customer ID from the customers table
        int updatedCreditTotal;
        try (PreparedStatement preparedStatement =
            connection.prepareStatement("SELECT * FROM sample.customers WHERE customer_id = ?")) {
          preparedStatement.setInt(1, customerId);

          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
              // If the customer info for the specified customer ID doesn't exist, throw an
              // exception
              throw new RuntimeException("Customer not found");
            }

            updatedCreditTotal = resultSet.getInt("credit_total") - amount;

            // Check if over repayment or not
            if (updatedCreditTotal < 0) {
              throw new RuntimeException("Over repayment");
            }
          }
        }

        // Reduce credit_total for the customer
        try (PreparedStatement preparedStatement =
            connection.prepareStatement(
                "UPDATE sample.customers SET credit_total = ? WHERE customer_id = ?")) {
          preparedStatement.setInt(1, updatedCreditTotal);
          preparedStatement.setInt(2, customerId);
          preparedStatement.executeUpdate();
        }

        // Commit the transaction
        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  private Connection getConnection() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:scalardb:scalardb-sql.properties");
    connection.setAutoCommit(false);
    return connection;
  }
}
