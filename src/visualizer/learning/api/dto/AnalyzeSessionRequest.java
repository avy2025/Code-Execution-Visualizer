package visualizer.learning.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Request body for {@code POST /sessions/analyze} (future REST endpoint).
 */
public final class AnalyzeSessionRequest {

    private final String sessionId;
    private final Map<String, Object> metrics;
    private final int totalSteps;
    private final int variableCount;

    /**
     * Creates an analyze-session request.
     *
     * @param sessionId      session identifier
     * @param metrics        collected session metrics
     * @param totalSteps     total steps executed
     * @param variableCount  variables in final store
     */
    public AnalyzeSessionRequest(
            String sessionId,
            Map<String, Object> metrics,
            int totalSteps,
            int variableCount) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.metrics = Collections.unmodifiableMap(new HashMap<>(metrics != null ? metrics : Map.of()));
        this.totalSteps = totalSteps;
        this.variableCount = variableCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getVariableCount() {
        return variableCount;
    }
}
