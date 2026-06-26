package visualizer.learning.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable record of a single execution event observed during a learning session.
 * Built by {@link visualizer.learning.bridge.StepListenerBridge} from engine callbacks.
 */
public final class ExecutionStep {

    /**
     * The phase of execution represented by this step.
     */
    public enum Phase {
        /** A line is about to be executed. */
        STEP_START,
        /** A line finished executing successfully. */
        STEP_END,
        /** An error occurred while executing a line. */
        ERROR,
        /** The entire program finished executing. */
        SESSION_COMPLETE
    }

    private final int pc;
    private final String line;
    private final Phase phase;
    private final Map<String, Integer> variables;
    private final String errorMessage;
    private final long timestampMillis;

    /**
     * Creates an execution step.
     *
     * @param pc               program counter index
     * @param line             source line text (may be empty for session-complete steps)
     * @param phase            execution phase
     * @param variables          variable snapshot at this step (defensive copy stored)
     * @param errorMessage     error detail when {@code phase == ERROR}, otherwise {@code null}
     * @param timestampMillis  wall-clock time when the step was observed
     */
    public ExecutionStep(
            int pc,
            String line,
            Phase phase,
            Map<String, Integer> variables,
            String errorMessage,
            long timestampMillis) {
        this.pc = pc;
        this.line = line != null ? line : "";
        this.phase = Objects.requireNonNull(phase, "phase");
        this.variables = Collections.unmodifiableMap(new HashMap<>(variables != null ? variables : Map.of()));
        this.errorMessage = errorMessage;
        this.timestampMillis = timestampMillis;
    }

    /** @return program counter index into the parsed line list */
    public int getPc() {
        return pc;
    }

    /** @return the source line associated with this step */
    public String getLine() {
        return line;
    }

    /** @return the execution phase */
    public Phase getPhase() {
        return phase;
    }

    /** @return unmodifiable snapshot of variables at this step */
    public Map<String, Integer> getVariables() {
        return variables;
    }

    /**
     * @return error message when {@link #getPhase()} is {@link Phase#ERROR}, otherwise {@code null}
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** @return epoch milliseconds when this step was observed */
    public long getTimestampMillis() {
        return timestampMillis;
    }

    @Override
    public String toString() {
        return "ExecutionStep{pc=" + pc + ", phase=" + phase + ", line='" + line + "'}";
    }
}
