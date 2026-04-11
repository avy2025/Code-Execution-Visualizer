package visualizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    // Scanner for step-by-step pause
    private final Scanner inputScanner;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public ExecutionEngine() {
        this.variableStore = new LinkedHashMap<>();   // LinkedHashMap keeps insertion order for printing
        this.stepCount = 0;
        this.inputScanner = new Scanner(System.in);
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

        System.out.println("[ExecutionEngine] Starting step-by-step execution of " + lines.size() + " line(s)...");
        System.out.println("Press Enter to execute each step.");
        printDivider();

        for (String line : lines) {
            stepCount++;
            System.out.println("Step " + stepCount + ":");
            System.out.println("Executing: " + line);

            try {
                Statement stmt = parseStatement(line);
                stmt.execute(variableStore);
            } catch (Exception e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }

            printVariableState();
            
            if (stepCount < lines.size()) {
                System.out.print("\n(Press Enter to continue...) ");
                inputScanner.nextLine();
            }
            printDivider();
        }

        System.out.println("[ExecutionEngine] Execution complete. "
                + variableStore.size() + " variable(s) in memory.");
    }

    // -----------------------------------------------------------------------
    // Core parsing and factory logic
    // -----------------------------------------------------------------------

    /**
     * Identifies the statement type and creates corresponding Statement object.
     */
    private Statement parseStatement(String line) {
        // Strip trailing semicolon
        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).strip();
        }

        // 1. Declaration or Assignment (contains '=')
        if (line.contains("=")) {
            int eqIdx = line.indexOf('=');
            String lhs = line.substring(0, eqIdx).strip();
            String rhs = line.substring(eqIdx + 1).strip();

            if (isDeclaration(lhs)) {
                String varName = stripTypeKeyword(lhs);
                return new DeclarationStatement(varName, rhs);
            } else {
                return new AssignmentStatement(lhs, rhs);
            }
        }

        // 2. Default to ExpressionStatement
        return new ExpressionStatement(line);
    }

    /**
     * Checks if the LHS contains a type keyword.
     */
    private boolean isDeclaration(String lhs) {
        return lhs.matches("^(int|double|float|long|short|byte)\\s+.*");
    }

    private String stripTypeKeyword(String lhs) {
        return lhs.replaceFirst("^(int|double|float|long|short|byte)\\s+", "").strip();
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
            System.out.println("Variables: {}");
            return;
        }

        StringBuilder sb = new StringBuilder("Variables: {");
        int count = 0;
        for (Map.Entry<String, Integer> entry : variableStore.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            if (++count < variableStore.size()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        System.out.println(sb.toString());
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
