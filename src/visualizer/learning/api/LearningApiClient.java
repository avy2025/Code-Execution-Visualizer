package visualizer.learning.api;

import visualizer.learning.api.dto.AnalyzeSessionRequest;
import visualizer.learning.api.dto.AnalyzeSessionResponse;
import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.ExplainStepResponse;
import visualizer.learning.api.dto.GenerateHintRequest;
import visualizer.learning.api.dto.GenerateHintResponse;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.GenerateQuizResponse;

/**
 * Single entry point for all learning backend communication.
 * Future HTTP implementations will serialize these DTOs to REST endpoints
 * configured via {@link LearningApiConfig}.
 */
public interface LearningApiClient {

    /**
     * Requests an explanation for a step or session summary.
     *
     * @param request explain request
     * @return explanation response
     * @throws LearningApiException when the call fails or times out
     */
    ExplainStepResponse explainStep(ExplainStepRequest request) throws LearningApiException;

    /**
     * Requests a contextual hint for the current step.
     *
     * @param request hint request
     * @return hint response
     * @throws LearningApiException when the call fails or times out
     */
    GenerateHintResponse generateHint(GenerateHintRequest request) throws LearningApiException;

    /**
     * Requests quiz questions for a session.
     *
     * @param request quiz request
     * @return quiz response
     * @throws LearningApiException when the call fails or times out
     */
    GenerateQuizResponse generateQuiz(GenerateQuizRequest request) throws LearningApiException;

    /**
     * Submits session metrics for backend analysis.
     *
     * @param request analyze-session request
     * @return analysis acknowledgment
     * @throws LearningApiException when the call fails or times out
     */
    AnalyzeSessionResponse analyzeSession(AnalyzeSessionRequest request) throws LearningApiException;
}
