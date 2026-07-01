package visualizer.learning.api;

import visualizer.learning.api.dto.AnalyzeSessionRequest;
import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.GenerateHintRequest;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.StepContextDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration tests for {@link HttpLearningApiClient} against a running learning-api backend.
 * <p>
 * Run with the backend up:
 * {@code USE_HTTP_CLIENT=true java -cp bin visualizer.learning.api.HttpLearningApiClientIntegrationTest}
 */
public final class HttpLearningApiClientIntegrationTest {

    private static final String SESSION_ID = "integration-test-session";

    private HttpLearningApiClientIntegrationTest() {
    }

    public static void main(String[] args) {
        System.out.println("=== HttpLearningApiClient Integration Tests ===");

        testFallbackWhenBackendUnavailable();
        if (isBackendHealthy()) {
            testLiveBackend();
        } else {
            System.out.println("[skip] Backend not reachable at "
                    + LearningApiConfig.DEFAULT_BASE_URL.replace("/api/v1", "/health")
                    + " — live HTTP tests skipped");
        }

        System.out.println("=== All Integration Tests Completed ===");
    }

    private static void testFallbackWhenBackendUnavailable() {
        System.out.print("Testing fallback with invalid backend URL -> ");
        LearningApiConfig badConfig = new LearningApiConfig(
                "http://127.0.0.1:1/api/v1", 500, 500);
        HttpLearningApiClient client = new HttpLearningApiClient(badConfig);

        ExplainStepRequest request = sampleExplainRequest();
        var response = client.explainStep(request);

        if (response.getText().contains("variable named x")) {
            System.out.println("PASS");
        } else {
            throw new AssertionError("Expected fallback explanation text");
        }
    }

    private static void testLiveBackend() {
        System.out.println("Testing live backend responses:");
        HttpLearningApiClient client = new HttpLearningApiClient(LearningApiConfig.defaults());

        var explain = client.explainStep(sampleExplainRequest());
        assertContains(explain.getText(), "variable", "explainStep text");

        var hint = client.generateHint(sampleHintRequest());
        assertContains(hint.getText(), "initialized", "generateHint text");

        var quiz = client.generateQuiz(sampleQuizRequest());
        if (quiz.getQuestions().isEmpty()) {
            throw new AssertionError("Expected quiz question");
        }
        assertContains(quiz.getQuestions().get(0).getPrompt(), "value of x", "generateQuiz question");

        var analyze = client.analyzeSession(sampleAnalyzeRequest());
        if (!analyze.isAccepted() || analyze.getMessage().isBlank()) {
            throw new AssertionError("Expected accepted analyze response with message");
        }
        System.out.println("  analyze message: " + analyze.getMessage());
        System.out.println("Live backend tests: PASS");
    }

    private static boolean isBackendHealthy() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String healthUrl = LearningApiConfig.DEFAULT_BASE_URL.replace("/api/v1", "") + "/health";
            if (healthUrl.endsWith("//health")) {
                healthUrl = "http://localhost:8080/health";
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().contains("UP");
        } catch (Exception e) {
            return false;
        }
    }

    private static ExplainStepRequest sampleExplainRequest() {
        StepContextDto step = new StepContextDto(
                0, "int x = 5;", "STEP_END", Map.of("x", 5), null);
        return new ExplainStepRequest(
                SESSION_ID, "int x = 5;", "JAVA", step,
                ExplainStepRequest.Purpose.STEP_END, 0, 0);
    }

    private static GenerateHintRequest sampleHintRequest() {
        StepContextDto step = new StepContextDto(
                0, "int x = 5;", "STEP_END", Map.of("x", 5), null);
        return new GenerateHintRequest(SESSION_ID, step, "GUIDE", 0L, false);
    }

    private static GenerateQuizRequest sampleQuizRequest() {
        return new GenerateQuizRequest(
                SESSION_ID,
                "int x = 5;",
                List.of("int x = 5;"),
                "JAVA",
                GenerateQuizRequest.QuizType.PREVIEW);
    }

    private static AnalyzeSessionRequest sampleAnalyzeRequest() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("stepCount", 3);
        metrics.put("errorCount", 0);
        return new AnalyzeSessionRequest(SESSION_ID, metrics, 3, 1);
    }

    private static void assertContains(String actual, String expectedSubstring, String label) {
        if (actual == null || !actual.toLowerCase().contains(expectedSubstring.toLowerCase())) {
            throw new AssertionError(label + " expected to contain \"" + expectedSubstring + "\" but was: " + actual);
        }
        System.out.println("  " + label + ": PASS");
    }
}
