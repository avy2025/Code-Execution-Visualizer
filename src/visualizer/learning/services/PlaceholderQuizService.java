package visualizer.learning.services;

import visualizer.learning.models.LearningSession;
import visualizer.learning.models.Quiz;

import java.util.Collections;
import java.util.List;

/**
 * Placeholder {@link QuizService} that returns empty quiz lists.
 * Does not call any external AI provider.
 */
public final class PlaceholderQuizService implements QuizService {

    @Override
    public void onSessionStart(LearningSession session) {
        // no-op
    }

    @Override
    public List<Quiz> generatePreviewQuiz(LearningSession session) {
        return Collections.emptyList();
    }

    @Override
    public List<Quiz> generatePostRunQuiz(LearningSession session) {
        return Collections.emptyList();
    }

    @Override
    public QuizResult gradeAnswer(LearningSession session, Quiz quiz, String questionId, String userAnswer) {
        return new QuizResult(false, "Quiz grading not implemented.");
    }

    @Override
    public void onSessionEnd(LearningSession session) {
        // no-op
    }
}
