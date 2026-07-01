package visualizer.learning.api;

import visualizer.learning.api.dto.AnalyzeSessionRequest;
import visualizer.learning.api.dto.AnalyzeSessionResponse;
import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.ExplainStepResponse;
import visualizer.learning.api.dto.GenerateHintRequest;
import visualizer.learning.api.dto.GenerateHintResponse;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.GenerateQuizResponse;
import visualizer.learning.api.dto.QuizQuestionDto;
import visualizer.learning.api.dto.StepContextDto;

import java.util.List;
import java.util.Objects;

/**
 * Deterministic in-process stand-in for the learning REST backend.
 * Does not perform HTTP calls or invoke any AI SDK.
 */
public final class MockLearningApiClient implements LearningApiClient {

    private final LearningApiConfig config;
    private final LearningApiFallback fallback = new LearningApiFallback();

    /**
     * Creates a mock client bound to the given configuration.
     *
     * @param config API configuration
     */
    public MockLearningApiClient(LearningApiConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public ExplainStepResponse explainStep(ExplainStepRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "explainStep", () -> fallback.explainStep(request));
    }

    @Override
    public GenerateHintResponse generateHint(GenerateHintRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "generateHint", () -> fallback.generateHint(request));
    }

    @Override
    public GenerateQuizResponse generateQuiz(GenerateQuizRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "generateQuiz", () -> fallback.generateQuiz(request));
    }

    @Override
    public AnalyzeSessionResponse analyzeSession(AnalyzeSessionRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "analyzeSession", () -> fallback.analyzeSession(request));
    }
}
