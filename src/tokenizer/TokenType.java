package tokenizer;

public enum TokenType {
	// KeyWords
	LET, BE, SAY, IF, THEN, REPEAT, TIMES, 
	
	// Comparisons
	IS_GREATER_THAN, IS_LESS_THAN, IS_EQUAL,

	// Literals
	NUMBER, STRING, IDENTIFIER, 

	// Operators
	PLUS, MINUS, MULTIPLY, DIVIDE,
	
	INDENT, DEDENT, NEWLINE, EOF
}
