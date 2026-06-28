package visualizer.learning.services;

import visualizer.learning.api.LearningApiClient;
import visualizer.learning.api.LearningApiException;
import visualizer.learning.api.LearningDtoMapper;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.GenerateQuizResponse;
import visualizer.learning.api.dto.QuizQuestionDto;
import visualizer.learning.models.LearningSession;
import visualizer.learning.models.Quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link QuizService} that delegates quiz generation to {@link LearningApiClient}.
 */
public final class PlaceholderQuizService implements QuizService {

    private final LearningApiClient apiClient;

    /**
     * Creates a service backed by the given API client.
     *
     * @param apiClient learning API client (sole gateway for quizzes)
     */
    public PlaceholderQuizService(LearningApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    @Override
    public void onSessionStart(LearningSession session) {
        // no local state
    }

    @Override
    public List<Quiz> generatePreviewQuiz(LearningSession session) {
        return fetchQuiz(session, GenerateQuizRequest.QuizType.PREVIEW);
    }

    @Override
    public List<Quiz> generatePostRunQuiz(LearningSession session) {
        return fetchQuiz(session, GenerateQuizRequest.QuizType.POST_RUN);
    }

    @Override
    public QuizResult gradeAnswer(LearningSession session, Quiz quiz, String questionId, String userAnswer) {
        return new QuizResult(false, "Quiz grading not implemented.");
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // no local state
    }

    private List<Quiz> fetchQuiz(LearningSession session, GenerateQuizRequest.QuizType quizType) {
        try {
            GenerateQuizResponse response = apiClient.generateQuiz(
                    LearningDtoMapper.toQuizRequest(session, quizType));
            if (response.getQuestions().isEmpty()) {
                return Collections.emptyList();
            }
            List<Quiz.Question> questions = new ArrayList<>();
            for (QuizQuestionDto dto : response.getQuestions()) {
                questions.add(new Quiz.Question(dto.getId(), dto.getPrompt()));
            }
            return List.of(new Quiz(response.getQuizId(), questions));
        } catch (LearningApiException e) {
            return Collections.emptyList();
        }
    }
}
