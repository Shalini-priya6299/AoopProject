package evaluator;

/*
 * Every executable statement implements this interface.
 */
public interface Instruction {

    /*
     * Execute instruction using the shared environment.
     */
    void execute(Environment env);
}