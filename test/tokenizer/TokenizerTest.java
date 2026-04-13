package tokenizer;
import java.util.List;

public class TokenizerTest {
    public static void main(String[] args){
        String input = "if x_val is greater than 5\n    say \"hello \\\"world\\\"\"";
        System.out.println(input);
        Tokenizer t1 = new Tokenizer(input);
        List<Token> tokens = t1.tokenize();
        for(Token t: tokens){
            System.out.println(t);
        }
        
    }
}
