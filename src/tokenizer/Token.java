package tokenizer;

public record Token(TokenType type, String value, int line) {
    public String toString() {
        return type + "(" + value + ")";
    }
}