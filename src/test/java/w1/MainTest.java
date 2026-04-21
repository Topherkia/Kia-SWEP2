package w1;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void addMe_returnsSum() {
        assertEquals(16, Main.addMe(12, 4));
        assertEquals(-1, Main.addMe(2, -3));
    }

    @Test
    void main_executesWithoutThrowing() {
        assertDoesNotThrow(() -> Main.main(new String[]{}),
                "Main method should execute without throwing any exceptions");
    }
    @Test
    void main_executesWithInfoLoggingDisabled() throws Exception {
        Field loggerField = Main.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        Logger logger = (Logger) loggerField.get(null);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            assertDoesNotThrow(() -> Main.main(new String[]{}),
                    "Main method should execute even when INFO logging is disabled");
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
