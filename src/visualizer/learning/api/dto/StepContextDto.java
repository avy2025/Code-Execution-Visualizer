package visualizer.learning.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Serializable snapshot of an execution step for API requests and responses.
 */
public final class StepContextDto {

    private final int pc;
    private final String line;
    private final String phase;
    private final Map<String, Integer> variables;
    private final String errorMessage;

    /**
     * Creates a step context DTO.
     *
     * @param pc            program counter
     * @param line          source line text
     * @param phase         execution phase name
     * @param variables     variable snapshot
     * @param errorMessage  error detail, or {@code null}
     */
    public StepContextDto(
            int pc,
            String line,
            String phase,
            Map<String, Integer> variables,
            String errorMessage) {
        this.pc = pc;
        this.line = line != null ? line : "";
        this.phase = Objects.requireNonNull(phase, "phase");
        this.variables = Collections.unmodifiableMap(new HashMap<>(variables != null ? variables : Map.of()));
        this.errorMessage = errorMessage;
    }

    public int getPc() {
        return pc;
    }

    public String getLine() {
        return line;
    }

    public String getPhase() {
        return phase;
    }

    public Map<String, Integer> getVariables() {
        return variables;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
