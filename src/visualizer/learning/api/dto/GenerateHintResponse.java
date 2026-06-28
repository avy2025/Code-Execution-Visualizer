package visualizer.learning.api.dto;

import java.util.Objects;

/**
 * Response body for {@code POST /hints} (future REST endpoint).
 */
public final class GenerateHintResponse {

    private final String text;
    private final String level;

    /**
     * Creates a generate-hint response.
     *
     * @param text  hint text
     * @param level hint level name
     */
    public GenerateHintResponse(String text, String level) {
        this.text = Objects.requireNonNull(text, "text");
        this.level = Objects.requireNonNull(level, "level");
    }

    public String getText() {
        return text;
    }

    public String getLevel() {
        return level;
    }
}
