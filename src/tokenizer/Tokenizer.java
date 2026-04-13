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
        indentStack.push(0); // base indentation level
    }
    
    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();
        
        while(position < source.length()) {
            char curr = source.charAt(position);
            
            // NEWLINE, INDENT, DEDENT handling
          if(curr == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                position++;

                // check blank line FIRST
                int tempPos = position;
                while (tempPos < source.length() && source.charAt(tempPos) == ' ') {
                    tempPos++;
                }

                // if blank line -> skipping
                if (tempPos < source.length() && source.charAt(tempPos) == '\n') {
                    position = tempPos;
                    continue;
                }

                // now count spaces
                int count = 0;

                if (position < source.length() && source.charAt(position) == '\t') {
                    throw new RuntimeException("Tabs not allowed for indentation at line " + line);
                }

                while (position < source.length() && source.charAt(position) == ' ') {
                    count++;
                    position++;
                }

                // validate indentation
                if (count % 4 != 0) {
                    throw new RuntimeException("Indentation must be multiple of 4 spaces at line " + line);
                }

                int prevIndent = indentStack.peek();

                if (count > prevIndent) {
                    indentStack.push(count);
                    tokens.add(new Token(TokenType.INDENT, "", line));
                } 
                else if (count < prevIndent) {
                    while (indentStack.size() > 1 && count < indentStack.peek()) {
                        indentStack.pop();
                        tokens.add(new Token(TokenType.DEDENT, "", line));
                    }
                    if (count != indentStack.peek()) {
                        throw new RuntimeException("Invalid indentation at line " + line);
                    }
                }

                continue;
            }

            // skip spaces
            if(curr == ' ') {
                position++;
                continue;
            }
            
            // Words Handling
            if(Character.isLetter(curr) || curr == '_') {
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
                default:
                    throw new RuntimeException("Unexpected character: '" + curr + "' at line " + line);
            }
            position++;
        }

        // Indents
        while(indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", line));
        }

        tokens.add(new Token(TokenType.EOF, "", line));

        return List.copyOf(tokens); // immutable output
    }
    
    private Token readWord() {
        int start = position;

        while (position < source.length()) {
            char c = source.charAt(position);

            if (Character.isLetterOrDigit(c) || c == '_') {
                position++;
            } else {
                break;
            }
        }
        
        String word = source.substring(start, position);

        // Handle multi-word operators
        if (word.equals("is")) {
            int tempPos;

            // is greater than
            tempPos = skipWhitespace(position);
            if (matchPhrase(tempPos, "greater", "than")) {
                position = moveAfterPhrase(tempPos, "greater", "than");
                return new Token(TokenType.IS_GREATER_THAN, "is greater than", line);
            }

            // is less than
            tempPos = skipWhitespace(position);
            if (matchPhrase(tempPos, "less", "than")) {
                position = moveAfterPhrase(tempPos, "less", "than");
                return new Token(TokenType.IS_LESS_THAN, "is less than", line);
            }

            // is equal to
            tempPos = skipWhitespace(position);
            if (matchPhrase(tempPos, "equal", "to")) {
                position = moveAfterPhrase(tempPos, "equal", "to");
                return new Token(TokenType.IS_EQUAL, "is equal to", line);
            }
        }
        
        // keywords
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
        while (pos < source.length() && source.charAt(pos) == ' ') {
            pos++;
        }
        return pos;
    }

    private boolean matchPhrase(int pos, String word1, String word2) {
        if (!source.startsWith(word1, pos)) return false;

        int end1 = pos + word1.length();
        if (end1 < source.length() && source.charAt(end1) != ' ') return false;

        pos += word1.length();
        pos = skipWhitespace(pos);

        if (!source.startsWith(word2, pos)) return false;

        int end2 = pos + word2.length();
        if (end2 < source.length() && source.charAt(end2) != ' ') return false;
        pos += word2.length();
        return pos == source.length() || !Character.isLetterOrDigit(source.charAt(pos));
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
            } 
            else if(c == '.' && !hasDot) {
                hasDot = true;
                position++;
            } 
            else {
                break;
            }
        }
        String number = source.substring(start, position);
        return new Token(TokenType.NUMBER, number, line);
    }
    
    private Token readString() {
        position++; // skip opening "

        StringBuilder sb = new StringBuilder();

        while (position < source.length()) {
            char c = source.charAt(position);

            if (c == '\\') {
                // handle escape sequences
                position++;

                if (position >= source.length()) {
                    throw new RuntimeException("Invalid escape at line " + line);
                }

                char next = source.charAt(position);

                switch (next) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    default:
                        throw new RuntimeException("Invalid escape: \\" + next + " at line " + line);
                }

                position++;
            } 
            else if (c == '"') {
                position++; // closing quote
                return new Token(TokenType.STRING, sb.toString(), line);
            } 
            else {
                sb.append(c);
                position++;
            }
        }

        throw new RuntimeException("Unterminated string at line " + line);
    }
}