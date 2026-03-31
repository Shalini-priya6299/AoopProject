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
		while(position< source.length()) {
			char curr = source.charAt(position);
			if(Character.isWhitespace(curr)) {
				if(curr == '\n') {
					line++;
					tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
				}
				position++;
				continue;
			}

			// Handles letter
			if(Character.isLetter(curr)) {
				tokens.add(readWord());
				continue;
			}

			// Handles digit
			if(Character.isDigit(curr)) {
				tokens.add(readNumber());
				continue;
			}
			
			if(curr =='"') {
				tokens.add(readString());
				continue;
			}
			
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
		if (word.equals("is") && source.startsWith(" greater than", position)) {
		    position += " greater than".length();
		    return new Token(TokenType.IS_GREATER_THAN, "is greater than", line);
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
		while(position< source.length() && (Character.isDigit(source.charAt(position)) || source.charAt(position) == '.')) {
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
		String value= source.substring(start, position);
		position++;
		return new Token(TokenType.STRING, value, line);
	}

}
