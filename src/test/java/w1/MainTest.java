package w1;

import org.junit.jupiter.api.Test;
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
}
