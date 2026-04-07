package W1;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.hamcrest.Matchers.containsString;

public class ShoppingCartGUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new ShoppingCartGUI().start(stage);
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

        clickOn("#clearButton");

        verifyThat("#totalLabel", hasText(containsString("0")));
    }
}