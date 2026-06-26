package visualizer.learning.models;

import java.util.Objects;

/**
 * Immutable container for a contextual learning hint.
 */
public final class Hint {

    /**
     * Progressive hint detail levels.
     */
    public enum Level {
        NUDGE,
        GUIDE,
        REVEAL
    }

    private final String text;
    private final Level level;

    /**
     * Creates a hint.
     *
     * @param text  hint body
     * @param level detail level
     */
    public Hint(String text, Level level) {
        this.text = Objects.requireNonNull(text, "text");
        this.level = Objects.requireNonNull(level, "level");
    }

    /** @return the hint text */
    public String getText() {
        return text;
    }

    /** @return the hint detail level */
    public Level getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "Hint{level=" + level + ", text='" + text + "'}";
    }
}
