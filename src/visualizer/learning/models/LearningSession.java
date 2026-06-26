package visualizer.learning.models;

import visualizer.Language;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable context for a single visualizer run from parse through execution.
 * Created when the user resets or restarts execution.
 */
public final class LearningSession {

    private final String sessionId;
    private final String sourceCode;
    private final List<String> parsedLines;
    private final Language language;

    /**
     * Creates a learning session.
     *
     * @param sessionId   unique session identifier
     * @param sourceCode  raw editor source text
     * @param parsedLines cleaned executable lines from {@link visualizer.CodeParser}
     * @param language    active language mode
     */
    public LearningSession(
            String sessionId,
            String sourceCode,
            List<String> parsedLines,
            Language language) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.sourceCode = sourceCode != null ? sourceCode : "";
        this.parsedLines = Collections.unmodifiableList(List.copyOf(parsedLines != null ? parsedLines : List.of()));
        this.language = Objects.requireNonNull(language, "language");
    }

    /** @return unique session identifier */
    public String getSessionId() {
        return sessionId;
    }

    /** @return raw editor source text */
    public String getSourceCode() {
        return sourceCode;
    }

    /** @return unmodifiable list of parsed executable lines */
    public List<String> getParsedLines() {
        return parsedLines;
    }

    /** @return active language mode */
    public Language getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "LearningSession{id='" + sessionId + "', lines=" + parsedLines.size() + ", language=" + language + "}";
    }
}
