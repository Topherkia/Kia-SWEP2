package w1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class ShoppingCartCalculator {
    private static Locale currentLocale;
    private static Map<String, String> messages;
    private static ShoppingCart cart;
    private static CartService cartService;
    private static LocalizationService localizationService;

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        cart = new ShoppingCart();
        cartService = new CartService();
        localizationService = new LocalizationService();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            selectLanguage(reader);
            messages = localizationService.getLocalizedStrings(currentLocale);

            runShoppingSession(reader);

            if (!cart.isEmpty()) {
                displayCartSummary();
                saveCartToDatabase();
            }
            displayTotal(cart.calculateTotalCost());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            //add for debugging
            //e.printStackTrace();
        }
    }

    private static void selectLanguage(BufferedReader reader) {
        System.out.println("Select language / Valitse kieli / Välj språk / 言語を選択 / زبان را بگزینید:");
        System.out.println("1. English");
        System.out.println("2. Finnish (Suomi)");
        System.out.println("3. Swedish (Svenska)");
        System.out.println("4. Japanese (日本語)");
        System.out.println("5. Persian (پارسی)");

        try {
            int choice = Integer.parseInt(reader.readLine());
            switch (choice) {
                case 1:
                    currentLocale = new Locale("en", "US");
                    break;
                case 2:
                    currentLocale = new Locale("fi", "FI");
                    break;
                case 3:
                    currentLocale = new Locale("sv", "SE");
                    break;
                case 4:
                    currentLocale = new Locale("ja", "JP");
                    break;
                case 5:
                    currentLocale = new Locale("fa", "IR");
                    break;
                default:
                    currentLocale = new Locale("en", "US");
            }
        } catch (Exception e) {
            currentLocale = new Locale("en", "US");
        }
    }

    private static String msg(String key) {
        return messages.getOrDefault(key, key);
    }

    private static void runShoppingSession(BufferedReader reader) {
        int itemCount = 1;

        try {
            while (true) {
                System.out.println("\n" + msg("item") + " " + itemCount);

                System.out.print(msg("price") + ": ");
                String priceInput = reader.readLine();
                if (priceInput == null || priceInput.trim().isEmpty()) {
                    break;
                }

                double price;
                try {
                    price = Double.parseDouble(priceInput);
                } catch (NumberFormatException e) {
                    System.out.println(msg("invalid_price"));
                    continue;
                }

                System.out.print(msg("quantity") + ": ");
                String quantityInput = reader.readLine();
                if (quantityInput == null || quantityInput.trim().isEmpty()) {
                    break;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(quantityInput);
                } catch (NumberFormatException e) {
                    System.out.println(msg("invalid_quantity"));
                    continue;
                }

                cart.addItem(price, quantity);

                System.out.print(msg("add_more") + " (y/n): ");
                String response = reader.readLine();
                if (response == null || !response.equalsIgnoreCase("y")) {
                    break;
                }

                itemCount++;
            }
        } catch (Exception e) {
            System.err.println(msg("error") + ": " + e.getMessage());
        }
    }

    private static void displayCartSummary() {
        System.out.println("\n" + msg("cart_summary"));
        System.out.println("=".repeat(50));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(currentLocale);

        for (ShoppingCart.ShoppingItem item : cart.getItems()) {
            System.out.printf("%s %d: %s x %d = %s%n",
                    msg("item"),
                    item.getNumber(),
                    currencyFormat.format(item.getPrice()),
                    item.getQuantity(),
                    currencyFormat.format(item.getTotal()));
        }

        System.out.println("=".repeat(50));
    }

    private static void saveCartToDatabase() {
        try {
            int cartId = cartService.saveCart(cart, currentLocale);
            System.out.println("Cart saved to database with id: " + cartId);
        } catch (SQLException e) {
            System.err.println("Could not save cart to database: " + e.getMessage());
        }
    }

    private static void displayTotal(double total) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(currentLocale);

        System.out.println("\n" + msg("total_cost") + ": " + currencyFormat.format(total));

        if (total == 0) {
            System.out.println(msg("empty_cart"));
        } else if (total < 50) {
            System.out.println(msg("small_total"));
        } else if (total < 200) {
            System.out.println(msg("medium_total"));
        } else {
            System.out.println(msg("large_total"));
        }

        System.out.println(msg("thank_you"));
    }
}