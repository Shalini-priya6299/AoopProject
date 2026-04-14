package evaluator;

import parser.Expression;
import java.util.List;

/*
 * Handles conditional execution
 */
public class IfInstruction implements Instruction {

    private Expression condition;
    private List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        Object result = condition.evaluate(env);

        if (result instanceof Boolean && (Boolean) result) {
            Environment localEnv = new Environment(env);  // NEW: Simple local scope
            try {
                for (Instruction instr : body) {
                    instr.execute(localEnv);
                }
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }
}