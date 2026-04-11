package visualizer;

import java.util.HashMap;

/**
 * Handles variable assignments (e.g., "a = 10;").
 */
public class AssignmentStatement implements Statement {
    private final String varName;
    private final String expression;

    public AssignmentStatement(String varName, String expression) {
        this.varName = varName;
        this.expression = expression;
    }

    @Override
    public void execute(HashMap<String, Integer> memory) {
        if (!memory.containsKey(varName)) {
            throw new RuntimeException("Undefined variable: \"" + varName + "\"");
        }
        int value = ExpressionEvaluator.evaluate(expression, memory);
        memory.put(varName, value);
    }
}
