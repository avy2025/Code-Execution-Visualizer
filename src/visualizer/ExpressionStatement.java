package visualizer;

import java.util.HashMap;

/**
 * Handles standalone expressions (e.g., "a + 5;").
 */
public class ExpressionStatement implements Statement {
    private final String expression;

    public ExpressionStatement(String expression) {
        this.expression = expression;
    }

    @Override
    public void execute(HashMap<String, Integer> memory) {
        // Just evaluate the expression.
        ExpressionEvaluator.evaluate(expression, memory);
    }
}
