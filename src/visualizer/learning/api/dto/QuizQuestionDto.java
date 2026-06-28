package visualizer.learning.api.dto;

import java.util.Objects;

/**
 * A single quiz question within a {@link GenerateQuizResponse}.
 */
public final class QuizQuestionDto {

    private final String id;
    private final String prompt;

    /**
     * Creates a quiz question DTO.
     *
     * @param id     question identifier
     * @param prompt question text
     */
    public QuizQuestionDto(String id, String prompt) {
        this.id = Objects.requireNonNull(id, "id");
        this.prompt = Objects.requireNonNull(prompt, "prompt");
    }

    public String getId() {
        return id;
    }

    public String getPrompt() {
        return prompt;
    }
}
