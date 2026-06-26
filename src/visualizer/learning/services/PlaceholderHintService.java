package visualizer.learning.services;

import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Hint;
import visualizer.learning.models.LearningSession;

import java.util.Optional;

/**
 * Placeholder {@link HintService} that returns no hints.
 * Does not call any external AI provider.
 */
public final class PlaceholderHintService implements HintService {

    @Override
    public void onSessionStart(LearningSession session) {
        // no-op
    }

    @Override
    public Optional<Hint> requestHint(LearningSession session, ExecutionStep step, Hint.Level level) {
        return Optional.empty();
    }

    @Override
    public Optional<Hint> suggestHintIfStuck(LearningSession session, ExecutionStep step, long idleMillis) {
        return Optional.empty();
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // no-op
    }
}
