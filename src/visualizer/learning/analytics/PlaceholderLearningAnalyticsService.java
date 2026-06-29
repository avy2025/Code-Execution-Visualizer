package visualizer.learning.analytics;

import visualizer.learning.api.LearningApiClient;
import visualizer.learning.api.LearningApiException;
import visualizer.learning.api.dto.AnalyzeSessionRequest;
import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.LearningSession;
import visualizer.learning.services.LearningAnalyticsService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory {@link LearningAnalyticsService} that records local metrics and
 * delegates session analysis to {@link LearningApiClient#analyzeSession}.
 */
public final class PlaceholderLearningAnalyticsService implements LearningAnalyticsService {

    private final LearningApiClient apiClient;

    private final AtomicInteger stepCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();
    private final AtomicInteger hintRequestCount = new AtomicInteger();
    private final AtomicInteger quizAnswerCount = new AtomicInteger();
    private final AtomicInteger quizGeneratedCount = new AtomicInteger();
    private volatile String activeSessionId;
    private volatile int lastTotalSteps;
    private volatile int lastVariableCount;

    /**
     * Creates an analytics service backed by the given API client.
     *
     * @param apiClient learning API client (sole gateway for session analysis)
     */
    public PlaceholderLearningAnalyticsService(LearningApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    @Override
    public void onSessionStart(LearningSession session) {
        stepCount.set(0);
        errorCount.set(0);
        hintRequestCount.set(0);
        quizAnswerCount.set(0);
        quizGeneratedCount.set(0);
        lastTotalSteps = 0;
        lastVariableCount = 0;
        activeSessionId = session.getSessionId();
    }

    @Override
    public void recordStep(LearningSession session, ExecutionStep step) {
        if (step.getPhase() == ExecutionStep.Phase.STEP_END
                || step.getPhase() == ExecutionStep.Phase.SESSION_COMPLETE) {
            stepCount.incrementAndGet();
        }
        if (step.getPhase() == ExecutionStep.Phase.SESSION_COMPLETE) {
            lastTotalSteps = step.getPc();
            lastVariableCount = step.getVariables().size();
        }
    }

    @Override
    public void recordError(LearningSession session, ExecutionStep step) {
        errorCount.incrementAndGet();
    }

    @Override
    public void recordHintRequested(LearningSession session, int pc, String hintLevel) {
        hintRequestCount.incrementAndGet();
    }

    @Override
    public void recordQuizAnswer(LearningSession session, String questionId, boolean correct) {
        quizAnswerCount.incrementAndGet();
    }

    @Override
    public void recordQuizGenerated(LearningSession session) {
        quizGeneratedCount.incrementAndGet();
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // metrics retained until next session start
    }

    @Override
    public Map<String, Object> getSessionMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("sessionId", activeSessionId != null ? activeSessionId : "");
        metrics.put("stepCount", stepCount.get());
        metrics.put("errorCount", errorCount.get());
        metrics.put("hintRequestCount", hintRequestCount.get());
        metrics.put("quizAnswerCount", quizAnswerCount.get());
        metrics.put("quizGeneratedCount", quizGeneratedCount.get());
        return Collections.unmodifiableMap(metrics);
    }

    @Override
    public void flush() {
        if (activeSessionId == null) {
            return;
        }
        try {
            apiClient.analyzeSession(new AnalyzeSessionRequest(
                    activeSessionId,
                    getSessionMetrics(),
                    lastTotalSteps,
                    lastVariableCount));
        } catch (LearningApiException ignored) {
            // analytics must not affect visualizer behavior
        }
    }
}
