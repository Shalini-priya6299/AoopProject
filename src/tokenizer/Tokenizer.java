package tokenizer;
import java.util.*; 

public class Tokenizer {
    private String source;
    private int position;
    private int line;
    
    public Tokenizer(String source) {
        this.source = source;
        this.position = 0;
        this.line = 1;
    }
    
    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();
        
        while(position < source.length()) {
            char curr = source.charAt(position);
            
            // Whitespace Handling
            if(Character.isWhitespace(curr)) {
                if(curr == '\n') {
                    line++;
                    tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                }
                position++;
                continue;
            }
            
            // Words Handling
            if(Character.isLetter(curr)) {
                tokens.add(readWord());
                continue;
            }
            
            // Numbers Handling
            if(Character.isDigit(curr)) {
                tokens.add(readNumber());
                continue;
            }
            
            // Strings Handling
            if(curr == '"') {
                tokens.add(readString());
                continue;
            }
            
            // Symbols Handling
            switch(curr) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", line));
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", line));
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MULTIPLY, "*", line));
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIVIDE, "/", line));
                    break;
                case '>':
                    tokens.add(new Token(TokenType.GREATER, ">", line));
                    break;
                case '<':
                    tokens.add(new Token(TokenType.LESS, "<", line));
                    break;
                case '=':
                    tokens.add(new Token(TokenType.EQUAL, "=", line));
                    break;
                default:
                    throw new RuntimeException("Unexpected character: " + curr);
            }
            
            position++;
        }
        
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }
    
    private Token readWord() {
        int start = position;
        
        while(position < source.length() && Character.isLetter(source.charAt(position))) {
            position++;
        }
        
        String word = source.substring(start, position);
        
        // Special phrase handling
        if (word.equals("is")) {

            int tempPos = position;

            while (tempPos < source.length() && Character.isWhitespace(source.charAt(tempPos))) {
                tempPos++;
            }

            if (tempPos < source.length() && source.startsWith("greater", tempPos)) {
                tempPos += "greater".length();

                while (tempPos < source.length() && Character.isWhitespace(source.charAt(tempPos))) {
                    tempPos++;
                }

                if (tempPos < source.length() && source.startsWith("than", tempPos)) {
                    tempPos += "than".length();

                    if (tempPos == source.length() || Character.isWhitespace(source.charAt(tempPos))) {
                        position = tempPos;
                        return new Token(TokenType.IS_GREATER_THAN, "is greater than", line);
                    }
                }
            }

            tempPos = position;

            while (tempPos < source.length() && Character.isWhitespace(source.charAt(tempPos))) {
                tempPos++;
            }

            if (tempPos < source.length() && source.startsWith("less", tempPos)) {
                tempPos += "less".length();

                while (tempPos < source.length() && Character.isWhitespace(source.charAt(tempPos))) {
                    tempPos++;
                }

                if (tempPos < source.length() && source.startsWith("than", tempPos)) {
                    tempPos += "than".length();

                    if (tempPos == source.length() || Character.isWhitespace(source.charAt(tempPos))) {
                        position = tempPos;
                        return new Token(TokenType.IS_LESS_THAN, "is less than", line);
                    }
                }
            }

            tempPos = position;

            while (tempPos < source.length() && Character.isWhitespace(source.charAt(tempPos))) {
                tempPos++;
            }

            if (tempPos < source.length() && source.startsWith("equal", tempPos)) {
                tempPos += "equal".length();

                while (tempPos < source.length() && Character.isWhitespace(source.charAt(tempPos))) {
                    tempPos++;
                }

                if (tempPos < source.length() && source.startsWith("to", tempPos)) {
                    tempPos += "to".length();

                    if (tempPos == source.length() || Character.isWhitespace(source.charAt(tempPos))) {
                        position = tempPos;
                        return new Token(TokenType.IS_EQUAL, "is equal to", line);
                    }
                }
            }
        }
        
        switch(word) {
            case "let":
                return new Token(TokenType.LET, word, line);
            case "be":
                return new Token(TokenType.BE, word, line);
            case "say":
                return new Token(TokenType.SAY, word, line);
            case "if":
                return new Token(TokenType.IF, word, line);
            case "then":
                return new Token(TokenType.THEN, word, line);
            case "repeat":
                return new Token(TokenType.REPEAT, word, line);
            case "times":
                return new Token(TokenType.TIMES, word, line);
            default:
                return new Token(TokenType.IDENTIFIER, word, line);
        }
    }
    
    private Token readNumber() {
        int start = position;
        
        while(position < source.length() &&
              (Character.isDigit(source.charAt(position)) || source.charAt(position) == '.')) {
            position++;
        }
        
        String number = source.substring(start, position);
        return new Token(TokenType.NUMBER, number, line);
    }
    
    private Token readString() {
        position++;
        int start = position;
        
        while(position < source.length() && source.charAt(position) != '"') {
            position++;
        }
        
        String value = source.substring(start, position);
        position++;
        
        return new Token(TokenType.STRING, value, line);
    }
}