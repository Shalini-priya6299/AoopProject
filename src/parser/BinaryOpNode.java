package parser;



public class BinaryOpNode implements Expression{
    private final Expression left;
    private final Expression right;
    private final String op;
    public BinaryOpNode(Expression left, String op, Expression right) {
        this.left = left;
        this.right = right;
        this.op = op;
    }
    @Override
    public Object evaluate(Environment env) {
        Object leftVal = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        //Handling String operations
        if (leftVal instanceof String && rightVal instanceof String) {
            if("==".equals(op)) {
            return leftVal.equals(rightVal);
            }else if("+".equals(op)) {
                return (String)leftVal + (String)rightVal;}
            else{
                throw new RuntimeException(
                    "Unsupported String operation required: [+ , ==]  got "+op);
            }
        }
        //Safety check before downcast
        if (!(leftVal instanceof Double) || !(rightVal instanceof Double)) {
            throw new RuntimeException(
                    "Invalid input type. Required Double , got: " + leftVal + " and " + rightVal
            );
        }
        double l = (Double) leftVal;
        double r = (Double) rightVal;

        switch (op) {
            case "+": return l + r;
            case "-": return l - r;
            case "*": return l * r;
            case "/":
                if (r == 0) throw new RuntimeException("Division by zero");
                return l / r;
            case ">":  return l > r;
            case "<":  return l < r;
            case "==": return l==r; // Floating point Issue for values like 0.1+0.2==0.3
            default: throw new RuntimeException("Unknown op: " + op);
        }
    }


}