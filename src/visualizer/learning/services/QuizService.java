package visualizer.learning.services;

import visualizer.learning.models.LearningSession;
import visualizer.learning.models.Quiz;

import java.util.List;

/**
 * Generates and grades quizzes derived from executed programs.
 */
public interface QuizService {

    /**
     * Outcome of grading a single quiz answer.
     */
    final class QuizResult {
        private final boolean correct;
        private final String feedback;

        /**
         * Creates a quiz grading result.
         *
         * @param correct  whether the answer was correct
         * @param feedback feedback text for the learner
         */
        public QuizResult(boolean correct, String feedback) {
            this.correct = correct;
            this.feedback = feedback != null ? feedback : "";
        }

        /** @return {@code true} if the answer was correct */
        public boolean isCorrect() {
            return correct;
        }

        /** @return feedback text for the learner */
        public String getFeedback() {
            return feedback;
        }
    }

    /**
     * Called when a new learning session begins.
     *
     * @param session session context
     */
    void onSessionStart(LearningSession session);

    /**
     * Generates preview questions from code structure before execution.
     *
     * @param session active session
     * @return generated quizzes (may be empty)
     */
    List<Quiz> generatePreviewQuiz(LearningSession session);

    /**
     * Generates questions after execution using the observed step trace.
     *
     * @param session active session
     * @return generated quizzes (may be empty)
     */
    List<Quiz> generatePostRunQuiz(LearningSession session);

    /**
     * Grades a single answer for a quiz question.
     *
     * @param session    active session
     * @param quiz       the quiz being answered
     * @param questionId identifier of the question within the quiz
     * @param userAnswer learner's answer text
     * @return grading result with feedback
     */
    QuizResult gradeAnswer(LearningSession session, Quiz quiz, String questionId, String userAnswer);

    /**
     * Called when a learning session ends.
     *
     * @param session session context
     */
    void onSessionEnd(LearningSession session);
}
