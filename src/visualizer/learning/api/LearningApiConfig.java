package visualizer.learning.api;

/**
 * Central configuration for the learning REST API.
 * All clients read the base URL and timeouts from a single {@link LearningApiConfig} instance.
 */
public final class LearningApiConfig {

    /** Default base URL for the future learning backend. */
    public static final String DEFAULT_BASE_URL = "http://localhost:8080/api/v1";

    /** Default connection timeout in milliseconds. */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;

    /** Default read timeout in milliseconds. */
    public static final int DEFAULT_READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    /**
     * Creates API configuration.
     *
     * @param baseUrl           REST API base URL (no trailing slash required)
     * @param connectTimeoutMs  connection timeout in milliseconds
     * @param readTimeoutMs     read timeout in milliseconds
     */
    public LearningApiConfig(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        if (connectTimeoutMs <= 0) {
            throw new IllegalArgumentException("connectTimeoutMs must be positive");
        }
        if (readTimeoutMs <= 0) {
            throw new IllegalArgumentException("readTimeoutMs must be positive");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * @return configuration with default base URL and timeouts
     */
    public static LearningApiConfig defaults() {
        return new LearningApiConfig(
                DEFAULT_BASE_URL,
                DEFAULT_CONNECT_TIMEOUT_MS,
                DEFAULT_READ_TIMEOUT_MS);
    }

    /** @return REST API base URL */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** @return connection timeout in milliseconds */
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    /** @return read timeout in milliseconds */
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    /**
     * Resolves a relative API path against {@link #getBaseUrl()}.
     *
     * @param path path beginning with {@code /} (e.g. {@code "/explain"})
     * @return fully qualified URL
     */
    public String resolveUrl(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with '/'");
        }
        return baseUrl + path;
    }
}
