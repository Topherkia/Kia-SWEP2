import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorTest {

    @Test
    void addMeShouldReturnSum() {
        assertEquals(5.0, Calculator.addMe(2.0, 3.0));
    }

    @Test
    void addMeShouldHandleNegativeAndDecimalValues() {
        assertEquals(-1.5, Calculator.addMe(-2.0, 0.5));
    }

    @Test
    void subMeShouldReturnDifference() {
        assertEquals(2.0, Calculator.subMe(5.0, 3.0));
    }

    @Test
    void subMeShouldHandleNegativeResult() {
        assertEquals(-4.0, Calculator.subMe(3.0, 7.0));
    }
}