package visualizer.learning.api.dto;

import java.util.Objects;

/**
 * Response body for {@code POST /sessions/analyze} (future REST endpoint).
 */
public final class AnalyzeSessionResponse {

    private final boolean accepted;
    private final String message;

    /**
     * Creates an analyze-session response.
     *
     * @param accepted whether the session analysis was accepted
     * @param message  status or summary message
     */
    public AnalyzeSessionResponse(boolean accepted, String message) {
        this.accepted = accepted;
        this.message = Objects.requireNonNull(message, "message");
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMessage() {
        return message;
    }
}
