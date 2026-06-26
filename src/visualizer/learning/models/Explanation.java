package visualizer.learning.models;

import java.util.Objects;

/**
 * Immutable container for a human-readable explanation of code execution.
 */
public final class Explanation {

    private final String text;
    private final int pc;

    /**
     * Creates an explanation tied to a program counter position.
     *
     * @param text explanation body
     * @param pc   program counter index ({@code -1} when not line-specific)
     */
    public Explanation(String text, int pc) {
        this.text = Objects.requireNonNull(text, "text");
        this.pc = pc;
    }

    /** @return the explanation text */
    public String getText() {
        return text;
    }

    /**
     * @return the program counter this explanation refers to, or {@code -1} if global
     */
    public int getPc() {
        return pc;
    }

    @Override
    public String toString() {
        return "Explanation{pc=" + pc + ", text='" + text + "'}";
    }
}
