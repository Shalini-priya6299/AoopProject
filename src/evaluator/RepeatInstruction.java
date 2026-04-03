package evaluator;

import java.util.List;

/*
 * Handles loops
 * Example:
 * repeat 4 times
 */
public class RepeatInstruction implements Instruction {

    private int count;
    private List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        this.count = count;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {

        for(int i = 0; i < count; i++) {

            for(Instruction instr : body) {
                instr.execute(env);
            }

        }
    }
}