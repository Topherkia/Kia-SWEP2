package W1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

public class ShoppingCartCalculator {
    private static Locale currentLocale;
    private static ResourceBundle messages;
    private static final String BASE_PATH = "MessagesBundle";
    private static ShoppingCart cart;

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        cart = new ShoppingCart();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            selectLanguage(reader);

            try {
                messages = ResourceBundle.getBundle(BASE_PATH, currentLocale);
            } catch (MissingResourceException e) {
                messages = ResourceBundle.getBundle(BASE_PATH, Locale.US);
            }

            runShoppingSession(reader);

            if (!cart.isEmpty()) {
                displayCartSummary();
            }
            displayTotal(cart.calculateTotalCost());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
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

    private static void runShoppingSession(BufferedReader reader) {
        int itemCount = 1;

        try {
            while (true) {
                System.out.println("\n" + messages.getString("item") + " " + itemCount);

                // Get price
                System.out.print(messages.getString("price") + ": ");
                String priceInput = reader.readLine();
                if (priceInput == null || priceInput.trim().isEmpty()) {
                    break;
                }

                double price;
                try {
                    price = Double.parseDouble(priceInput);
                } catch (NumberFormatException e) {
                    System.out.println(messages.getString("invalid_price"));
                    continue;
                }

                // Get quantity
                System.out.print(messages.getString("quantity") + ": ");
                String quantityInput = reader.readLine();
                if (quantityInput == null || quantityInput.trim().isEmpty()) {
                    break;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(quantityInput);
                } catch (NumberFormatException e) {
                    System.out.println(messages.getString("invalid_quantity"));
                    continue;
                }

                cart.addItem(price, quantity);

                // Ask if user wants to add more items
                System.out.print(messages.getString("add_more") + " (y/n): ");
                String response = reader.readLine();
                if (response == null || !response.equalsIgnoreCase("y")) {
                    break;
                }

                itemCount++;
            }
        } catch (Exception e) {
            System.err.println(messages.getString("error") + ": " + e.getMessage());
        }
    }

    private static void displayCartSummary() {
        System.out.println("\n" + messages.getString("cart_summary"));
        System.out.println("=".repeat(50));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(currentLocale);

        for (ShoppingCart.ShoppingItem item : cart.getItems()) {
            System.out.printf("%s %d: %s x %d = %s%n",
                    messages.getString("item"),
                    item.getNumber(),
                    currencyFormat.format(item.getPrice()),
                    item.getQuantity(),
                    currencyFormat.format(item.getTotal()));
        }

        System.out.println("=".repeat(50));
    }

    private static void displayTotal(double total) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(currentLocale);

        System.out.println("\n" + messages.getString("total_cost") + ": " +
                currencyFormat.format(total));

        if (total == 0) {
            System.out.println(messages.getString("empty_cart"));
        } else if (total < 50) {
            System.out.println(messages.getString("small_total"));
        } else if (total < 200) {
            System.out.println(messages.getString("medium_total"));
        } else {
            System.out.println(messages.getString("large_total"));
        }

        System.out.println(messages.getString("thank_you"));
    }
}