package visualizer.learning.models;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable container for a set of quiz questions generated from a learning session.
 */
public final class Quiz {

    /**
     * A single quiz question within a {@link Quiz}.
     */
    public static final class Question {
        private final String id;
        private final String prompt;

        /**
         * Creates a quiz question.
         *
         * @param id     unique question identifier within the quiz
         * @param prompt question text
         */
        public Question(String id, String prompt) {
            this.id = Objects.requireNonNull(id, "id");
            this.prompt = Objects.requireNonNull(prompt, "prompt");
        }

        /** @return unique question identifier */
        public String getId() {
            return id;
        }

        /** @return question prompt text */
        public String getPrompt() {
            return prompt;
        }

        @Override
        public String toString() {
            return "Question{id='" + id + "', prompt='" + prompt + "'}";
        }
    }

    private final String id;
    private final List<Question> questions;

    /**
     * Creates a quiz.
     *
     * @param id        unique quiz identifier
     * @param questions ordered list of questions (defensive copy stored)
     */
    public Quiz(String id, List<Question> questions) {
        this.id = Objects.requireNonNull(id, "id");
        this.questions = Collections.unmodifiableList(List.copyOf(questions != null ? questions : List.of()));
    }

    /** @return unique quiz identifier */
    public String getId() {
        return id;
    }

    /** @return unmodifiable list of questions (may be empty) */
    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public String toString() {
        return "Quiz{id='" + id + "', questions=" + questions.size() + "}";
    }
}
