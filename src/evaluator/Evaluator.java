package evaluator;

import parser.Expression;
import java.util.List;

public class Evaluator {
    private Environment env;

    public Evaluator() {
        env = new Environment();
    }

    // MAIN METHOD - parser calls this!
    public void executeProgram(List<Instruction> program) {
        try {
            for (Instruction instr : program) {
                instr.execute(env);
            }
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
        }
    }

    
}