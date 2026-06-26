package visualizer.learning.services;

import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.LearningSession;

import java.util.Map;

/**
 * Tracks engagement and learning signals during a session without affecting execution.
 */
public interface LearningAnalyticsService {

    /**
     * Called when a new learning session begins.
     *
     * @param session session context
     */
    void onSessionStart(LearningSession session);

    /**
     * Records a successful execution step.
     *
     * @param session active session
     * @param step    observed step
     */
    void recordStep(LearningSession session, ExecutionStep step);

    /**
     * Records an execution error step.
     *
     * @param session active session
     * @param step    error step
     */
    void recordError(LearningSession session, ExecutionStep step);

    /**
     * Records that the learner requested a hint.
     *
     * @param session    active session
     * @param pc         program counter when the hint was requested
     * @param hintLevel  hint level name (e.g. {@code "NUDGE"})
     */
    void recordHintRequested(LearningSession session, int pc, String hintLevel);

    /**
     * Records a quiz answer submission.
     *
     * @param session     active session
     * @param questionId  question identifier
     * @param correct     whether the answer was correct
     */
    void recordQuizAnswer(LearningSession session, String questionId, boolean correct);

    /**
     * Called when a learning session ends.
     *
     * @param session session context
     */
    void onSessionEnd(LearningSession session);

    /**
     * @return aggregated metrics for the current or most recent session
     */
    Map<String, Object> getSessionMetrics();

    /**
     * Optional persistence hook; implementations may write metrics to storage.
     */
    void flush();
}
