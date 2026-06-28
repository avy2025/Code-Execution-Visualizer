package visualizer.learning.services;

import visualizer.learning.api.LearningApiClient;
import visualizer.learning.api.LearningApiException;
import visualizer.learning.api.LearningDtoMapper;
import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.ExplainStepResponse;
import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Explanation;
import visualizer.learning.models.LearningSession;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link ExplanationService} that delegates all explanation generation to {@link LearningApiClient}.
 */
public final class PlaceholderExplanationService implements ExplanationService {

    private final LearningApiClient apiClient;

    /**
     * Creates a service backed by the given API client.
     *
     * @param apiClient learning API client (sole gateway for explanations)
     */
    public PlaceholderExplanationService(LearningApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    @Override
    public void onSessionStart(LearningSession session) {
        // no local state
    }

    @Override
    public Optional<Explanation> explainStepStart(LearningSession session, ExecutionStep step) {
        return explain(session, step, ExplainStepRequest.Purpose.STEP_START, 0, 0);
    }

    @Override
    public Optional<Explanation> explainStepEnd(LearningSession session, ExecutionStep step) {
        return explain(session, step, ExplainStepRequest.Purpose.STEP_END, 0, 0);
    }

    @Override
    public Optional<Explanation> explainError(LearningSession session, ExecutionStep step) {
        return explain(session, step, ExplainStepRequest.Purpose.ERROR, 0, 0);
    }

    @Override
    public Optional<Explanation> summarizeExecution(LearningSession session, int totalSteps, int variableCount) {
        ExecutionStep summaryStep = new ExecutionStep(
                totalSteps,
                "",
                ExecutionStep.Phase.SESSION_COMPLETE,
                Collections.emptyMap(),
                null,
                System.currentTimeMillis());
        return explain(session, summaryStep, ExplainStepRequest.Purpose.SESSION_SUMMARY, totalSteps, variableCount);
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // no local state
    }

    private Optional<Explanation> explain(
            LearningSession session,
            ExecutionStep step,
            ExplainStepRequest.Purpose purpose,
            int totalSteps,
            int variableCount) {
        try {
            ExplainStepRequest request = LearningDtoMapper.toExplainRequest(
                    session, step, purpose, totalSteps, variableCount);
            ExplainStepResponse response = apiClient.explainStep(request);
            return Optional.of(new Explanation(response.getText(), response.getPc()));
        } catch (LearningApiException e) {
            return Optional.empty();
        }
    }
}
