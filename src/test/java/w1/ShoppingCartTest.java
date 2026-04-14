package w1;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shopping Cart Tests")
class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @AfterEach
    void tearDown() {
        cart.clearCart();
    }

    @Nested
    @DisplayName("Item Total Calculation Tests")
    class ItemTotalCalculationTests {

        @Test
        @DisplayName("Should calculate correct total for single item")
        void testCalculateItemTotal_SingleItem() {
            double price = 10.99;
            int quantity = 3;
            double expectedTotal = 32.97;

            double actualTotal = cart.calculateItemTotal(price, quantity);

            assertEquals(expectedTotal, actualTotal, 0.001,
                    "Item total should be price * quantity");
        }

        @ParameterizedTest
        @DisplayName("Should calculate correct total for various price and quantity combinations")
        @CsvSource({
                "5.00, 2, 10.00",
                "3.50, 4, 14.00",
                "0.99, 10, 9.90",
                "100.00, 1, 100.00",
                "0.01, 100, 1.00"
        })
        void testCalculateItemTotal_VariousInputs(double price, int quantity, double expectedTotal) {
            double actualTotal = cart.calculateItemTotal(price, quantity);

            assertEquals(expectedTotal, actualTotal, 0.001,
                    String.format("Item total for %.2f x %d should be %.2f", price, quantity, expectedTotal));
        }

        @Test
        @DisplayName("Should throw exception for negative price")
        void testCalculateItemTotal_NegativePrice() {
            assertThrows(IllegalArgumentException.class,
                    () -> cart.calculateItemTotal(-5.00, 2),
                    "Negative price should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void testCalculateItemTotal_ZeroQuantity() {
            assertThrows(IllegalArgumentException.class,
                    () -> cart.calculateItemTotal(10.00, 0),
                    "Zero quantity should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void testCalculateItemTotal_NegativeQuantity() {
            assertThrows(IllegalArgumentException.class,
                    () -> cart.calculateItemTotal(10.00, -2),
                    "Negative quantity should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("Should handle zero price correctly")
        void testCalculateItemTotal_ZeroPrice() {
            double total = cart.calculateItemTotal(0.00, 5);

            assertEquals(0.00, total, 0.001, "Item total should be 0 when price is 0");
        }

        @Test
        @DisplayName("Should handle large numbers correctly")
        void testCalculateItemTotal_LargeNumbers() {
            double price = 999999.99;
            int quantity = 999;
            double expectedTotal = 999999.99 * 999;

            double actualTotal = cart.calculateItemTotal(price, quantity);

            assertEquals(expectedTotal, actualTotal, 0.001, "Should handle large number calculations");
        }
    }

    @Nested
    @DisplayName("Shopping Cart Total Calculation Tests")
    class CartTotalCalculationTests {

        @Test
        @DisplayName("Should calculate total correctly for empty cart")
        void testCalculateTotalCost_EmptyCart() {
            double total = cart.calculateTotalCost();

            assertEquals(0.00, total, 0.001, "Empty cart should have total of 0");
        }

        @Test
        @DisplayName("Should calculate total correctly for single item")
        void testCalculateTotalCost_SingleItem() {
            cart.addItem(25.50, 2);
            double expectedTotal = 51.00;

            double actualTotal = cart.calculateTotalCost();

            assertEquals(expectedTotal, actualTotal, 0.001,
                    "Cart with one item should calculate correct total");
        }

        @Test
        @DisplayName("Should calculate total correctly for multiple items")
        void testCalculateTotalCost_MultipleItems() {
            cart.addItem(10.99, 2);  // 21.98
            cart.addItem(5.50, 3);   // 16.50
            cart.addItem(3.25, 1);   // 3.25
            double expectedTotal = 41.73;

            double actualTotal = cart.calculateTotalCost();

            assertEquals(expectedTotal, actualTotal, 0.001,
                    "Cart with multiple items should sum correctly");
        }

        @Test
        @DisplayName("Should calculate total correctly with decimal precision")
        void testCalculateTotalCost_DecimalPrecision() {
            cart.addItem(0.10, 3);   // 0.30
            cart.addItem(0.20, 3);   // 0.60
            cart.addItem(0.30, 3);   // 0.90
            double expectedTotal = 1.80;

            double actualTotal = cart.calculateTotalCost();

            assertEquals(expectedTotal, actualTotal, 0.001,
                    "Should handle decimal precision correctly");
        }

        @Test
        @DisplayName("Should calculate total correctly for items with same price")
        void testCalculateTotalCost_IdenticalItems() {
            cart.addItem(15.00, 2);
            cart.addItem(15.00, 3);
            double expectedTotal = 75.00;

            double actualTotal = cart.calculateTotalCost();

            assertEquals(expectedTotal, actualTotal, 0.001,
                    "Should sum correctly even with identical items");
        }

        @Test
        @DisplayName("Should maintain correct total after multiple operations")
        void testCalculateTotalCost_MultipleOperations() {
            cart.addItem(10.00, 1);  // 10.00
            cart.addItem(20.00, 2);  // 40.00
            double total1 = cart.calculateTotalCost();  // 50.00

            cart.addItem(30.00, 1);  // 30.00
            double total2 = cart.calculateTotalCost();  // 80.00

            assertEquals(50.00, total1, 0.001, "Total after first operations should be correct");
            assertEquals(80.00, total2, 0.001, "Total after adding more items should be correct");
        }
    }

    @Nested
    @DisplayName("Cart Management Tests")
    class CartManagementTests {

        @Test
        @DisplayName("Should add item correctly to cart")
        void testAddItem() {
            cart.addItem(15.99, 2);

            assertEquals(1, cart.getItemCount(), "Cart should have 1 item");
            assertFalse(cart.isEmpty(), "Cart should not be empty");
        }

        @Test
        @DisplayName("Should add multiple items correctly")
        void testAddMultipleItems() {
            cart.addItem(10.00, 1);
            cart.addItem(20.00, 2);
            cart.addItem(30.00, 3);

            assertEquals(3, cart.getItemCount(), "Cart should have 3 items");
        }

        @Test
        @DisplayName("Should clear cart correctly")
        void testClearCart() {
            cart.addItem(10.00, 1);
            cart.addItem(20.00, 2);
            assertFalse(cart.isEmpty(), "Cart should not be empty before clearing");

            cart.clearCart();

            assertTrue(cart.isEmpty(), "Cart should be empty after clearing");
            assertEquals(0, cart.calculateTotalCost(), 0.001, "Total should be 0 after clearing");
        }

        @Test
        @DisplayName("Should get items list correctly")
        void testGetItems() {
            cart.addItem(10.00, 2);
            cart.addItem(20.00, 1);

            List<ShoppingCart.ShoppingItem> items = cart.getItems();

            assertEquals(2, items.size(), "Should have 2 items");
            assertEquals(10.00, items.get(0).getPrice(), 0.001, "First item price should match");
            assertEquals(2, items.get(0).getQuantity(), "First item quantity should match");
            assertEquals(20.00, items.get(0).getTotal(), 0.001, "First item total should match");
        }

        @Test
        @DisplayName("Should throw exception when adding item with negative price")
        void testAddItem_NegativePrice() {
            assertThrows(IllegalArgumentException.class,
                    () -> cart.addItem(-10.00, 2),
                    "Should throw exception for negative price");
        }

        @Test
        @DisplayName("Should throw exception when adding item with zero quantity")
        void testAddItem_ZeroQuantity() {
            assertThrows(IllegalArgumentException.class,
                    () -> cart.addItem(10.00, 0),
                    "Should throw exception for zero quantity");
        }

        @Test
        @DisplayName("Should throw exception when adding item with negative quantity")
        void testAddItem_NegativeQuantity() {
            assertThrows(IllegalArgumentException.class,
                    () -> cart.addItem(10.00, -1),
                    "Should throw exception for negative quantity");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very small decimal values")
        void testVerySmallDecimals() {
            cart.addItem(0.001, 1000);
            double expectedTotal = 1.00;

            double actualTotal = cart.calculateTotalCost();

            assertEquals(expectedTotal, actualTotal, 0.001, "Should handle very small decimals");
        }

        @Test
        @DisplayName("Should handle maximum values")
        void testMaximumValues() {
            double maxPrice = Double.MAX_VALUE / 2;
            cart.addItem(maxPrice, 1);
            cart.addItem(maxPrice, 1);

            double total = cart.calculateTotalCost();

            assertTrue(total > 0, "Should handle large values without overflow");
        }

        @Test
        @DisplayName("Should maintain item number sequence")
        void testItemNumberSequence() {
            cart.addItem(10.00, 1);
            cart.addItem(20.00, 1);
            cart.addItem(30.00, 1);

            List<ShoppingCart.ShoppingItem> items = cart.getItems();

            assertEquals(1, items.get(0).getNumber(), "First item should have number 1");
            assertEquals(2, items.get(1).getNumber(), "Second item should have number 2");
            assertEquals(3, items.get(2).getNumber(), "Third item should have number 3");
        }

        @Test
        @DisplayName("Should handle mixed item types correctly")
        void testMixedItemTypes() {
            cart.addItem(1.99, 1);   // Small price
            cart.addItem(100.00, 1); // Medium price
            cart.addItem(1000.00, 1); // Large price

            double expectedTotal = 1101.99;
            double actualTotal = cart.calculateTotalCost();

            assertEquals(expectedTotal, actualTotal, 0.001, "Should correctly sum different price ranges");
        }
    }
}