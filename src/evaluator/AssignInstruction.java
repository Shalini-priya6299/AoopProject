package evaluator;

import parser.Expression;

/*
 * Handles variable assignment
 * Example:
 * let x be 10
 */
public class AssignInstruction implements Instruction {

    private String variableName;
    private Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {

        // evaluate expression
        Object value = expression.evaluate(env);

        // store variable
        env.set(variableName, value);
    }
}