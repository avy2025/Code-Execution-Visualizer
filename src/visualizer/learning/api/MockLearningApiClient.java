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

import java.util.List;
import java.util.Objects;

/**
 * Deterministic in-process stand-in for the future REST learning backend.
 * Does not perform HTTP calls or invoke any AI SDK.
 */
public final class MockLearningApiClient implements LearningApiClient {

    private static final String MOCK_EXPLANATION = "This variable is initialized with value 5.";
    private static final String MOCK_HINT = "Check whether the variable has been initialized.";
    private static final String MOCK_QUIZ_PROMPT = "What will be the value of x after this line?";

    private final LearningApiConfig config;

    /**
     * Creates a mock client bound to the given configuration.
     *
     * @param config API configuration (base URL reserved for future HTTP client)
     */
    public MockLearningApiClient(LearningApiConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public ExplainStepResponse explainStep(ExplainStepRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "explainStep", () -> {
            int pc = request.getPurpose() == ExplainStepRequest.Purpose.SESSION_SUMMARY
                    ? -1
                    : request.getStep().getPc();
            return new ExplainStepResponse(MOCK_EXPLANATION, pc);
        });
    }

    @Override
    public GenerateHintResponse generateHint(GenerateHintRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "generateHint", () ->
                new GenerateHintResponse(MOCK_HINT, request.getHintLevel()));
    }

    @Override
    public GenerateQuizResponse generateQuiz(GenerateQuizRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "generateQuiz", () -> {
            String quizId = request.getSessionId() + "-" + request.getQuizType().name().toLowerCase();
            List<QuizQuestionDto> questions = List.of(
                    new QuizQuestionDto("q1", MOCK_QUIZ_PROMPT));
            return new GenerateQuizResponse(quizId, questions);
        });
    }

    @Override
    public AnalyzeSessionResponse analyzeSession(AnalyzeSessionRequest request) throws LearningApiException {
        Objects.requireNonNull(request, "request");
        return LearningApiTimeouts.execute(config, "analyzeSession", () ->
                new AnalyzeSessionResponse(true, "Session analysis recorded for " + request.getSessionId()));
    }
}
