import evaluator.*;
import parser.*;

public class TestInterpreter {

    public static void main(String[] args) {

        Evaluator evaluator = new Evaluator();

        Expression expr =
            new BinaryOpNode(
                new NumberNode(5),
                "+",
                new NumberNode(3)
            );

        Object result = evaluator.evaluate(expr);

        System.out.println("Test Result = " + result);
    }
}