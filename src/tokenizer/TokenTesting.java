package tokenizer;
import java.util.*;

public class TokenTesting {
    public static void main(String[] args){
        Tokenizer t1 = new Tokenizer("let x be 10\n     let y be 10");
        List<Token> result = t1.tokenize();
        for(Token t : result){
            System.out.println(t.getType() + " : " + t.getValue() + " : " + t.getLine());
        }
    }
}