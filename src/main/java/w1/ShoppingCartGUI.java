package w1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ShoppingCartGUI extends Application {

    private ShoppingCart cart = new ShoppingCart();

    private TextField priceField;
    private TextField quantityField;
    private TextArea outputArea;
    private Label totalLabel;
    private ComboBox<String> languageBox;

    private Locale currentLocale;
    private ResourceBundle messages;

    private final String BASE_PATH = "MessagesBundle";

    private CartService cartService;
    // For Testing:
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
    @Override
    public void start(Stage stage) {
        stage.setTitle("Shopping Cart");

        if (this.cartService == null) {
            this.cartService = new CartService();
        }

        // Language selector
        languageBox = new ComboBox<>();
        languageBox.setId("languageBox");
        languageBox.getItems().addAll(
                "English",
                "Suomi",
                "Svenska",
                "日本語",
                "پارسی"
        );
        languageBox.setValue("English");
        setLocale("English");

        languageBox.setOnAction(e -> {
            setLocale(languageBox.getValue());
            updateTexts();
        });

        // Input fields
        priceField = new TextField();
        priceField.setId("priceField");
        quantityField = new TextField();
        quantityField.setId("quantityField");

        // Buttons
        Button addButton = new Button();
        addButton.setId("addButton");
        Button clearButton = new Button();
        clearButton.setId("clearButton");
        Button totalButton = new Button();
        totalButton.setId("totalButton");
        Button saveButton = new Button();
        saveButton.setId("saveButton");
        Button newCartButton = new Button();
        newCartButton.setId("newCartButton");

        // Output
        outputArea = new TextArea();
        outputArea.setId("outputArea");
        outputArea.setEditable(false);

        totalLabel = new Label();
        totalLabel.setId("totalLabel");

        // Actions
        addButton.setOnAction(e -> addItem());
        clearButton.setOnAction(e -> clearCart());
        totalButton.setOnAction(e -> updateTotal());
        saveButton.setOnAction(e -> saveCartToDatabase());
        newCartButton.setOnAction(e -> newCart());

        // Layout
        HBox inputBox = new HBox(10, priceField, quantityField, addButton);
        HBox actionBox = new HBox(10, totalButton, clearButton, saveButton, newCartButton);
        VBox root = new VBox(10, languageBox, inputBox, actionBox, outputArea, totalLabel);

        root.setPadding(new Insets(15));

        // Store buttons for text updates
        this.addButtonRef = addButton;
        this.clearButtonRef = clearButton;
        this.totalButtonRef = totalButton;
        this.saveButtonRef = saveButton;
        this.newCartButtonRef = newCartButton;

        updateTexts();

        Scene scene = new Scene(root, 650, 420);
        stage.setScene(scene);
        stage.show();
    }

    private Button addButtonRef;
    private Button clearButtonRef;
    private Button totalButtonRef;
    private Button saveButtonRef;
    private Button newCartButtonRef;

    private void setLocale(String language) {
        switch (language) {
            case "Suomi":
                currentLocale = new Locale("fi", "FI");
                break;
            case "Svenska":
                currentLocale = new Locale("sv", "SE");
                break;
            case "日本語":
                currentLocale = new Locale("ja", "JP");
                break;
            case "پارسی":
                currentLocale = new Locale("fa", "IR");
                break;
            default:
                currentLocale = new Locale("en", "US");
        }

        messages = ResourceBundle.getBundle(BASE_PATH, currentLocale);
    }

    private void updateTexts() {
        priceField.setPromptText(messages.getString("price"));
        quantityField.setPromptText(messages.getString("quantity"));

        addButtonRef.setText(messages.getString("add"));
        clearButtonRef.setText(messages.getString("clear"));
        totalButtonRef.setText(messages.getString("total"));
        saveButtonRef.setText(messages.getString("save"));
        newCartButtonRef.setText(messages.getString("new_cart"));

        updateTotal();
    }

    private void addItem() {
        try {
            String priceText = priceField.getText().trim();
            String quantityText = quantityField.getText().trim();

            double price;
            int quantity;

            if (!priceText.isEmpty() && !quantityText.isEmpty()) {
                price = Double.parseDouble(priceText);
                quantity = Integer.parseInt(quantityText);

            } else if (!priceText.isEmpty()) {
                String digitsOnly = priceText.replaceAll("[^0-9]", "");

                if (digitsOnly.length() < 2) {
                    throw new IllegalArgumentException();
                }

                price = Double.parseDouble(digitsOnly.substring(0, digitsOnly.length() - 1));
                quantity = Integer.parseInt(digitsOnly.substring(digitsOnly.length() - 1));

            } else {
                throw new IllegalArgumentException();
            }

            cart.addItem(price, quantity);

            NumberFormat format = NumberFormat.getCurrencyInstance(currentLocale);

            outputArea.appendText(
                    messages.getString("item") + " " + cart.getItemCount() + ": " +
                            format.format(price) + " x " + quantity + " = " +
                            format.format(price * quantity) + "\n"
            );

            priceField.clear();
            quantityField.clear();

            updateTotal();

        } catch (Exception e) {
            showError(messages.getString("error"));
        }
    }

    private void clearCart() {
        cart.clearCart();
        outputArea.clear();
        totalLabel.setText(messages.getString("total_cost") + ": 0");
        showInfo(messages.getString("cart_cleared"));
    }

    private void newCart() {
        if (!cart.isEmpty()) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle(messages.getString("confirm"));
            confirmDialog.setHeaderText(messages.getString("new_cart_confirm_header"));
            confirmDialog.setContentText(messages.getString("new_cart_confirm_content"));

            ButtonType result = confirmDialog.showAndWait().orElse(ButtonType.CANCEL);

            if (result == ButtonType.OK) {
                cart.clearCart();
                outputArea.clear();
                updateTotal();
                showInfo(messages.getString("new_cart_created"));
            }
        } else {
            showInfo(messages.getString("cart_already_empty"));
        }
    }

    private void saveCartToDatabase() {
        if (cart.isEmpty()) {
            showWarning(messages.getString("cannot_save_empty_cart"));
            return;
        }

        try {
            int cartId = cartService.saveCart(cart, currentLocale);
            showInfo(messages.getString("cart_saved") + " " + messages.getString("id") + ": " + cartId);
        } catch (SQLException e) {
            showError(messages.getString("database_error") + ": " + e.getMessage());
        }
    }

    private void updateTotal() {
        double total = cart.calculateTotalCost();
        NumberFormat format = NumberFormat.getCurrencyInstance(currentLocale);

        totalLabel.setText(messages.getString("total_cost") + ": " + format.format(total));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(messages.getString("error"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(messages.getString("warning"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(messages.getString("information"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
