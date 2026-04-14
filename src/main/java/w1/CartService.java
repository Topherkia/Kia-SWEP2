package w1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class CartService {
    private static final String INSERT_CART_RECORD_SQL =
            "INSERT INTO cart_records(total_items, total_cost, language) VALUES (?, ?, ?)";
    private static final String INSERT_CART_ITEM_SQL =
            "INSERT INTO cart_items(cart_record_id, item_number, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";

    public int saveCart(ShoppingCart cart, Locale locale) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int cartRecordId = saveCartRecord(connection, cart, locale);
                saveCartItems(connection, cartRecordId, cart);
                connection.commit();
                return cartRecordId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private int saveCartRecord(Connection connection, ShoppingCart cart, Locale locale) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_CART_RECORD_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, cart.getItemCount());
            statement.setDouble(2, cart.calculateTotalCost());
            statement.setString(3, locale.toLanguageTag());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to create cart record.");
    }

    private void saveCartItems(Connection connection, int cartRecordId, ShoppingCart cart) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_CART_ITEM_SQL)) {
            for (ShoppingCart.ShoppingItem item : cart.getItems()) {
                statement.setInt(1, cartRecordId);
                statement.setInt(2, item.getNumber());
                statement.setDouble(3, item.getPrice());
                statement.setInt(4, item.getQuantity());
                statement.setDouble(5, item.getTotal());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}