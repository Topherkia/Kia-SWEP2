package w1;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.junit.jupiter.api.AfterEach;
import org.testfx.util.WaitForAsyncUtils;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.hamcrest.Matchers.containsString;
import java.sql.SQLException;

class ShoppingCartGUITest extends ApplicationTest {
    private ShoppingCartGUI gui;

    @Override
    public void start(Stage stage) throws Exception {
        gui = new ShoppingCartGUI();
        CartService stubService = new CartService() {
            @Override
            public int saveCart(ShoppingCart cart, java.util.Locale locale) {
                return 999;
            }
        };
        gui.setCartService(stubService);
        gui.start(stage);
        }

    @AfterEach
    void tearDown() {
        // Explicitly clear the cart object inside the GUI to ensure
        // a fresh state for the next test.
        interact(() -> {
            clickOn("#clearButton");
        });
    }

    @Test
    void testAddItemAndTotal() {
        clickOn("#priceField").write("10");
        clickOn("#quantityField").write("2");

        clickOn("#addButton");

        clickOn("#totalButton");

        verifyThat("#totalLabel", hasText(containsString("20")));
    }

    @Test
    void testClearCart() {
        // First add an item
        clickOn("#priceField").write("10");
        clickOn("#quantityField").write("2");
        clickOn("#addButton");

        // Then clear the cart
        clickOn("#clearButton");

        verifyThat("#totalLabel", hasText(containsString("0")));
    }

    @Test
    void testNewCartButton() {
        // Add an item first
        clickOn("#priceField").write("15");
        clickOn("#quantityField").write("3");
        clickOn("#addButton");

        verifyThat("#totalLabel", hasText(containsString("45")));

        // Click new cart button
        clickOn("#newCartButton");

        // Confirm dialog should appear - press OK
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("OK");

        // Verify cart is empty
        verifyThat("#totalLabel", hasText(containsString("0")));
    }

    @Test
    void testSaveButtonWithEmptyCart() {
        // Try to save empty cart
        clickOn("#saveButton");

        // Should show warning dialog
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("Cannot save an empty cart", hasText("Cannot save an empty cart"));

        // Close the dialog
        clickOn("OK");
    }

    @Test
    void testSaveButtonWithNonEmptyCart() {
        // Add items to cart
        clickOn("#priceField").write("25.50");
        clickOn("#quantityField").write("2");
        clickOn("#addButton");

        clickOn("#priceField").write("10.00");
        clickOn("#quantityField").write("1");
        clickOn("#addButton");

        // Save the cart
        clickOn("#saveButton");

        // Wait for dialog to appear
        WaitForAsyncUtils.waitForFxEvents();

        // Should show success message
        verifyThat(".dialog-pane .content.label", hasText(containsString("Cart saved successfully")));

        // Close the dialog
        clickOn("OK");
    }

    @Test
    void testAddMultipleItemsAndSave() {
        // Add first item
        clickOn("#priceField").write("10");
        clickOn("#quantityField").write("2");
        clickOn("#addButton");

        // Add second item
        clickOn("#priceField").write("5");
        clickOn("#quantityField").write("3");
        clickOn("#addButton");

        // Check total
        clickOn("#totalButton");
        verifyThat("#totalLabel", hasText(containsString("35")));

        // Save the cart
        clickOn("#saveButton");

        // Wait for dialog to appear
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(".dialog-pane .content.label", hasText(containsString("Cart saved successfully")));
        clickOn("OK");
    }
    @Test
    void testAddItemUsingCombinedPriceQuantityInput() {
        clickOn("#priceField").write("1231");
        clickOn("#addButton");

        verifyThat("#totalLabel", hasText(containsString("123")));
    }

    @Test
    void testAddItemShowsErrorForInvalidCombinedInput() {
        clickOn("#priceField").write("9");
        clickOn("#addButton");

        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(".dialog-pane .header-panel .label", hasText(containsString("Error")));
        clickOn("OK");
    }

    @Test
    void testNewCartWhenAlreadyEmptyShowsInfoDialog() {
        clickOn("#newCartButton");

        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(".dialog-pane .content.label", hasText(containsString("already empty")));
        clickOn("OK");
    }

    @Test
    void testSaveButtonShowsDatabaseError() {
        interact(() -> {
            gui.setCartService(new CartService() {
                @Override
                public int saveCart(ShoppingCart cart, java.util.Locale locale) throws SQLException {
                    throw new SQLException("simulated db failure");
                }
            });
        });

        clickOn("#priceField").write("5");
        clickOn("#quantityField").write("1");
        clickOn("#addButton");
        clickOn("#saveButton");

        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(".dialog-pane .content.label", hasText(containsString("simulated db failure")));
        clickOn("OK");
    }
}