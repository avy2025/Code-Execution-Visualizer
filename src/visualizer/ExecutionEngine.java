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
    private Language currentLanguage = Language.JAVA;
    private java.util.Stack<StateSnapshot> history;

    public ExecutionEngine() {
        this.variableStore = new LinkedHashMap<>();
        this.pc = 0;
        this.history = new java.util.Stack<>();
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
    public void prepare(List<String> lines, Language lang) {
        this.lines = lines;
        this.pc = 0;
        this.currentLanguage = lang;
        this.variableStore.clear();
        this.history.clear();
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
        
        String commentPrefix = (currentLanguage == Language.PYTHON) ? "#" : "//";
        if (line.isEmpty() || line.startsWith(commentPrefix)) {
            pc++;
            return executeNextStep(); 
        }

        // Save current state before executing
        history.push(new StateSnapshot(pc, variableStore));

        if (listener != null) listener.onStepStart(pc, line);

        try {
            if (line.startsWith("if")) {
                handleIf(line);
            } else if (line.startsWith("while")) {
                handleWhile(line);
            } else if (line.equals("}") || line.equals("pass")) {
                // Check if we need to jump back for a loop
                handleBlockEnd();
            } else {
                Statement stmt = parseStatement(line);
                if (stmt != null) stmt.execute(variableStore);
                pc++;
            }

            if (listener != null) {
                listener.onStepEnd(pc - 1, line, getVariableStateString());
            }

        } catch (Exception e) {
            if (listener != null) listener.onError(pc, line, e.getMessage());
            pc++; 
        }

        return pc < lines.size();
    }

    /**
     * Reverts to the previous state.
     */
    public boolean stepBack() {
        if (history.isEmpty()) return false;
        
        StateSnapshot snapshot = history.pop();
        this.pc = snapshot.getPC();
        this.variableStore.clear();
        this.variableStore.putAll(snapshot.getVariableStore());
        
        if (listener != null) listener.onStepStart(pc, lines.get(pc));
        return true;
    }

    private void handleIf(String line) {
        String conditionExpr = extractCondition(line);
        boolean condition = ExpressionEvaluator.evaluateBoolean(conditionExpr, variableStore);

        if (condition) {
            pc++;
        } else {
            skipBlock(line);
        }
    }

    private void handleWhile(String line) {
        String conditionExpr = extractCondition(line);
        boolean condition = ExpressionEvaluator.evaluateBoolean(conditionExpr, variableStore);

        if (condition) {
            pc++; // Enter loop
        } else {
            skipBlock(line); // Skip loop
        }
    }

    private void handleBlockEnd() {
        // Scan backwards to find the matching start (if or while)
        int braceCount = 1;
        int searchPc = pc - 1;
        while (searchPc >= 0) {
            String l = lines.get(searchPc).strip();
            if (l.contains("}")) braceCount++;
            if (l.contains("{") || l.endsWith(":")) {
                braceCount--;
                if (braceCount == 0) {
                    if (l.startsWith("while")) {
                        pc = searchPc; // Jump back to while condition
                        return;
                    }
                    break;
                }
            }
            searchPc--;
        }
        pc++; // It was an if block, just continue
    }

    private String extractCondition(String line) {
        if (currentLanguage == Language.PYTHON) {
            return line.substring(line.indexOf("if") != -1 ? line.indexOf("if")+2 : line.indexOf("while")+5, line.length() - 1).strip();
        } else {
            int startParen = line.indexOf('(');
            int endParen = line.lastIndexOf(')');
            if (startParen == -1 || endParen == -1) throw new RuntimeException("Malformed condition: " + line);
            return line.substring(startParen + 1, endParen).strip();
        }
    }

    private void skipBlock(String startLine) {
        if (currentLanguage == Language.PYTHON) {
             pc++; // Simplification: skip one line for false condition in Python
        } else {
            int braceCount = 0;
            if (startLine.contains("{")) braceCount = 1;
            pc++;
            while (pc < lines.size()) {
                String l = lines.get(pc).strip();
                if (l.contains("{")) braceCount++;
                if (l.contains("}")) {
                    braceCount--;
                    if (braceCount <= 0) {
                        pc++;
                        return;
                    }
                }
                pc++;
            }
        }
    }

    private Statement parseStatement(String line) {
        if (currentLanguage == Language.JAVA) {
            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).strip();
            } else if (!line.endsWith("{") && !line.endsWith("}")) {
                 throw new RuntimeException("Syntax error: Missing semicolon at end of line.");
            }
        } else {
            // Python: semicolon optional
            if (line.endsWith(":")) return null; // Block start handled in handleIf
            if (line.endsWith(";")) line = line.substring(0, line.length() - 1).strip();
        }

        if (line.isEmpty()) return null;

        if (line.contains("=")) {
            int eqIdx = line.indexOf('=');
            String lhs = line.substring(0, eqIdx).strip();
            String rhs = line.substring(eqIdx + 1).strip();

            if (currentLanguage == Language.JAVA && isDeclaration(lhs)) {
                String varName = stripTypeKeyword(lhs);
                return new DeclarationStatement(varName, rhs);
            } else {
                // In Python or non-declaration Java
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
