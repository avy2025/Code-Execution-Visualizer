package visualizer;

import java.util.Map;

/**
 * Utility class to evaluate arithmetic expressions.
 */
public class ExpressionEvaluator {

    /**
     * Evaluates a simple arithmetic expression.
     */
    public static int evaluate(String expr, Map<String, Integer> memory) {
        expr = expr.strip();

        // Try each operator (order matters — process left to right)
        for (char op : new char[]{'+', '-', '*', '/'}) {
            int opIdx = findOperatorIndex(expr, op);
            if (opIdx != -1) {
                String leftToken  = expr.substring(0, opIdx).strip();
                String rightToken = expr.substring(opIdx + 1).strip();

                int left  = resolveOperand(leftToken, memory);
                int right = resolveOperand(rightToken, memory);

                return applyOperator(op, left, right, expr);
            }
        }

        // No operator found — must be a single operand
        return resolveOperand(expr, memory);
    }

    private static int findOperatorIndex(String expr, char op) {
        int start = (op == '-' || op == '+') ? 1 : 0;
        for (int i = start; i < expr.length(); i++) {
            if (expr.charAt(i) == op) {
                return i;
            }
        }
        return -1;
    }

    private static int resolveOperand(String token, Map<String, Integer> memory) {
        token = token.strip();
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {}

        if (!memory.containsKey(token)) {
            throw new RuntimeException("Undefined variable: \"" + token + "\"");
        }
        return memory.get(token);
    }

    private static int applyOperator(char op, int left, int right, String exprContext) {
        switch (op) {
            case '+': return left + right;
            case '-': return left - right;
            case '*': return left * right;
            case '/':
                if (right == 0) throw new RuntimeException("Division by zero in: \"" + exprContext + "\"");
                return left / right;
            default:
                throw new RuntimeException("Unknown operator: " + op);
        }
    }
}
