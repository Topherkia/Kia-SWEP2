package w1;

import java.util.logging.Level;
import java.util.logging.Logger;
public class Main {
    // Create a logger instance
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        // Calculate the result
        int result = addMe(12, 4);
        // Conditionally log the result only if INFO level is enabled
        if (logger.isLoggable(Level.INFO)) {
            // Construct the log message only if INFO level logging is enabled
            logger.info(String.format("Result of addMe(12, 4): %d", result));
        }
    }
    public static int addMe(int a, int b) {
        return a + b;
    }
}