package parser;



public final class StringNode implements Expression {
    private final String value;
    public StringNode(String value) {
        this.value = value;
    }
    @Override
    public Object evaluate(Environment env) {
        return this.value;
    }
}