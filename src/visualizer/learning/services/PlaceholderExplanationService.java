package visualizer.learning.services;

import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Explanation;
import visualizer.learning.models.LearningSession;

import java.util.Optional;

/**
 * Placeholder {@link ExplanationService} that returns deterministic stub text.
 * Does not call any external AI provider.
 */
public final class PlaceholderExplanationService implements ExplanationService {

    private static final String NOT_IMPLEMENTED = "Explanation not implemented.";

    @Override
    public void onSessionStart(LearningSession session) {
        // no-op
    }

    @Override
    public Optional<Explanation> explainStepStart(LearningSession session, ExecutionStep step) {
        return Optional.of(new Explanation(NOT_IMPLEMENTED, step.getPc()));
    }

    @Override
    public Optional<Explanation> explainStepEnd(LearningSession session, ExecutionStep step) {
        return Optional.of(new Explanation(NOT_IMPLEMENTED, step.getPc()));
    }

    @Override
    public Optional<Explanation> explainError(LearningSession session, ExecutionStep step) {
        return Optional.of(new Explanation(NOT_IMPLEMENTED, step.getPc()));
    }

    @Override
    public Optional<Explanation> summarizeExecution(LearningSession session, int totalSteps, int variableCount) {
        return Optional.of(new Explanation(NOT_IMPLEMENTED, -1));
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // no-op
    }
}
