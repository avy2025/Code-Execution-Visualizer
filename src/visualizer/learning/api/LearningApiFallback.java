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

/**
 * Deterministic fallback responses used when the learning HTTP backend is unavailable.
 */
public final class LearningApiFallback {

    private static final String EXPLANATION =
            "A variable named x is created and initialized with value 5.";
    private static final String HINT =
            "Remember that variables must be initialized before use.";
    private static final String QUIZ_QUESTION =
            "What will be the value of x after this line executes?";

    /**
     * @param request original explain request
     * @return local fallback explanation
     */
    public ExplainStepResponse explainStep(ExplainStepRequest request) {
        int pc = request.getPurpose() == ExplainStepRequest.Purpose.SESSION_SUMMARY
                ? -1
                : request.getStep().getPc();
        return new ExplainStepResponse(EXPLANATION, pc);
    }

    /**
     * @param request original hint request
     * @return local fallback hint
     */
    public GenerateHintResponse generateHint(GenerateHintRequest request) {
        return new GenerateHintResponse(HINT, request.getHintLevel());
    }

    /**
     * @param request original quiz request
     * @return local fallback quiz
     */
    public GenerateQuizResponse generateQuiz(GenerateQuizRequest request) {
        String quizId = request.getSessionId() + "-" + request.getQuizType().name().toLowerCase();
        return new GenerateQuizResponse(
                quizId,
                List.of(new QuizQuestionDto("q1", QUIZ_QUESTION)));
    }

    /**
     * @param request original analyze request
     * @return local fallback analysis acknowledgment
     */
    public AnalyzeSessionResponse analyzeSession(AnalyzeSessionRequest request) {
        return new AnalyzeSessionResponse(
                true,
                "Session analysis unavailable. Fallback recorded for " + request.getSessionId() + ".");
    }
}
