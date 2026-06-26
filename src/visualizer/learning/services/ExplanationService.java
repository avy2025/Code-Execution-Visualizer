package visualizer.learning.services;

import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Explanation;
import visualizer.learning.models.LearningSession;

import java.util.Optional;

/**
 * Generates human-readable explanations for code execution events.
 * Implementations may be rule-based, cached, or backed by an AI provider.
 */
public interface ExplanationService {

    /**
     * Called when a new learning session begins.
     *
     * @param session session context
     */
    void onSessionStart(LearningSession session);

    /**
     * Explains what is about to happen when a step starts.
     *
     * @param session active session
     * @param step    the step being started
     * @return an explanation, or empty if none is available
     */
    Optional<Explanation> explainStepStart(LearningSession session, ExecutionStep step);

    /**
     * Explains what changed after a step completes.
     *
     * @param session active session
     * @param step    the completed step
     * @return an explanation, or empty if none is available
     */
    Optional<Explanation> explainStepEnd(LearningSession session, ExecutionStep step);

    /**
     * Explains a runtime error in student-friendly terms.
     *
     * @param session active session
     * @param step    the error step
     * @return an explanation, or empty if none is available
     */
    Optional<Explanation> explainError(LearningSession session, ExecutionStep step);

    /**
     * Summarizes the full execution run.
     *
     * @param session        active session
     * @param totalSteps     total program counter steps taken
     * @param variableCount  number of variables in the final store
     * @return a summary explanation, or empty if none is available
     */
    Optional<Explanation> summarizeExecution(LearningSession session, int totalSteps, int variableCount);

    /**
     * Called when a learning session ends.
     *
     * @param session session context
     */
    void onSessionEnd(LearningSession session);
}
