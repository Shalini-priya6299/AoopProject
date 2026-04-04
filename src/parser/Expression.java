package parser;



public interface Expression {
    Object evaluate(Environment env);
}