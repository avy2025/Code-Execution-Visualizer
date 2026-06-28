package visualizer.learning.api;

/**
 * Exception thrown when a learning API call fails, times out, or returns an error.
 */
public class LearningApiException extends Exception {

    /**
     * Creates an exception with a message.
     *
     * @param message error description
     */
    public LearningApiException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and cause.
     *
     * @param message error description
     * @param cause   underlying cause
     */
    public LearningApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
