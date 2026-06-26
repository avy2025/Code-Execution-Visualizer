package visualizer.learning.services;

import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Hint;
import visualizer.learning.models.LearningSession;

import java.util.Optional;

/**
 * Provides progressive, context-aware hints tied to execution position and state.
 */
public interface HintService {

    /**
     * Called when a new learning session begins.
     *
     * @param session session context
     */
    void onSessionStart(LearningSession session);

    /**
     * Returns the next hint for the current execution position.
     *
     * @param session active session
     * @param step    current execution step
     * @param level   requested hint detail level
     * @return a hint, or empty if none is available
     */
    Optional<Hint> requestHint(LearningSession session, ExecutionStep step, Hint.Level level);

    /**
     * Suggests a proactive hint when the learner appears stuck.
     *
     * @param session     active session
     * @param step        current execution step
     * @param idleMillis  milliseconds since last step activity
     * @return a hint, or empty if none is available
     */
    Optional<Hint> suggestHintIfStuck(LearningSession session, ExecutionStep step, long idleMillis);

    /**
     * Called when a learning session ends.
     *
     * @param session session context
     */
    void onSessionEnd(LearningSession session);
}
