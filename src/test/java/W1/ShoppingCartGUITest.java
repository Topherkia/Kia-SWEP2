package W1;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class ShoppingCartGUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new ShoppingCartGUI().start(stage);
    }

    @Test
    void testAddItemAndTotal() {

        clickOn(".text-field").write("10");   // price
        clickOn(".text-field").write("2");    // quantity

        clickOn("Add");

        clickOn("Total");

        verifyThat(".label", hasText(org.hamcrest.Matchers.containsString("20")));
    }

    @Test
    void testClearCart() {

        clickOn("Clear");

        verifyThat(".label", hasText(org.hamcrest.Matchers.containsString("0")));
    }
}
