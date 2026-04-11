package visualizer;

import java.util.HashMap;

/**
 * Handles variable declarations (e.g., "int a = 5;").
 */
public class DeclarationStatement implements Statement {
    private final String varName;
    private final String expression;

    public DeclarationStatement(String varName, String expression) {
        this.varName = varName;
        this.expression = expression;
    }

    @Override
    public void execute(HashMap<String, Integer> memory) {
        if (memory.containsKey(varName)) {
            throw new RuntimeException("Variable already declared: \"" + varName + "\". Use assignment instead.");
        }
        
        // If there's an assignment part, evaluate it
        int value = 0; // Default or null equivalent in this simple engine
        if (expression != null && !expression.isBlank()) {
            value = ExpressionEvaluator.evaluate(expression, memory);
        }
        memory.put(varName, value);
    }
}
