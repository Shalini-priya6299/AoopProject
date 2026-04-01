package parser;

import evaluator.Environment;

public interface Expression {
    Object evaluate(Environment env);
}