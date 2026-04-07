package W1;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class ShoppingCartFxTest {

    private ShoppingCartFxView view;

    @Start
    private void start(Stage stage) {
        view = new ShoppingCartFxView();
        stage.setScene(new Scene(view, 300, 200));
        stage.show();
    }

    @Test
    void shouldDisplayInitialTotal(FxRobot robot) {
        assertThat(robot.lookup("#totalLabel").queryLabeled()).hasText("Total: 0.00");
        assertEquals(0.0, view.getTotal(), 0.001);
    }

    @Test
    void shouldIncreaseTotalWhenButtonIsClicked(FxRobot robot) {
        robot.clickOn("#addButton");

        assertThat(robot.lookup("#totalLabel").queryLabeled()).hasText("Total: 10.00");
        assertEquals(10.0, view.getTotal(), 0.001);
    }
}