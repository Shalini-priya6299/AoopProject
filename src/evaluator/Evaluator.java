package evaluator;

import parser.Expression;

public class Evaluator {

    private Environment env;

    public Evaluator() {
        env = new Environment();
    }

    public Object evaluate(Expression expr) {
        return expr.evaluate(env);
    }

    public Environment getEnvironment() {
        return env;
    }
}