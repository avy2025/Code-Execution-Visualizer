package visualizer.learning.api;

/**
 * Creates the active {@link LearningApiClient} based on configuration.
 */
public final class LearningApiClientFactory {

    private static final String USE_HTTP_CLIENT_KEY = "USE_HTTP_CLIENT";

    private LearningApiClientFactory() {
    }

    /**
     * Creates either {@link HttpLearningApiClient} or {@link MockLearningApiClient}.
     *
     * @param config API configuration
     * @return configured client implementation
     */
    public static LearningApiClient create(LearningApiConfig config) {
        if (isHttpClientEnabled()) {
            System.out.println("[LearningAPI] Using HttpLearningApiClient -> " + config.getBaseUrl());
            return new HttpLearningApiClient(config);
        }
        System.out.println("[LearningAPI] Using MockLearningApiClient");
        return new MockLearningApiClient(config);
    }

    /**
     * @return {@code true} when {@code USE_HTTP_CLIENT=true} in system properties or environment
     */
    public static boolean isHttpClientEnabled() {
        String value = System.getProperty(USE_HTTP_CLIENT_KEY);
        if (value == null || value.isBlank()) {
            value = System.getenv(USE_HTTP_CLIENT_KEY);
        }
        return value != null && value.equalsIgnoreCase("true");
    }
}
