package visualizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ExecutionEngine simulates step-by-step execution of parsed Java-like code lines.
 *
 * Supported statement forms:
 *   int a = 5;
 *   int b = 10;
 *   int c = a + b;       // variable on RHS
 *   a = a * 2;           // reassignment
 *   int d = a - b / 2;   // chained (left-to-right, one operator per expression)
 *
 * Supported operators: +  -  *  /
 * Variable store      : HashMap<String, Integer>
 */
public class ExecutionEngine {

    // Primary variable store  –  name → Integer value
    private HashMap<String, Integer> variableStore;

    // Step counter for display
    private int stepCount;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public ExecutionEngine() {
        this.variableStore = new LinkedHashMap<>();   // LinkedHashMap keeps insertion order for printing
        this.stepCount = 0;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Executes each line in sequence.
     * After every line, the current variable state is printed to stdout.
     *
     * @param lines  clean list of executable code lines from CodeParser
     */
    public void execute(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            System.out.println("[ExecutionEngine] Nothing to execute.");
            return;
        }

        System.out.println("[ExecutionEngine] Starting execution of " + lines.size() + " line(s)...");
        printDivider();

        for (String line : lines) {
            stepCount++;
            System.out.println("Step " + stepCount + " | Line : " + line);

            try {
                processLine(line);
            } catch (ExecutionException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }

            printVariableState();
            printDivider();
        }

        System.out.println("[ExecutionEngine] Execution complete. "
                + variableStore.size() + " variable(s) in memory.");
    }

    // -----------------------------------------------------------------------
    // Core parsing logic
    // -----------------------------------------------------------------------

    /**
     * Determines the type of statement and dispatches to the right handler.
     *
     * Grammar handled:
     *   [type] varName = expression ;
     *   varName = expression ;
     */
    private void processLine(String line) {
        // Strip trailing semicolon if present
        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).strip();
        }

        // Must contain '=' to be a valid assignment
        if (!line.contains("=")) {
            throw new ExecutionException("Unrecognised statement (no '=' found): " + line);
        }

        // Split on first '=' only
        int eqIdx = line.indexOf('=');
        String lhs = line.substring(0, eqIdx).strip();   // e.g. "int a"  or  "a"
        String rhs = line.substring(eqIdx + 1).strip();  // e.g. "5"  or  "a + b"

        // Strip optional type keyword from LHS
        String varName = stripTypeKeyword(lhs);

        // Validate variable name
        if (!isValidIdentifier(varName)) {
            throw new ExecutionException("Invalid variable name: \"" + varName + "\"");
        }

        // Evaluate RHS expression
        int result = evaluateExpression(rhs);

        // Store in map
        variableStore.put(varName, result);
        System.out.println("  → Assigned: " + varName + " = " + result);
    }

    /**
     * Strips type keywords (int, double, float, long, short, byte) from LHS.
     */
    private String stripTypeKeyword(String lhs) {
        return lhs.replaceFirst("^(int|double|float|long|short|byte)\\s+", "").strip();
    }

    /**
     * Evaluates a simple arithmetic expression.
     * Supports: literal integers, variable names, and one binary operator.
     *
     * Examples:
     *   "5"       → 5
     *   "a"       → value of a
     *   "a + b"   → value of a + value of b
     *   "10 - 3"  → 7
     *   "a * 2"   → value of a * 2
     *   "b / 2"   → value of b / 2  (integer division)
     */
    private int evaluateExpression(String expr) {
        expr = expr.strip();

        // Try each operator (order matters — process left to right)
        for (char op : new char[]{'+', '-', '*', '/'}) {
            int opIdx = findOperatorIndex(expr, op);
            if (opIdx != -1) {
                String leftToken  = expr.substring(0, opIdx).strip();
                String rightToken = expr.substring(opIdx + 1).strip();

                int left  = resolveOperand(leftToken);
                int right = resolveOperand(rightToken);

                return applyOperator(op, left, right, expr);
            }
        }

        // No operator found — must be a single operand
        return resolveOperand(expr);
    }

    /**
     * Finds the index of operator character in the expression string,
     * skipping occurrences inside negative number literals.
     * Returns -1 if not found.
     */
    private int findOperatorIndex(String expr, char op) {
        // Scan from right for + and - to handle unary minus on left operand naturally
        // Use simple left-to-right scan; skip position 0 for '-' to avoid treating
        // a leading minus as binary subtraction.
        int start = (op == '-' || op == '+') ? 1 : 0;
        for (int i = start; i < expr.length(); i++) {
            if (expr.charAt(i) == op) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Resolves a token to its integer value.
     * Token is either a numeric literal or a variable name.
     */
    private int resolveOperand(String token) {
        token = token.strip();
        // Try parsing as integer literal first
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {
            // Not a literal — look up as variable
        }

        if (!variableStore.containsKey(token)) {
            throw new ExecutionException("Undefined variable: \"" + token + "\"");
        }
        return variableStore.get(token);
    }

    /**
     * Applies a binary arithmetic operator.
     */
    private int applyOperator(char op, int left, int right, String exprContext) {
        switch (op) {
            case '+': return left + right;
            case '-': return left - right;
            case '*': return left * right;
            case '/':
                if (right == 0) {
                    throw new ExecutionException("Division by zero in: \"" + exprContext + "\"");
                }
                return left / right;
            default:
                throw new ExecutionException("Unknown operator: " + op);
        }
    }

    /**
     * Rudimentary identifier validation (letters, digits, underscore; not starting with digit).
     */
    private boolean isValidIdentifier(String name) {
        return name.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    // -----------------------------------------------------------------------
    // Display helpers
    // -----------------------------------------------------------------------

    private void printVariableState() {
        if (variableStore.isEmpty()) {
            System.out.println("  Variables: (none)");
            return;
        }
        System.out.println("  Variables after this step:");
        variableStore.forEach((name, value) ->
                System.out.printf("    %-12s = %d%n", name, value));
    }

    private void printDivider() {
        System.out.println("  " + "-".repeat(45));
    }

    // -----------------------------------------------------------------------
    // Getters / Setters  (Encapsulation)
    // -----------------------------------------------------------------------

    /** Returns an unmodifiable view of the variable store. */
    public Map<String, Integer> getVariableStore() {
        return Collections.unmodifiableMap(variableStore);
    }

    /** Replaces the entire variable store (useful for testing/reset). */
    public void setVariableStore(HashMap<String, Integer> variableStore) {
        this.variableStore = variableStore;
    }

    /** Returns the number of steps executed so far. */
    public int getStepCount() {
        return stepCount;
    }

    /** Resets engine state (variable store + step counter). */
    public void reset() {
        variableStore.clear();
        stepCount = 0;
    }

    // -----------------------------------------------------------------------
    // Inner exception class for clean error reporting
    // -----------------------------------------------------------------------

    static class ExecutionException extends RuntimeException {
        ExecutionException(String message) {
            super(message);
        }
    }
}
