import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Calculator {
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        Locale locale = null;
        ResourceBundle rb = null;

        System.out.println("Select a language");
        System.out.println("1. English");
        System.out.println("2. Finnish");
        System.out.println("3. Persian");
        int choice = input.nextInt();

        switch (choice) {
            case 1:
                locale = new Locale("en", "US");
                break;

            case 2:
                locale = new Locale("fi", "FI");
                break;

            case 3:
                locale = new Locale("fa", "IR");
                break;

            default:
                locale = new Locale("en", "US");
                System.out.println("Invalid choice. Using English as default.");
                break;
        }

        rb = ResourceBundle.getBundle("MessagesBundle", locale);
        String wish = rb.getString("wish");
        System.out.println(wish);
    }

    public static double addMe(double a, double b){
        return a + b;
    }

    public static double subMe(double a, double b){
        return a - b;
    }
}