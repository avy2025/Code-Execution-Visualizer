package visualizer.learning.api;

/**
 * Logs learning HTTP failures separately from execution-engine errors.
 */
final class LearningApiHttpLogger {

    private static final String PREFIX = "[LearningAPI-HTTP]";

    private LearningApiHttpLogger() {
    }

    static void logFailure(String operation, String detail) {
        System.err.println(PREFIX + " " + operation + " failed: " + detail);
    }

    static void logRetry(String operation) {
        System.err.println(PREFIX + " " + operation + " timed out; retrying once");
    }

    static void logFallback(String operation) {
        System.err.println(PREFIX + " " + operation + " using local fallback response");
    }
}
