
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Simple Interpreter ===");
        System.out.println("Enter your code. Press ENTER on empty line to execute.");
        System.out.println("Type 'exit' to quit.\n");

        while (true) {
            StringBuilder inputBuilder = new StringBuilder();

            System.out.println("Enter code:");

            while (true) {
                String line = scanner.nextLine();

                // Exit condition
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    scanner.close();
                    return;
                }

                // Empty line → stop input
                if (line.trim().isEmpty()) {
                    break;
                }

                inputBuilder.append(line).append("\n");
            }

            String input = inputBuilder.toString();

            if (input.trim().isEmpty()) continue;

            try {
                Interpreter interpreter = new Interpreter(input);
                interpreter.execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
