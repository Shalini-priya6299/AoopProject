package tokenizer;
import java.util.List;

public class TokenizerTest {
    public static void main(String[] args){
        String input = "repeat 4 times\n" +
               "    say \"hello\"\n" +
               "\n" +
               "    say 5";;
        System.out.println(input);
        Tokenizer t1 = new Tokenizer(input);
        List<Token> tokens = t1.tokenize();
        for(Token t: tokens){
            System.out.println(t);
        }
        
    }
}
