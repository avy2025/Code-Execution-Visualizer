package visualizer.learning.api.dto;

import java.util.Objects;

/**
 * Request body for {@code POST /hints} (future REST endpoint).
 */
public final class GenerateHintRequest {

    private final String sessionId;
    private final StepContextDto step;
    private final String hintLevel;
    private final long idleMillis;
    private final boolean proactive;

    /**
     * Creates a generate-hint request.
     *
     * @param sessionId   session identifier
     * @param step        current step context
     * @param hintLevel   requested hint level name (e.g. {@code "NUDGE"})
     * @param idleMillis  idle time in milliseconds (for proactive hints)
     * @param proactive   {@code true} when suggesting a hint because the learner is stuck
     */
    public GenerateHintRequest(
            String sessionId,
            StepContextDto step,
            String hintLevel,
            long idleMillis,
            boolean proactive) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.step = Objects.requireNonNull(step, "step");
        this.hintLevel = Objects.requireNonNull(hintLevel, "hintLevel");
        this.idleMillis = idleMillis;
        this.proactive = proactive;
    }

    public String getSessionId() {
        return sessionId;
    }

    public StepContextDto getStep() {
        return step;
    }

    public String getHintLevel() {
        return hintLevel;
    }

    public long getIdleMillis() {
        return idleMillis;
    }

    public boolean isProactive() {
        return proactive;
    }
}
