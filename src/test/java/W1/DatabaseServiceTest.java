package W1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database-backed Services Tests")
class DatabaseServicesTest {

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("DB_URL", "jdbc:h2:mem:shopping_cart_localization;MODE=MySQL;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USERNAME", "sa");
        System.setProperty("DB_PASSWORD", "");

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS cart_items");
            connection.createStatement().execute("DROP TABLE IF EXISTS cart_records");
            connection.createStatement().execute("DROP TABLE IF EXISTS localization_strings");

            connection.createStatement().execute("""
                    CREATE TABLE cart_records (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        total_items INT NOT NULL,
                        total_cost DOUBLE NOT NULL,
                        language VARCHAR(10),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            connection.createStatement().execute("""
                    CREATE TABLE cart_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        cart_record_id INT,
                        item_number INT NOT NULL,
                        price DOUBLE NOT NULL,
                        quantity INT NOT NULL,
                        subtotal DOUBLE NOT NULL,
                        FOREIGN KEY (cart_record_id) REFERENCES cart_records(id) ON DELETE CASCADE
                    )
                    """);

            connection.createStatement().execute("""
                    CREATE TABLE localization_strings (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        `key` VARCHAR(100) NOT NULL,
                        value VARCHAR(255) NOT NULL,
                        language VARCHAR(10) NOT NULL
                    )
                    """);

            connection.createStatement().execute("""
                    INSERT INTO localization_strings (`key`, value, language)
                    VALUES
                    ('item', 'Item', 'en-US'),
                    ('price', 'Enter price', 'en-US'),
                    ('item', 'Tavara', 'fi-FI')
                    """);
        }
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USERNAME");
        System.clearProperty("DB_PASSWORD");
    }

    @Test
    @DisplayName("LocalizationService should load values for selected language")
    void localizationService_LoadsLocalizedValues() {
        LocalizationService service = new LocalizationService();

        Map<String, String> localized = service.getLocalizedStrings(Locale.forLanguageTag("fi-FI"));

        assertEquals("Tavara", localized.get("item"));
    }

    @Test
    @DisplayName("LocalizationService should fallback to en-US when selected language is missing")
    void localizationService_FallsBackToEnglish() {
        LocalizationService service = new LocalizationService();

        Map<String, String> localized = service.getLocalizedStrings(Locale.forLanguageTag("sv-SE"));

        assertEquals("Item", localized.get("item"));
        assertEquals("Enter price", localized.get("price"));
    }

    @Test
    @DisplayName("CartService should store cart header and item rows")
    void cartService_SavesCartAndItems() throws SQLException {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(10.0, 2);
        cart.addItem(5.5, 3);

        CartService service = new CartService();
        int cartRecordId = service.saveCart(cart, Locale.forLanguageTag("en-US"));

        assertTrue(cartRecordId > 0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT total_items, total_cost, language FROM cart_records WHERE id = ?")) {
                statement.setInt(1, cartRecordId);

                try (ResultSet rs = statement.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt("total_items"));
                    assertEquals(36.5, rs.getDouble("total_cost"), 0.001);
                    assertEquals("en-US", rs.getString("language"));
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS row_count FROM cart_items WHERE cart_record_id = ?")) {
                statement.setInt(1, cartRecordId);

                try (ResultSet rs = statement.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt("row_count"));
                }
            }
        }
    }

    @Test
    @DisplayName("DatabaseConnection should prefer system properties for testability")
    void databaseConnection_UsesSystemProperties() {
        assertTrue(DatabaseConnection.getDbUrl().contains("jdbc:h2:mem:shopping_cart_localization"));
        assertEquals("sa", DatabaseConnection.getDbUsername());
        assertEquals("", DatabaseConnection.getDbPassword());
    }
}