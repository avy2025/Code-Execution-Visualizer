package visualizer.learning.api.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Response body for {@code POST /quizzes} (future REST endpoint).
 */
public final class GenerateQuizResponse {

    private final String quizId;
    private final List<QuizQuestionDto> questions;

    /**
     * Creates a generate-quiz response.
     *
     * @param quizId    quiz identifier
     * @param questions quiz questions
     */
    public GenerateQuizResponse(String quizId, List<QuizQuestionDto> questions) {
        this.quizId = Objects.requireNonNull(quizId, "quizId");
        this.questions = Collections.unmodifiableList(
                List.copyOf(questions != null ? questions : List.of()));
    }

    public String getQuizId() {
        return quizId;
    }

    public List<QuizQuestionDto> getQuestions() {
        return questions;
    }
}
