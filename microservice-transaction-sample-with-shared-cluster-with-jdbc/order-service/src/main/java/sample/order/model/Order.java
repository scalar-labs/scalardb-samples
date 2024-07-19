package sample.order.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Order {

  public final String id;
  public final int customerId;
  public final long timestamp;

  public Order(String id, int customerId, long timestamp) {
    this.id = id;
    this.customerId = customerId;
    this.timestamp = timestamp;
  }

  public static void put(Connection connection, String id, int customerId, long timestamp)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(
            "INSERT INTO order_service.orders (customer_id, order_id, timestamp) VALUES (?, ?, ?)")) {
      preparedStatement.setInt(1, customerId);
      preparedStatement.setString(2, id);
      preparedStatement.setLong(3, timestamp);
      preparedStatement.executeUpdate();
    }
  }

  public static Optional<Order> getById(Connection connection, String id) throws SQLException {

    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT * FROM order_service.orders WHERE order_id = ?")) {
      preparedStatement.setString(1, id);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(resultSetToOrder(resultSet));
        }
        return Optional.empty();
      }
    }
  }

  public static List<Order> getByCustomerId(Connection connection, int customerId)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT * FROM order_service.orders WHERE customer_id = ?")) {
      preparedStatement.setInt(1, customerId);

      List<Order> orders = new ArrayList<>();
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          orders.add(resultSetToOrder(resultSet));
        }
      }

      return orders;
    }
  }

  private static Order resultSetToOrder(ResultSet resultSet) throws SQLException {
    return new Order(
        resultSet.getString("order_id"),
        resultSet.getInt("customer_id"),
        resultSet.getLong("timestamp"));
  }
}
