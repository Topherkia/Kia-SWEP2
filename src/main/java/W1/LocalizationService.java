package W1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationService {

    private static final String LOCALIZATION_SQL = "SELECT `key`, value FROM localization_strings WHERE language = ?";

    public Map<String, String> getLocalizedStrings(Locale locale) {
        String languageTag = locale.toLanguageTag();
        Map<String, String> localizedValues = fetchLocalizedStrings(languageTag);

        if (localizedValues.isEmpty() && !"en-US".equalsIgnoreCase(languageTag)) {
            localizedValues = fetchLocalizedStrings("en-US");
        }

        return localizedValues;
    }

    private Map<String, String> fetchLocalizedStrings(String languageTag) {
        Map<String, String> messages = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOCALIZATION_SQL)) {

            statement.setString(1, languageTag);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    messages.put(resultSet.getString("key"), resultSet.getString("value"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load localization strings from database: " + e.getMessage());
        }

        return messages;
    }
}