package src;

import tokenizer.Tokenizer;
import tokenizer.Token;
import parser.Parser;  
import evaluator.Evaluator;
import evaluator.Instruction;
import java.util.List;

public class Interpreter {
    private String sourceCode;
    private List<Instruction> program;
    
    public Interpreter(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    
    public void execute() {
        // 1. TOKENIZE
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();
        
        // 2. PARSE (tokens → Instructions)
        Parser parser = new Parser(tokens);
        program = parser.parse();  // Returns List<Instruction>
        
        // 3. EVALUATE
        Evaluator evaluator = new Evaluator();
        evaluator.executeProgram(program);
    }
}