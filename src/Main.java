package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: java src.Main <filename.speek>");
            return;
        }

        String fileName = args[0];

        try {
            // Read full file as a single program
            String input = Files.readString(Paths.get(fileName));

            // Execute entire program at once
            Interpreter interpreter = new Interpreter(input);
            interpreter.execute();

        } catch (IOException e) {
            System.err.println("File Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Runtime Error: " + e.getMessage());
        }
    }
}