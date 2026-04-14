package w1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartCalculatorTest {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() throws Exception {
        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        setStaticField("cart", new ShoppingCart());
        setStaticField("currentLocale", Locale.US);

        Map<String, String> messages = new HashMap<>();
        messages.put("item", "Item");
        messages.put("price", "Price");
        messages.put("quantity", "Quantity");
        messages.put("invalid_price", "Invalid price");
        messages.put("invalid_quantity", "Invalid quantity");
        messages.put("add_more", "Add more");
        messages.put("error", "Error");
        messages.put("cart_summary", "Cart Summary");
        messages.put("total_cost", "Total Cost");
        messages.put("empty_cart", "Empty cart");
        messages.put("small_total", "Small total");
        messages.put("medium_total", "Medium total");
        messages.put("large_total", "Large total");
        messages.put("thank_you", "Thanks");
        setStaticField("messages", messages);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void selectLanguage_usesChosenLanguageAndDefaultsOnInvalid() throws Exception {
        invokePrivate("selectLanguage", reader("2\n"));
        assertEquals(Locale.forLanguageTag("fi-FI"), getStaticField("currentLocale"));

        invokePrivate("selectLanguage", reader("99\n"));
        assertEquals(Locale.US, getStaticField("currentLocale"));

        invokePrivate("selectLanguage", reader("abc\n"));
        assertEquals(Locale.US, getStaticField("currentLocale"));
    }

    @Test
    void runShoppingSession_handlesInvalidInputsAndAddsValidItem() throws Exception {
        String input = String.join("\n",
                "bad",      // invalid price
                "10",       // valid price
                "bad",      // invalid qty
                "10",       // valid price
                "2",        // valid qty
                "n"         // stop
        ) + "\n";

        invokePrivate("runShoppingSession", reader(input));

        ShoppingCart cart = getStaticField("cart");
        assertEquals(1, cart.getItemCount());

        String out = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("Invalid price"));
        assertTrue(out.contains("Invalid quantity"));
    }

    @Test
    void runShoppingSession_printsErrorWhenReaderThrows() throws Exception {
        BufferedReader explodingReader = new BufferedReader(new StringReader("")) {
            @Override
            public String readLine() {
                throw new RuntimeException("boom");
            }
        };

        invokePrivate("runShoppingSession", explodingReader);

        assertTrue(errContent.toString(StandardCharsets.UTF_8).contains("Error"));
    }

    @Test
    void displayCartSummary_and_displayTotal_coverAllThresholds() throws Exception {
        ShoppingCart cart = getStaticField("cart");
        cart.addItem(10, 2);

        invokePrivate("displayCartSummary");

        String summaryOutput = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(summaryOutput.contains("Cart Summary"));
        assertTrue(summaryOutput.contains("Item 1"));

        outContent.reset();
        invokePrivate("displayTotal", 0.0);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("Empty cart"));

        outContent.reset();
        invokePrivate("displayTotal", 49.99);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("Small total"));

        outContent.reset();
        invokePrivate("displayTotal", 150.0);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("Medium total"));

        outContent.reset();
        invokePrivate("displayTotal", 250.0);
        String largeOutput = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(largeOutput.contains("Large total"));
        assertTrue(largeOutput.contains(NumberFormat.getCurrencyInstance(Locale.US).format(250.0)));
        assertTrue(largeOutput.contains("Thanks"));
    }

    @Test
    void saveCartToDatabase_printsSuccessAndErrorPaths() throws Exception {
        ShoppingCart cart = getStaticField("cart");
        cart.addItem(5, 1);

        setStaticField("cartService", new CartService() {
            @Override
            public int saveCart(ShoppingCart c, Locale locale) {
                return 123;
            }
        });

        invokePrivate("saveCartToDatabase");
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("Cart saved to database with id: 123"));

        outContent.reset();
        errContent.reset();
        setStaticField("cartService", new CartService() {
            @Override
            public int saveCart(ShoppingCart c, Locale locale) throws SQLException {
                throw new SQLException("db down");
            }
        });

        invokePrivate("saveCartToDatabase");
        assertTrue(errContent.toString(StandardCharsets.UTF_8).contains("Could not save cart to database: db down"));
    }

    @Test
    void msg_returnsKeyWhenMissing() throws Exception {
        String existing = invokePrivate("msg", "item");
        String missing = invokePrivate("msg", "missing_key");

        assertEquals("Item", existing);
        assertEquals("missing_key", missing);
    }

    private BufferedReader reader(String input) {
        return new BufferedReader(new StringReader(input));
    }

    @SuppressWarnings("unchecked")
    private <T> T getStaticField(String fieldName) throws Exception {
        Field field = ShoppingCartCalculator.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(null);
    }

    private void setStaticField(String fieldName, Object value) throws Exception {
        Field field = ShoppingCartCalculator.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivate(String methodName, Object... args) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }

        if (methodName.equals("displayTotal")) {
            parameterTypes = new Class<?>[]{double.class};
        } else if (methodName.equals("msg")) {
            parameterTypes = new Class<?>[]{String.class};
        } else if (methodName.equals("selectLanguage") || methodName.equals("runShoppingSession")) {
            parameterTypes = new Class<?>[]{BufferedReader.class};
        }

        Method method = ShoppingCartCalculator.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(null, args);
    }
}