package tokenizer;
import java.util.List;

public class TokenizerTest {
    public static void main(String[] args){
        String input = "x is greater\nthan y";
        Tokenizer t1 = new Tokenizer(input);
        List<Token> tokens = t1.tokenize();
        for(Token t: tokens){
            System.out.println(t);
        }
    }
}
