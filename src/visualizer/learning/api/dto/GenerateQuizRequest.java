package visualizer.learning.api.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Request body for {@code POST /quizzes} (future REST endpoint).
 */
public final class GenerateQuizRequest {

    /**
     * When the quiz should be generated relative to execution.
     */
    public enum QuizType {
        PREVIEW,
        POST_RUN
    }

    private final String sessionId;
    private final String sourceCode;
    private final List<String> parsedLines;
    private final String language;
    private final QuizType quizType;

    /**
     * Creates a generate-quiz request.
     *
     * @param sessionId    session identifier
     * @param sourceCode   raw source code
     * @param parsedLines  cleaned executable lines
     * @param language     language name
     * @param quizType     preview or post-run quiz
     */
    public GenerateQuizRequest(
            String sessionId,
            String sourceCode,
            List<String> parsedLines,
            String language,
            QuizType quizType) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.sourceCode = sourceCode != null ? sourceCode : "";
        this.parsedLines = Collections.unmodifiableList(
                List.copyOf(parsedLines != null ? parsedLines : List.of()));
        this.language = Objects.requireNonNull(language, "language");
        this.quizType = Objects.requireNonNull(quizType, "quizType");
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public List<String> getParsedLines() {
        return parsedLines;
    }

    public String getLanguage() {
        return language;
    }

    public QuizType getQuizType() {
        return quizType;
    }
}
