package visualizer.learning.services;

import visualizer.learning.api.LearningApiClient;
import visualizer.learning.api.LearningApiException;
import visualizer.learning.api.LearningDtoMapper;
import visualizer.learning.api.dto.GenerateHintResponse;
import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Hint;
import visualizer.learning.models.LearningSession;

import java.util.Objects;
import java.util.Optional;

/**
 * {@link HintService} that delegates all hint generation to {@link LearningApiClient}.
 */
public final class PlaceholderHintService implements HintService {

    private final LearningApiClient apiClient;

    /**
     * Creates a service backed by the given API client.
     *
     * @param apiClient learning API client (sole gateway for hints)
     */
    public PlaceholderHintService(LearningApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    @Override
    public void onSessionStart(LearningSession session) {
        // no local state
    }

    @Override
    public Optional<Hint> requestHint(LearningSession session, ExecutionStep step, Hint.Level level) {
        return fetchHint(session, step, level, 0L, false);
    }

    @Override
    public Optional<Hint> suggestHintIfStuck(LearningSession session, ExecutionStep step, long idleMillis) {
        return fetchHint(session, step, Hint.Level.NUDGE, idleMillis, true);
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // no local state
    }

    private Optional<Hint> fetchHint(
            LearningSession session,
            ExecutionStep step,
            Hint.Level level,
            long idleMillis,
            boolean proactive) {
        try {
            GenerateHintResponse response = apiClient.generateHint(
                    LearningDtoMapper.toHintRequest(session, step, level, idleMillis, proactive));
            return Optional.of(new Hint(response.getText(), Hint.Level.valueOf(response.getLevel())));
        } catch (LearningApiException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
