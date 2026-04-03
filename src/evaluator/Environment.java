package evaluator;

import java.util.HashMap;
import java.util.Map;

/*
 * Environment stores all variables during execution.
 * Every instruction shares the same Environment instance.
 */
public class Environment {

    // variable name -> value
    private Map<String, Object> variables = new HashMap<>();

    /*
     * Store or update variable value
     */
    public void set(String name, Object value) {
        variables.put(name, value);
    }

    /*
     * Retrieve variable value
     */
    public Object get(String name) {

        if(!variables.containsKey(name)) {
            throw new RuntimeException("Variable not defined: " + name);
        }

        return variables.get(name);
    }
}