package src;  // Adjust to your package

import evaluator.Evaluator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Evaluator evaluator = new Evaluator();
        
        System.out.println("=== Simple Interpreter REPL ===");
        System.out.println("Enter code (or 'exit' to quit):");
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.equals("exit") || input.equals("quit")) {
                break;
            }
            
            if (input.isEmpty()) continue;
            
            try {
                // PIPELINE: input → tokenize → parse → evaluate
                Interpreter interpreter = new Interpreter(input);
                interpreter.execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}