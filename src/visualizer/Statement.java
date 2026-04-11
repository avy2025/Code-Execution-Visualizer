package visualizer;

import java.util.HashMap;

/**
 * Interface representing a generic executable statement.
 */
public interface Statement {
    /**
     * Executes the statement logic against the provided memory store.
     * @param memory The variable store (name to value map).
     */
    void execute(HashMap<String, Integer> memory);
}
