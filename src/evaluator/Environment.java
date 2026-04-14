package evaluator;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();
    private final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void set(String name, Object value) {
        if (!variables.containsKey(name) && enclosing != null) {
            enclosing.set(name, value);
            return;
        }
        variables.put(name, value);
    }

    public Object get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeException("Variable not defined: " + name);
    }
}