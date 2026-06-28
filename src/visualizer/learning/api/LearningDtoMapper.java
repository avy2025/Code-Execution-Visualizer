package visualizer.learning.api;

import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.GenerateHintRequest;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.StepContextDto;
import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Hint;
import visualizer.learning.models.LearningSession;

/**
 * Maps domain learning models to API request DTOs.
 */
public final class LearningDtoMapper {

    private LearningDtoMapper() {
    }

    /**
     * Converts an execution step to a {@link StepContextDto}.
     *
     * @param step execution step
     * @return API step context
     */
    public static StepContextDto toStepContext(ExecutionStep step) {
        return new StepContextDto(
                step.getPc(),
                step.getLine(),
                step.getPhase().name(),
                step.getVariables(),
                step.getErrorMessage());
    }

    /**
     * Builds an explain-step request.
     *
     * @param session       learning session
     * @param step          execution step
     * @param purpose       explanation purpose
     * @param totalSteps    total steps for session summary
     * @param variableCount variable count for session summary
     * @return API request DTO
     */
    public static ExplainStepRequest toExplainRequest(
            LearningSession session,
            ExecutionStep step,
            ExplainStepRequest.Purpose purpose,
            int totalSteps,
            int variableCount) {
        return new ExplainStepRequest(
                session.getSessionId(),
                session.getSourceCode(),
                session.getLanguage().name(),
                toStepContext(step),
                purpose,
                totalSteps,
                variableCount);
    }

    /**
     * Builds a generate-hint request.
     *
     * @param session     learning session
     * @param step        execution step
     * @param level       hint level
     * @param idleMillis  idle duration for proactive hints
     * @param proactive   whether this is a stuck-learner suggestion
     * @return API request DTO
     */
    public static GenerateHintRequest toHintRequest(
            LearningSession session,
            ExecutionStep step,
            Hint.Level level,
            long idleMillis,
            boolean proactive) {
        return new GenerateHintRequest(
                session.getSessionId(),
                toStepContext(step),
                level.name(),
                idleMillis,
                proactive);
    }

    /**
     * Builds a generate-quiz request.
     *
     * @param session  learning session
     * @param quizType preview or post-run quiz
     * @return API request DTO
     */
    public static GenerateQuizRequest toQuizRequest(
            LearningSession session,
            GenerateQuizRequest.QuizType quizType) {
        return new GenerateQuizRequest(
                session.getSessionId(),
                session.getSourceCode(),
                session.getParsedLines(),
                session.getLanguage().name(),
                quizType);
    }
}
