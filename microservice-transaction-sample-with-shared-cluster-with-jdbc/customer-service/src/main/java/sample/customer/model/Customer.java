package sample.customer.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Customer {

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
      Connection connection, int id, String name, int creditLimit, int creditTotal)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(
            "INSERT INTO customer_service.customers (customer_id, name, credit_limit, credit_total) VALUES (?, ?, ?, ?)")) {
      preparedStatement.setInt(1, id);
      preparedStatement.setString(2, name);
      preparedStatement.setInt(3, creditLimit);
      preparedStatement.setInt(4, creditTotal);
      preparedStatement.executeUpdate();
    }
  }

  public static void updateCreditTotal(Connection connection, int id, int creditTotal)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(
            "UPDATE customer_service.customers SET credit_total = ? WHERE customer_id = ?")) {
      preparedStatement.setInt(1, creditTotal);
      preparedStatement.setInt(2, id);
      preparedStatement.executeUpdate();
    }
  }

  public static Optional<Customer> get(Connection connection, int id) throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(
            "SELECT * FROM customer_service.customers WHERE customer_id = ?")) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(
              new Customer(
                  resultSet.getInt("customer_id"),
                  resultSet.getString("name"),
                  resultSet.getInt("credit_limit"),
                  resultSet.getInt("credit_total")));
        }
      }
      return Optional.empty();
    }
  }
}
