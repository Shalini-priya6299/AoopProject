package tokenizer;
import java.util.*;

public class TokenizerTest {
    public static void main(String[] args){
        String input = "repeat 2 times\n    say x\nsay done";
        Tokenizer t1 = new Tokenizer(input);
        List<Token> tokens = t1.tokenize();
        for(Token t: tokens){
            System.out.println(t);
        }
    }
}
