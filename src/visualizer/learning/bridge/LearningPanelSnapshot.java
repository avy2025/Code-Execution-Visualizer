package visualizer.learning.bridge;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable view-model pushed to the Learning Center UI after each bridge event.
 */
public final class LearningPanelSnapshot {

    private final int lineNumber;
    private final String statement;
    private final String variablesText;
    private final String explanationText;
    private final String hintText;
    private final String quizQuestion;
    private final List<String> quizOptions;
    private final String quizAnswer;
    private final int totalSteps;
    private final int hintsGenerated;
    private final int quizzesGenerated;
    private final int errorsEncountered;

    private LearningPanelSnapshot(Builder builder) {
        this.lineNumber = builder.lineNumber;
        this.statement = builder.statement;
        this.variablesText = builder.variablesText;
        this.explanationText = builder.explanationText;
        this.hintText = builder.hintText;
        this.quizQuestion = builder.quizQuestion;
        this.quizOptions = Collections.unmodifiableList(builder.quizOptions);
        this.quizAnswer = builder.quizAnswer;
        this.totalSteps = builder.totalSteps;
        this.hintsGenerated = builder.hintsGenerated;
        this.quizzesGenerated = builder.quizzesGenerated;
        this.errorsEncountered = builder.errorsEncountered;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getStatement() {
        return statement;
    }

    public String getVariablesText() {
        return variablesText;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public String getHintText() {
        return hintText;
    }

    public String getQuizQuestion() {
        return quizQuestion;
    }

    public List<String> getQuizOptions() {
        return quizOptions;
    }

    public String getQuizAnswer() {
        return quizAnswer;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getHintsGenerated() {
        return hintsGenerated;
    }

    public int getQuizzesGenerated() {
        return quizzesGenerated;
    }

    public int getErrorsEncountered() {
        return errorsEncountered;
    }

    /**
     * @return a new builder seeded from this snapshot
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link LearningPanelSnapshot}.
     */
    public static final class Builder {
        private int lineNumber;
        private String statement = "";
        private String variablesText = "";
        private String explanationText = "Explanation unavailable.";
        private String hintText = "No hint available.";
        private String quizQuestion = "No quiz available.";
        private List<String> quizOptions = List.of("—", "—", "—", "—");
        private String quizAnswer = "—";
        private int totalSteps;
        private int hintsGenerated;
        private int quizzesGenerated;
        private int errorsEncountered;

        public Builder() {
        }

        private Builder(LearningPanelSnapshot snapshot) {
            this.lineNumber = snapshot.lineNumber;
            this.statement = snapshot.statement;
            this.variablesText = snapshot.variablesText;
            this.explanationText = snapshot.explanationText;
            this.hintText = snapshot.hintText;
            this.quizQuestion = snapshot.quizQuestion;
            this.quizOptions = snapshot.quizOptions;
            this.quizAnswer = snapshot.quizAnswer;
            this.totalSteps = snapshot.totalSteps;
            this.hintsGenerated = snapshot.hintsGenerated;
            this.quizzesGenerated = snapshot.quizzesGenerated;
            this.errorsEncountered = snapshot.errorsEncountered;
        }

        public Builder lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Builder statement(String statement) {
            this.statement = statement != null ? statement : "";
            return this;
        }

        public Builder variablesText(String variablesText) {
            this.variablesText = variablesText != null ? variablesText : "";
            return this;
        }

        public Builder explanationText(String explanationText) {
            this.explanationText = explanationText != null ? explanationText : "Explanation unavailable.";
            return this;
        }

        public Builder hintText(String hintText) {
            this.hintText = hintText != null ? hintText : "No hint available.";
            return this;
        }

        public Builder quizQuestion(String quizQuestion) {
            this.quizQuestion = quizQuestion != null ? quizQuestion : "No quiz available.";
            return this;
        }

        public Builder quizOptions(List<String> quizOptions) {
            this.quizOptions = quizOptions != null ? List.copyOf(quizOptions) : List.of();
            return this;
        }

        public Builder quizAnswer(String quizAnswer) {
            this.quizAnswer = quizAnswer != null ? quizAnswer : "—";
            return this;
        }

        public Builder totalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
            return this;
        }

        public Builder hintsGenerated(int hintsGenerated) {
            this.hintsGenerated = hintsGenerated;
            return this;
        }

        public Builder quizzesGenerated(int quizzesGenerated) {
            this.quizzesGenerated = quizzesGenerated;
            return this;
        }

        public Builder errorsEncountered(int errorsEncountered) {
            this.errorsEncountered = errorsEncountered;
            return this;
        }

        public LearningPanelSnapshot build() {
            return new LearningPanelSnapshot(this);
        }
    }
}
