package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: java Main <filename.speek>");
            return;
        }

        String fileName = args[0];

        try {
            // Read all lines (like Scanner.nextLine())
            List<String> lines = Files.readAllLines(Paths.get(fileName));

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty()) continue;

                try {
                    Interpreter interpreter = new Interpreter(line);
                    interpreter.execute();
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("File Error: " + e.getMessage());
        }
    }
}