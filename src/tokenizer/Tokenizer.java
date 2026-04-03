package tokenizer;
import java.util.*;

public class Tokenizer {
    private String source;
    private int position;
    private int line;
    private Deque<Integer> indentStack = new ArrayDeque<>();
    
    public Tokenizer(String source) {
        this.source = source;
        this.position = 0;
        this.line = 1;
        indentStack.push(0);
    }
    
    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();
        
        while(position < source.length()) {
            char curr = source.charAt(position);
            
           // NEWLINE + INDENT/DEDENT handling
            if(curr == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                position++;

                int count = 0;

                // count spaces after newline
                while(position < source.length() && source.charAt(position) == ' ') {
                    count++;
                    position++;
                }
                int prevIndent = indentStack.peek();
                if(count > prevIndent) {
                    indentStack.push(count);
                    tokens.add(new Token(TokenType.INDENT, "", line));
                } 
                else if(count < prevIndent) {
                    while(indentStack.size() > 1 && count < indentStack.peek()) {
                        indentStack.pop();
                        tokens.add(new Token(TokenType.DEDENT, "", line));
                    }
                    if(count != indentStack.peek()) {
                        throw new RuntimeException("Invalid indentation at line " + line);
                    }
                }
                continue;
            }
            if(Character.isWhitespace(curr)) {
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
                    throw new RuntimeException( "Unexpected character: '" + curr + "' at line " + line);
            }
            position++;
        }

        while(indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", line));
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return List.copyOf(tokens);
    }
    
    private Token readWord() {
        int start = position;
        while(position < source.length() && Character.isLetter(source.charAt(position))) {
            position++;
        }
        
        String word = source.substring(start, position);
        if (word.equals("is")) {
            int tempPos = skipWhitespace(position);

            // is greater than Handling
            if (matchPhrase(tempPos, "greater", "than")) {
                position = moveAfterPhrase(tempPos, "greater", "than");
                return new Token(TokenType.IS_GREATER_THAN, "is greater than", line);
            }
            tempPos = skipWhitespace(position);

            // Is less than Handling
            if (matchPhrase(tempPos, "less", "than")) {
                position = moveAfterPhrase(tempPos, "less", "than");
                return new Token(TokenType.IS_LESS_THAN, "is less than", line);
            }

            // Is equal Handling
            tempPos = skipWhitespace(position);
            if (matchPhrase(tempPos, "equal", "to")) {
                position = moveAfterPhrase(tempPos, "equal", "to");
                return new Token(TokenType.IS_EQUAL, "is equal to", line);
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
    
    private int skipWhitespace(int pos) {
        while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private boolean matchPhrase(int pos, String word1, String word2) {
        if (!source.startsWith(word1, pos)) return false;
        pos += word1.length();
        pos = skipWhitespace(pos);
        if (!source.startsWith(word2, pos)) return false;
        pos += word2.length();

        return pos == source.length() || Character.isWhitespace(source.charAt(pos));
    }

    private int moveAfterPhrase(int pos, String word1, String word2) {
        pos += word1.length();
        pos = skipWhitespace(pos);
        pos += word2.length();
        return pos;
    }
    
    private Token readNumber() {
        int start = position;
        boolean hasDot = false;
        
        while(position < source.length()) {
            char c = source.charAt(position);
            
            if(Character.isDigit(c)) {
                position++;
            } else if(c == '.' && !hasDot) {
                hasDot = true;
                position++;
            } else {
                break;
            }
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
        
        if(position >= source.length()) {
            throw new RuntimeException("Unterminated string at line " + line);
        }
        
        String value = source.substring(start, position);
        position++;
        
        return new Token(TokenType.STRING, value, line);
    }
}