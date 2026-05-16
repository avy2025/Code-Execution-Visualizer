package visualizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Capture the state of the execution at a specific point in time.
 */
public class StateSnapshot {
    private final int pc;
    private final Map<String, Integer> variableStoreSnapshot;

    public StateSnapshot(int pc, Map<String, Integer> variableStore) {
        this.pc = pc;
        this.variableStoreSnapshot = new HashMap<>(variableStore);
    }

    public int getPC() {
        return pc;
    }

    public Map<String, Integer> getVariableStore() {
        return variableStoreSnapshot;
    }
}
