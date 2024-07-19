package sample.order.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Item {

  public final int id;
  public final String name;
  public final int price;

  public Item(int id, String name, int price) {
    this.id = id;
    this.name = name;
    this.price = price;
  }

  public static void put(Connection connection, int id, String name, int price)
      throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement(
            "INSERT INTO order_service.items (item_id, name, price) VALUES (?, ?, ?)")) {
      preparedStatement.setInt(1, id);
      preparedStatement.setString(2, name);
      preparedStatement.setInt(3, price);
      preparedStatement.executeUpdate();
    }
  }

  public static Optional<Item> get(Connection connection, int id) throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT * FROM order_service.items WHERE item_id = ?")) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(
              new Item(
                  resultSet.getInt("item_id"),
                  resultSet.getString("name"),
                  resultSet.getInt("price")));
        }
        return Optional.empty();
      }
    }
  }
}
