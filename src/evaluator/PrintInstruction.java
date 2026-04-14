package evaluator;

import parser.Expression;

/*
 * Handles printing values
 * Example:
 * say x
 * say "hello"
 */
public class PrintInstruction implements Instruction {

    private Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {

        Object value = expression.evaluate(env);

        System.out.println(value);
    }
}