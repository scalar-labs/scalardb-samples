package sample.order.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Statement {

  public final String orderId;
  public final int itemId;
  public final int count;

  public Statement(String orderId, int itemId, int count) {
    this.orderId = orderId;
    this.itemId = itemId;
    this.count = count;
  }

  public static void put(Connection connection, String orderId, int itemId, int count)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(
            "INSERT INTO order_service.statements (order_id, item_id, count) VALUES (?, ?, ?)")) {
      preparedStatement.setString(1, orderId);
      preparedStatement.setInt(2, itemId);
      preparedStatement.setInt(3, count);
      preparedStatement.executeUpdate();
    }
  }

  public static List<Statement> getByOrderId(Connection connection, String orderId)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT * FROM order_service.statements WHERE order_id = ?")) {
      preparedStatement.setString(1, orderId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        List<Statement> statements = new ArrayList<>();
        while (resultSet.next()) {
          statements.add(
              new Statement(
                  resultSet.getString("order_id"),
                  resultSet.getInt("item_id"),
                  resultSet.getInt("count")));
        }
        return statements;
      }
    }
  }
}
