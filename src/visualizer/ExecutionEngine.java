package visualizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ExecutionEngine handles the step-by-step execution.
 * It now supports a program counter (pc) and basic flow control.
 */
public class ExecutionEngine {

    public interface StepListener {
        void onStepStart(int pc, String line);
        void onStepEnd(int pc, String line, String state);
        void onError(int pc, String line, String message);
        void onExecutionComplete(int totalSteps, int variablesCount);
    }

    private HashMap<String, Integer> variableStore;
    private List<String> lines;
    private int pc; // Program Counter (index in lines list)
    private StepListener listener;

    public ExecutionEngine() {
        this.variableStore = new LinkedHashMap<>();
        this.pc = 0;
    }

    public void setStepListener(StepListener listener) {
        this.listener = listener;
    }

    public StepListener getStepListener() {
        return listener;
    }

    /**
     * Prepares the engine for a new execution session.
     */
    public void prepare(List<String> lines) {
        this.lines = lines;
        this.pc = 0;
        this.variableStore.clear();
    }

    /**
     * Executes the next line in the sequence.
     * Returns true if there are more lines to execute.
     */
    public boolean executeNextStep() {
        if (lines == null || pc >= lines.size()) {
            return false;
        }

        String line = lines.get(pc).strip();
        
        // Handle empty lines or comments
        if (line.isEmpty() || line.startsWith("//")) {
            pc++;
            return executeNextStep(); // Recursively skip until we find code or end
        }

        if (listener != null) listener.onStepStart(pc, line);

        try {
            if (line.startsWith("if")) {
                handleIf(line);
            } else if (line.equals("}")) {
                // Closing brace of an if-block, just skip it
                pc++;
            } else {
                Statement stmt = parseStatement(line);
                stmt.execute(variableStore);
                pc++;
            }

            if (listener != null) {
                listener.onStepEnd(pc - 1, line, getVariableStateString());
            }

        } catch (Exception e) {
            if (listener != null) listener.onError(pc, line, e.getMessage());
            pc++; // Advance even on error to avoid infinite loops
        }

        return pc < lines.size();
    }

    private void handleIf(String line) {
        // Basic pattern: if (condition) {
        int startParen = line.indexOf('(');
        int endParen = line.lastIndexOf(')');
        int openBrace = line.indexOf('{');

        if (startParen == -1 || endParen == -1 || endParen < startParen) {
            throw new RuntimeException("Malformed if statement: " + line);
        }

        String conditionExpr = line.substring(startParen + 1, endParen).strip();
        boolean condition = ExpressionEvaluator.evaluateBoolean(conditionExpr, variableStore);

        if (condition) {
            // Condition true: just advance to the next line (inside the block)
            pc++;
        } else {
            // Condition false: skip until matching '}'
            int braceCount = 0;
            if (openBrace != -1) braceCount = 1;
            
            pc++; // Start looking from next line
            while (pc < lines.size()) {
                String l = lines.get(pc).strip();
                if (l.contains("{")) braceCount++;
                if (l.contains("}")) {
                    braceCount--;
                    if (braceCount <= 0) {
                        pc++; // Move past the closing brace
                        return;
                    }
                }
                pc++;
            }
            throw new RuntimeException("Unclosed if-block starting at: " + line);
        }
    }

    private Statement parseStatement(String line) {
        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).strip();
        } else if (!line.endsWith("{") && !line.endsWith("}")) {
             throw new RuntimeException("Syntax error: Missing semicolon at end of line.");
        }

        if (line.isEmpty()) throw new RuntimeException("Syntax error: Empty statement.");

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
        return new ExpressionStatement(line);
    }

    private boolean isDeclaration(String lhs) {
        return lhs.matches("^(int|double|float|long|short|byte)\\s+.*");
    }

    private String stripTypeKeyword(String lhs) {
        return lhs.replaceFirst("^(int|double|float|long|short|byte)\\s+", "").strip();
    }

    public String getVariableStateString() {
        return variableStore.toString();
    }

    public Map<String, Integer> getVariableStore() {
        return Collections.unmodifiableMap(variableStore);
    }

    public int getPC() {
        return pc;
    }

    public void reset() {
        pc = 0;
        variableStore.clear();
    }
}
