package visualizer.learning.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Request body for {@code POST /explain} (future REST endpoint).
 */
public final class ExplainStepRequest {

    /**
     * The kind of explanation requested for a step or session.
     */
    public enum Purpose {
        STEP_START,
        STEP_END,
        ERROR,
        SESSION_SUMMARY
    }

    private final String sessionId;
    private final String sourceCode;
    private final String language;
    private final StepContextDto step;
    private final Purpose purpose;
    private final int totalSteps;
    private final int variableCount;

    /**
     * Creates an explain-step request.
     *
     * @param sessionId      session identifier
     * @param sourceCode     raw source code
     * @param language       language name (e.g. {@code "JAVA"})
     * @param step           step context
     * @param purpose        explanation purpose
     * @param totalSteps     total steps (used for session summary; {@code 0} otherwise)
     * @param variableCount  final variable count (used for session summary; {@code 0} otherwise)
     */
    public ExplainStepRequest(
            String sessionId,
            String sourceCode,
            String language,
            StepContextDto step,
            Purpose purpose,
            int totalSteps,
            int variableCount) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.sourceCode = sourceCode != null ? sourceCode : "";
        this.language = Objects.requireNonNull(language, "language");
        this.step = Objects.requireNonNull(step, "step");
        this.purpose = Objects.requireNonNull(purpose, "purpose");
        this.totalSteps = totalSteps;
        this.variableCount = variableCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getLanguage() {
        return language;
    }

    public StepContextDto getStep() {
        return step;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getVariableCount() {
        return variableCount;
    }
}
