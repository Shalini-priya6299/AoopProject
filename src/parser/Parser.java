package parser;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the SPEEK language.
 *
 * Reads the flat List<Token> produced by the Tokenizer and builds
 * a structured List<Instruction> that the Evaluator can execute.
 *
 * The parser works in one pass — it reads tokens left to right,
 * never going back. Each token is consumed exactly once.
 *
 * Pipeline position:
 *   Tokenizer → [Parser] → Evaluator
 *
 * Input  : List<Token>       (immutable, from Tokenizer)
 * Output : List<Instruction> (immutable, passed to Evaluator)
 */
public class Parser {

    private final List<Token> tokens;  // immutable token list from Tokenizer
    private int current = 0;           // index of token we are currently looking at

    /**
     * Creates a new Parser with a defensive immutable copy of the token list.
     *
     * List.copyOf() is used here even if the tokenizer already returned an
     * immutable list — this protects the parser's internal state regardless
     * of how the token list was constructed or modified before being passed in.
     */
    public Parser(List<Token> tokens) {
        this.tokens = List.copyOf(tokens);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAIN PARSE LOOP
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Entry point of the parser. Walks through all tokens and builds
     * a list of top-level Instructions.
     *
     * Think of this as reading a recipe line by line —
     * each line becomes one Instruction object.
     *
     * Blank lines between instructions are silently skipped.
     * Parsing stops when the EOF token is reached.
     *
     * Example SPEEK program and what this method produces:
     *   let x be 10        → AssignInstruction("x", NumberNode(10))
     *   let y be 3         → AssignInstruction("y", NumberNode(3))
     *   say x              → PrintInstruction(VariableNode("x"))
     *
     * @return immutable list of Instructions ready for the Evaluator
     */
    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();

        while (!isAtEnd()) {
            skipNewlines();       // skip blank lines between instructions
            if (isAtEnd()) break; // file may end after trailing blank lines
            instructions.add(parseInstruction());
        }

        return List.copyOf(instructions); // immutable — evaluator only reads this
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INSTRUCTION PARSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Dispatches to the correct instruction parser based on the current token.
     *
     * In SPEEK every instruction starts with a keyword:
     *   let    → assignment  → parseAssign()
     *   say    → print       → parsePrint()
     *   if     → conditional → parseIf()
     *   repeat → loop        → parseRepeat()
     *
     * Any other token at this position is a syntax error in the source program.
     * The error message includes the unexpected token and its line number
     * so the user knows exactly where their mistake is.
     *
     * @return the parsed Instruction
     * @throws RuntimeException if token does not start a valid instruction
     */
    private Instruction parseInstruction() {
        Token token = peek(); // look ahead without consuming

        switch (token.type()) {
            case LET:    return parseAssign();
            case SAY:    return parsePrint();
            case IF:     return parseIf();
            case REPEAT: return parseRepeat();
            default:
                throw new RuntimeException(
                        "Unexpected token '" + token.value() +
                                "' at line " + token.line() +
                                ". Expected: let, say, if, or repeat."
                );
        }
    }

    /**
     * Parses an assignment instruction.
     *
     * SPEEK syntax:
     *   let x be 10
     *   let result be x + y * 2
     *
     * Token pattern:
     *   LET → IDENTIFIER → BE → expression → NEWLINE/EOF
     *
     * The right hand side is a full expression — it can be a single
     * value or a complex arithmetic expression. parseExpression() handles
     * operator precedence automatically through the three-level call chain.
     *
     * Example:
     *   "let result be x + y * 2"
     *   name  = "result"
     *   value = BinaryOpNode(
     *               VariableNode("x"),
     *               "+",
     *               BinaryOpNode(VariableNode("y"), "*", NumberNode(2))
     *           )
     *
     * @return AssignInstruction holding the variable name and value expression
     * @throws RuntimeException if token pattern does not match
     */
    private Instruction parseAssign() {
        consume(TokenType.LET);                     // eat 'let'
        Token name = consume(TokenType.IDENTIFIER); // eat variable name e.g. 'x'
        consume(TokenType.BE);                      // eat 'be'
        Expression value = parseExpression();       // parse right hand side
        consumeNewline();                           // eat end of line (EOF is also fine)
        return new AssignInstruction(name.value(), value);
    }

    /**
     * Parses a print instruction.
     *
     * SPEEK syntax:
     *   say x
     *   say "Hello from SPEEK"
     *   say result
     *
     * Token pattern:
     *   SAY → expression → NEWLINE/EOF
     *
     * The expression can be a variable, a string literal, or any arithmetic
     * expression. Whatever evaluate() returns on it gets printed to stdout.
     *
     * Examples:
     *   "say result"              → PrintInstruction(VariableNode("result"))
     *   "say "Hello from SPEEK"" → PrintInstruction(StringNode("Hello from SPEEK"))
     *   "say x + 1"              → PrintInstruction(BinaryOpNode(x + 1))
     *
     * EOF after this instruction is valid — the file can end after a say.
     *
     * @return PrintInstruction holding the expression to print
     * @throws RuntimeException if token pattern does not match
     */
    private Instruction parsePrint() {
        consume(TokenType.SAY);               // eat 'say'
        Expression value = parseExpression(); // parse what to print
        consumeNewline();                     // eat end of line (EOF is also fine)
        return new PrintInstruction(value);
    }

    /**
     * Parses a conditional instruction.
     *
     * SPEEK syntax:
     *   if score is greater than 50 then
     *       say "Pass"
     *
     * Token pattern:
     *   IF → expression → comparison_op → expression → THEN → NEWLINE → INDENT → body → DEDENT
     *
     * The condition is always a comparison between two expressions.
     * parseComparisonOp() converts SPEEK's natural language operators
     * (IS_GREATER_THAN, IS_LESS_THAN, IS_EQUAL) into symbol strings
     * (">" "<" "==") that BinaryOpNode understands.
     *
     * The body is an indented block — consumeNewlineForBlock() validates
     * that a newline and body actually exist after THEN. EOF here is an
     * error because the body is mandatory.
     *
     * Example:
     *   "if score is greater than 50 then"
     *   left      = VariableNode("score")
     *   op        = ">"   ← parseComparisonOp converts IS_GREATER_THAN
     *   right     = NumberNode(50)
     *   condition = BinaryOpNode(VariableNode("score"), ">", NumberNode(50))
     *   body      = [PrintInstruction(StringNode("Pass"))]
     *
     * @return IfInstruction holding the condition and body instructions
     * @throws RuntimeException if token pattern does not match or body is missing
     */
    private Instruction parseIf() {
        consume(TokenType.IF);                // eat 'if'
        Expression left = parseExpression(); // parse left side of comparison e.g. score
        String op = parseComparisonOp();     // convert IS_GREATER_THAN → ">" etc.
        Expression right = parseExpression(); // parse right side of comparison e.g. 50
        consume(TokenType.THEN);             // eat 'then'
        consumeNewlineForBlock();            // validate newline + safety net for missing body

        Expression condition = new BinaryOpNode(left, op, right);
        List<Instruction> body = parseBody(); // parse the indented block below
        return new IfInstruction(condition, body);
    }

    /**
     * Parses a repeat (loop) instruction.
     *
     * SPEEK syntax:
     *   repeat 4 times
     *       say i
     *       let i be i + 1
     *
     * Token pattern:
     *   REPEAT → NUMBER → TIMES → NEWLINE → INDENT → body → DEDENT
     *
     * The repeat count is always a literal number token — variables and
     * expressions are not supported as the repeat count. The number is
     * parsed as Double first (consistent with the pipeline) then cast
     * to int since a fractional loop count makes no sense.
     *
     * The body is an indented block — consumeNewlineForBlock() validates
     * that a newline and body actually exist after TIMES. EOF here is an
     * error because the body is mandatory.
     *
     * Example:
     *   "repeat 4 times"
     *   times = 4
     *   body  = [PrintInstruction(VariableNode("i")),
     *            AssignInstruction("i", BinaryOpNode(i + 1))]
     *
     * @return RepeatInstruction holding the count and body instructions
     * @throws RuntimeException if token pattern does not match or body is missing
     */
    private Instruction parseRepeat() {
        consume(TokenType.REPEAT);                   // eat 'repeat'
        Token count = consume(TokenType.NUMBER);     // eat loop count e.g. 4
        consume(TokenType.TIMES);                    // eat 'times'
        consumeNewlineForBlock();                    // validate newline + safety net for missing body

        // parse as Double first (all numbers in pipeline are Double)
        // then cast to int — fractional repeat count is meaningless
        int times = (int) Double.parseDouble(count.value());
        List<Instruction> body = parseBody();        // parse the indented block below
        return new RepeatInstruction(times, body);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BODY PARSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Parses an indented block of instructions inside an if or repeat.
     *
     * SPEEK uses indentation to delimit blocks. The Tokenizer converts
     * indentation changes into INDENT and DEDENT tokens, so this method
     * simply consumes INDENT at the start and DEDENT at the end.
     *
     * Nested blocks work automatically — when parseInstruction() encounters
     * an IF or REPEAT inside the body, it calls parseIf() or parseRepeat()
     * which each call parseBody() recursively. Each recursive call consumes
     * its own INDENT/DEDENT pair, so nesting depth is handled naturally.
     *
     * The isAtEnd() check in the while condition is a safety net —
     * if the tokenizer somehow forgot to emit a DEDENT at end of file,
     * the loop would run forever without it.
     *
     * Example — body of "repeat 4 times":
     *   INDENT
     *   SAY IDENTIFIER(i) NEWLINE          ← instruction 1
     *   LET IDENTIFIER(i) BE ... NEWLINE   ← instruction 2
     *   DEDENT
     *
     * @return immutable list of Instructions in this block
     * @throws RuntimeException if INDENT or DEDENT tokens are missing
     */
    private List<Instruction> parseBody() {
        List<Instruction> body = new ArrayList<>();

        consume(TokenType.INDENT);  // block must open with INDENT

        while (!check(TokenType.DEDENT) && !isAtEnd()) {
            skipNewlines();                        // skip blank lines inside block
            if (check(TokenType.DEDENT)) break;   // blank lines may land on DEDENT
            body.add(parseInstruction());          // recursive call handles nested blocks
        }

        consume(TokenType.DEDENT);  // block must close with DEDENT

        return List.copyOf(body);   // immutable — evaluator only reads this
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPRESSION PARSING — OPERATOR PRECEDENCE CHAIN
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handles + and - operators (LOWEST precedence).
     *
     * This is the top of the three-level precedence chain:
     *   parseExpression()  → handles + and -   (lowest)
     *       └── parseTerm()    → handles * and /   (higher)
     *               └── parsePrimary() → handles single values (highest)
     *
     * Why does calling parseTerm() first give * and / higher precedence?
     * Because parseTerm() fully resolves all * and / before returning —
     * so by the time parseExpression() sees the + or -, the right-hand
     * side already contains a fully evaluated multiplication subtree.
     *
     * The while loop handles chained operators like x + y + z:
     *   iteration 1: left = x,          op = "+", right = y   → left = (x+y)
     *   iteration 2: left = (x+y),      op = "+", right = z   → left = ((x+y)+z)
     *
     * Tree for "x + y * 2":
     *        +
     *       / \
     *      x   *        ← * is deeper, so it evaluates first
     *         / \
     *        y   2
     *
     * @return Expression tree for the full expression
     */
    private Expression parseExpression() {
        Expression left = parseTerm(); // start by resolving higher-precedence * and /

        // keep consuming + and - building left-associative tree
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = advance().value();  // consume operator, get its symbol
            Expression right = parseTerm(); // right side may contain * or /
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    /**
     * Handles * and / operators (HIGHER precedence than + and -).
     *
     * Middle level of the three-level precedence chain.
     * Called by parseExpression() — always resolves fully before returning,
     * which is what gives * and / their higher precedence.
     *
     * The while loop handles chained operators like x * y * z:
     *   iteration 1: left = x,     op = "*", right = y  → left = (x*y)
     *   iteration 2: left = (x*y), op = "*", right = z  → left = ((x*y)*z)
     *
     * Example — "y * 2":
     *   left  = parsePrimary() → VariableNode("y")
     *   op    = "*"
     *   right = parsePrimary() → NumberNode(2)
     *   returns BinaryOpNode(y * 2)
     *
     * @return Expression tree for this term
     */
    private Expression parseTerm() {
        Expression left = parsePrimary(); // start with a single atomic value

        // keep consuming * and / building left-associative tree
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            String op = advance().value();     // consume operator, get its symbol
            Expression right = parsePrimary(); // right side is always a single value
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    /**
     * Handles a single atomic value — a number, string, or variable (HIGHEST precedence).
     *
     * Bottom of the three-level precedence chain. Does not call any
     * other parse method — it just consumes exactly one token and
     * wraps it in the appropriate node.
     *
     * This is where the token stream meets the expression tree:
     *   NUMBER token     → NumberNode  (value stored as double)
     *   STRING token     → StringNode  (quotes already stripped by Tokenizer)
     *   IDENTIFIER token → VariableNode (value looked up in Environment at runtime)
     *
     * Examples:
     *   10        → NumberNode(10.0)
     *   3.14      → NumberNode(3.14)
     *   "Sitare"  → StringNode("Sitare")
     *   score     → VariableNode("score")
     *
     * @return a leaf node in the expression tree
     * @throws RuntimeException if current token is not a valid value
     */
    private Expression parsePrimary() {
        if (check(TokenType.NUMBER)) {
            // tokenizer stores numbers as strings — parse to double here
            // all numbers in the pipeline are Double, never Integer
            return new NumberNode(Double.parseDouble(advance().value()));
        }
        if (check(TokenType.STRING)) {
            // tokenizer already stripped surrounding quotes from string value
            return new StringNode(advance().value());
        }
        if (check(TokenType.IDENTIFIER)) {
            // store just the name — Environment.get() resolves the value at runtime
            return new VariableNode(advance().value());
        }
        throw new RuntimeException(
                "Expected a value (number, string, or variable name) " +
                        "but got '" + peek().value() + "' at line " + peek().line()
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // COMPARISON OPERATOR PARSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Converts SPEEK's natural language comparison tokens into operator symbols.
     *
     * This is the only SPEEK-specific logic in the entire Parser.
     * After this conversion, BinaryOpNode only ever sees ">", "<", "=="
     * and never has to deal with SPEEK-specific phrasing.
     *
     * The Tokenizer already grouped multi-word phrases into single tokens:
     *   "is greater than" → IS_GREATER_THAN token → ">"
     *   "is less than"    → IS_LESS_THAN token    → "<"
     *   "is equal to"     → IS_EQUAL token        → "=="
     *
     * This clean separation means BinaryOpNode.evaluate() is completely
     * language-agnostic — it would work identically for ZARA, BLOOP, or CALC.
     *
     * @return operator symbol string used by BinaryOpNode
     * @throws RuntimeException if current token is not a comparison operator
     */
    private String parseComparisonOp() {
        if (check(TokenType.IS_GREATER_THAN)) { advance(); return ">"; }
        if (check(TokenType.IS_LESS_THAN))    { advance(); return "<"; }
        if (check(TokenType.IS_EQUAL))        { advance(); return "=="; }
        throw new RuntimeException(
                "Expected a comparison operator " +
                        "(is greater than / is less than / is equal to) " +
                        "at line " + peek().line()
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns the current token WITHOUT consuming it or advancing current.
     *
     * Safe against out-of-bounds — returns a virtual EOF token if current
     * overshoots the list. This means isAtEnd() and all check() calls are
     * always safe regardless of loop logic.
     *
     * @return current token, or virtual EOF token if past end of list
     */
    private Token peek() {
        if (current >= tokens.size()) {
            return new Token(TokenType.EOF, "", -1); // virtual safety EOF
        }
        return tokens.get(current);
    }

    /**
     * Consumes the current token and advances to the next one.
     * Returns the token that was consumed, not the next one.
     *
     * Captures the current token BEFORE advancing — this is important
     * because at EOF, current must not advance past the EOF token.
     *
     * Use this when you need the token's value after consuming it
     * e.g. consuming an IDENTIFIER to get the variable name.
     *
     * @return the token that was consumed
     */
    private Token advance() {
        Token currentToken = peek();   // capture before advancing
        if (!isAtEnd()) current++;     // only advance if not already at EOF
        return currentToken;           // return what we consumed, not what's next
    }

    /**
     * Consumes the current token only if it matches the expected type.
     * Throws a descriptive error if the token does not match.
     *
     * Use this for tokens that are grammatically required — where anything
     * other than the expected token is a syntax error in the source program.
     *
     * Example errors:
     *   "let 10 be x"   → "Expected IDENTIFIER but got '10' at line 1"
     *   "let x have 10" → "Expected BE but got 'have' at line 1"
     *
     * @param type the expected TokenType
     * @return the consumed token
     * @throws RuntimeException with descriptive message if type does not match
     */
    private Token consume(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException(
                "Expected " + type +
                        " but got '" + peek().value() +
                        "' at line " + peek().line()
        );
    }

    /**
     * Checks if the current token matches the given type WITHOUT consuming it.
     *
     * Always returns false at EOF — prevents accidentally matching
     * against the EOF sentinel token in normal token checks.
     *
     * @param type the TokenType to check against
     * @return true if current token matches, false if not or at EOF
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    /**
     * Skips over consecutive NEWLINE tokens without consuming anything else.
     *
     * Used in two places:
     *   1. Between top-level instructions — blank lines in the source file
     *   2. Inside blocks — blank lines between instructions in a body
     *
     * Does nothing if the current token is not a NEWLINE.
     */
    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) advance();
    }

    /**
     * Consumes the NEWLINE at the end of a SAY or LET instruction.
     *
     * EOF is acceptable here — the file is allowed to end after
     * a say or let instruction with no trailing newline.
     *
     * Used by: parseAssign(), parsePrint()
     *
     * @throws RuntimeException if current token is neither NEWLINE nor EOF
     */
    private void consumeNewline() {
        if (!check(TokenType.NEWLINE) && !isAtEnd()) {
            throw new RuntimeException(
                    "Expected end of line at line " + peek().line() +
                            " but got '" + peek().value() + "'"
            );
        }
        skipNewlines(); // consume all trailing newlines
    }

    /**
     * Consumes the NEWLINE after THEN or TIMES where a body block is mandatory.
     *
     * Unlike consumeNewline(), EOF is NOT acceptable here — if the file ends
     * after 'then' or 'times', the required body block is missing and that
     * is a syntax error.
     *
     * Two EOF checks are needed:
     *   1. Before consuming — catches "if x is greater than 5 then EOF"
     *   2. After consuming  — catches "if x is greater than 5 then\nEOF"
     *      (newline present but body never written)
     *
     * Used by: parseIf(), parseRepeat()
     *
     * @throws RuntimeException if EOF is reached before or after the newline,
     *                          or if no newline is present at all
     */
    private void consumeNewlineForBlock() {
        // case 1 — EOF immediately after THEN/TIMES
        if (isAtEnd()) {
            throw new RuntimeException(
                    "Unexpected end of file — expected a block body"
            );
        }
        // case 2 — something other than NEWLINE after THEN/TIMES
        if (!check(TokenType.NEWLINE)) {
            throw new RuntimeException(
                    "Expected new line at line " + peek().line() +
                            " but got '" + peek().value() + "'"
            );
        }
        skipNewlines(); // consume all newlines including blank lines

        // case 3 — newline was there but file ended before body was written
        if (isAtEnd()) {
            throw new RuntimeException(
                    "Unexpected end of file — expected a block body after newline"
            );
        }
    }

    /**
     * Returns true if the parser has reached the end of the token stream.
     *
     * The Tokenizer always appends an EOF token as the last token —
     * this sentinel means we never need to check array bounds here.
     * peek() additionally guards against out-of-bounds with a virtual EOF,
     * so this method is always safe to call.
     *
     * @return true if current token is EOF
     */
    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }
}