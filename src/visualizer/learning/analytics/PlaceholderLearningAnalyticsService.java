package visualizer.learning.analytics;

import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.LearningSession;
import visualizer.learning.services.LearningAnalyticsService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory placeholder {@link LearningAnalyticsService}.
 * Records step and error counts without persisting data or calling external services.
 */
public final class PlaceholderLearningAnalyticsService implements LearningAnalyticsService {

    private final AtomicInteger stepCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();
    private final AtomicInteger hintRequestCount = new AtomicInteger();
    private final AtomicInteger quizAnswerCount = new AtomicInteger();
    private volatile String activeSessionId;

    @Override
    public void onSessionStart(LearningSession session) {
        stepCount.set(0);
        errorCount.set(0);
        hintRequestCount.set(0);
        quizAnswerCount.set(0);
        activeSessionId = session.getSessionId();
    }

    @Override
    public void recordStep(LearningSession session, ExecutionStep step) {
        if (step.getPhase() == ExecutionStep.Phase.STEP_END
                || step.getPhase() == ExecutionStep.Phase.SESSION_COMPLETE) {
            stepCount.incrementAndGet();
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
        return Collections.unmodifiableMap(metrics);
    }

    @Override
    public void flush() {
        // no persistence in placeholder implementation
    }
}
