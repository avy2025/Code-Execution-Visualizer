package visualizer.learning.api.dto;

import java.util.Objects;

/**
 * Response body for {@code POST /explain} (future REST endpoint).
 */
public final class ExplainStepResponse {

    private final String text;
    private final int pc;

    /**
     * Creates an explain-step response.
     *
     * @param text explanation text
     * @param pc   program counter ({@code -1} when not line-specific)
     */
    public ExplainStepResponse(String text, int pc) {
        this.text = Objects.requireNonNull(text, "text");
        this.pc = pc;
    }

    public String getText() {
        return text;
    }

    public int getPc() {
        return pc;
    }
}
