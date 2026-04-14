package w1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    static String getDbUrl() {
        return readSetting(
                "DB_URL",
                "jdbc:mysql://localhost:3306/shopping_cart_localization?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
    }
    static String getDbUsername() {
        return readSetting("DB_USERNAME", "root");
    }

    static String getDbPassword() {
        return readSetting("DB_PASSWORD", "");
    }

    private static String readSetting(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUsername(), getDbPassword());
    }
}