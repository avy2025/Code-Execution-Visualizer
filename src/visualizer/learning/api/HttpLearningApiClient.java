package visualizer.learning.api;

import visualizer.learning.api.dto.AnalyzeSessionRequest;
import visualizer.learning.api.dto.AnalyzeSessionResponse;
import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.ExplainStepResponse;
import visualizer.learning.api.dto.GenerateHintRequest;
import visualizer.learning.api.dto.GenerateHintResponse;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.GenerateQuizResponse;
import visualizer.learning.api.dto.QuizQuestionDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Production HTTP implementation of {@link LearningApiClient} using Java 11+ {@link HttpClient}.
 * Returns deterministic fallback responses when the backend is unavailable.
 */
public final class HttpLearningApiClient implements LearningApiClient {

    private final LearningApiConfig config;
    private final HttpClient httpClient;
    private final LearningApiFallback fallback;

    /**
     * Creates an HTTP client bound to the given configuration.
     *
     * @param config API configuration including base URL and timeouts
     */
    public HttpLearningApiClient(LearningApiConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.fallback = new LearningApiFallback();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                .build();
    }

    @Override
    public ExplainStepResponse explainStep(ExplainStepRequest request) {
        Objects.requireNonNull(request, "request");
        return execute(
                "explainStep",
                "/explain",
                LearningApiJson.toJson(request),
                HttpLearningApiClient::parseExplainResponse,
                () -> fallback.explainStep(request));
    }

    @Override
    public GenerateHintResponse generateHint(GenerateHintRequest request) {
        Objects.requireNonNull(request, "request");
        return execute(
                "generateHint",
                "/hint",
                LearningApiJson.toJson(request),
                HttpLearningApiClient::parseHintResponse,
                () -> fallback.generateHint(request));
    }

    @Override
    public GenerateQuizResponse generateQuiz(GenerateQuizRequest request) {
        Objects.requireNonNull(request, "request");
        return execute(
                "generateQuiz",
                "/quiz",
                LearningApiJson.toJson(request),
                HttpLearningApiClient::parseQuizResponse,
                () -> fallback.generateQuiz(request));
    }

    @Override
    public AnalyzeSessionResponse analyzeSession(AnalyzeSessionRequest request) {
        Objects.requireNonNull(request, "request");
        return execute(
                "analyzeSession",
                "/analyze",
                LearningApiJson.toJson(request),
                HttpLearningApiClient::parseAnalyzeResponse,
                () -> fallback.analyzeSession(request));
    }

    private <T> T execute(
            String operation,
            String path,
            String requestBody,
            ResponseParser<T> parser,
            Supplier<T> fallbackSupplier) {
        try {
            return postWithRetry(operation, path, requestBody, parser);
        } catch (Exception e) {
            String detail = e.getMessage();
            if (detail == null || detail.isBlank()) {
                detail = e.getClass().getSimpleName();
            }
            LearningApiHttpLogger.logFailure(operation, detail);
            LearningApiHttpLogger.logFallback(operation);
            return fallbackSupplier.get();
        }
    }

    private <T> T postWithRetry(
            String operation,
            String path,
            String requestBody,
            ResponseParser<T> parser) throws Exception {
        try {
            return doPost(operation, path, requestBody, parser);
        } catch (HttpTimeoutException timeout) {
            LearningApiHttpLogger.logRetry(operation);
            return doPost(operation, path, requestBody, parser);
        }
    }

    private <T> T doPost(
            String operation,
            String path,
            String requestBody,
            ResponseParser<T> parser) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.resolveUrl(path)))
                .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new LearningApiException(
                    operation + " returned HTTP " + response.statusCode() + ": " + response.body());
        }

        String dataJson = LearningApiJson.extractDataObject(response.body());
        return parser.parse(dataJson);
    }

    private static ExplainStepResponse parseExplainResponse(String dataJson) {
        return new ExplainStepResponse(
                LearningApiJson.getString(dataJson, "text"),
                LearningApiJson.getInt(dataJson, "pc"));
    }

    private static GenerateHintResponse parseHintResponse(String dataJson) {
        String level = LearningApiJson.getString(dataJson, "level");
        if (level.isEmpty()) {
            level = LearningApiJson.getString(dataJson, "hintLevel");
        }
        return new GenerateHintResponse(
                LearningApiJson.getString(dataJson, "text"),
                level.isEmpty() ? "GUIDE" : level);
    }

    private static GenerateQuizResponse parseQuizResponse(String dataJson) {
        String quizId = LearningApiJson.getString(dataJson, "quizId");
        String question = LearningApiJson.getString(dataJson, "question");
        if (question.isEmpty()) {
            question = "Quiz question unavailable.";
        }
        return new GenerateQuizResponse(
                quizId.isEmpty() ? "quiz-fallback" : quizId,
                List.of(new QuizQuestionDto("q1", question)));
    }

    private static AnalyzeSessionResponse parseAnalyzeResponse(String dataJson) {
        String recommendation = LearningApiJson.getString(dataJson, "recommendation");
        if (recommendation.isEmpty()) {
            recommendation = LearningApiJson.getString(dataJson, "message");
        }
        if (recommendation.isEmpty()) {
            recommendation = "Session analysis recorded.";
        }
        return new AnalyzeSessionResponse(true, recommendation);
    }

    @FunctionalInterface
    private interface ResponseParser<T> {
        T parse(String dataJson) throws Exception;
    }
}
