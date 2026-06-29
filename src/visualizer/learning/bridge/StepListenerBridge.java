package visualizer.learning.bridge;

import visualizer.ExecutionEngine;
import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.Explanation;
import visualizer.learning.models.Hint;
import visualizer.learning.models.LearningSession;
import visualizer.learning.models.Quiz;
import visualizer.learning.services.ExplanationService;
import visualizer.learning.services.HintService;
import visualizer.learning.services.LearningAnalyticsService;
import visualizer.learning.services.QuizService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Observes {@link ExecutionEngine.StepListener} callbacks and publishes
 * {@link ExecutionStep} events to the learning layer.
 * <p>
 * This bridge is read-only with respect to the engine: it never mutates engine
 * state or affects execution timing beyond the cost of in-memory event dispatch.
 */
public final class StepListenerBridge implements ExecutionEngine.StepListener {

    private static final List<String> DEFAULT_QUIZ_OPTIONS = List.of("5", "10", "105", "0");
    private static final String DEFAULT_QUIZ_ANSWER = "105";

    private final ExecutionEngine engine;
    private final ExplanationService explanationService;
    private final HintService hintService;
    private final QuizService quizService;
    private final LearningAnalyticsService analyticsService;

    private LearningSession currentSession;
    private final List<ExecutionStep> observedSteps = new ArrayList<>();
    private LearningPanelListener panelListener;
    private LearningPanelSnapshot.Builder panelState = new LearningPanelSnapshot.Builder();

    /**
     * Creates a bridge with constructor-injected service dependencies.
     *
     * @param engine              execution engine (read-only access via getters only)
     * @param explanationService  explanation provider
     * @param hintService         hint provider
     * @param quizService         quiz provider
     * @param analyticsService    analytics recorder
     */
    public StepListenerBridge(
            ExecutionEngine engine,
            ExplanationService explanationService,
            HintService hintService,
            QuizService quizService,
            LearningAnalyticsService analyticsService) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.explanationService = Objects.requireNonNull(explanationService, "explanationService");
        this.hintService = Objects.requireNonNull(hintService, "hintService");
        this.quizService = Objects.requireNonNull(quizService, "quizService");
        this.analyticsService = Objects.requireNonNull(analyticsService, "analyticsService");
    }

    /**
     * Registers a listener that receives {@link LearningPanelSnapshot} updates for the UI.
     *
     * @param panelListener UI listener, or {@code null} to detach
     */
    public void setPanelListener(LearningPanelListener panelListener) {
        this.panelListener = panelListener;
    }

    /**
     * Starts a new learning session and notifies all services.
     *
     * @param session immutable session context for this run
     */
    public void beginSession(LearningSession session) {
        this.currentSession = Objects.requireNonNull(session, "session");
        observedSteps.clear();
        panelState = new LearningPanelSnapshot.Builder();
        explanationService.onSessionStart(session);
        hintService.onSessionStart(session);
        quizService.onSessionStart(session);
        analyticsService.onSessionStart(session);

        List<Quiz> previewQuizzes = safePreviewQuiz(session);
        if (!previewQuizzes.isEmpty()) {
            analyticsService.recordQuizGenerated(session);
            applyQuiz(previewQuizzes.get(0));
        }

        notifySessionReset();
        publishPanelSnapshot(0, "", engine.getVariableStore(), null, null);
    }

    /**
     * Refreshes the current-step section from engine state without calling learning services.
     * Used after stepping backward in the visualizer.
     *
     * @param pc program counter
     */
    public void syncCurrentStep(int pc) {
        if (currentSession == null) {
            return;
        }
        String line = pc >= 0 && pc < currentSession.getParsedLines().size()
                ? currentSession.getParsedLines().get(pc)
                : "";
        panelState
                .lineNumber(pc + 1)
                .statement(line)
                .variablesText(formatVariables(engine.getVariableStore()));
        applyProgressMetrics();
        publishPanel();
    }

    /**
     * @return unmodifiable list of steps observed in the current session
     */
    public List<ExecutionStep> getObservedSteps() {
        return Collections.unmodifiableList(new ArrayList<>(observedSteps));
    }

    /**
     * @return the active session, or {@code null} if no session has started
     */
    public LearningSession getCurrentSession() {
        return currentSession;
    }

    @Override
    public void onStepStart(int pc, String line) {
        if (currentSession == null) {
            return;
        }
        ExecutionStep step = buildStep(pc, line, ExecutionStep.Phase.STEP_START, null);
        publish(step);

        Optional<Explanation> explanation = safeExplain(() ->
                explanationService.explainStepStart(currentSession, step));
        panelState
                .lineNumber(pc + 1)
                .statement(line)
                .variablesText(formatVariables(step.getVariables()));
        if (explanation.isPresent()) {
            panelState.explanationText(explanation.get().getText());
        }
        applyProgressMetrics();
        publishPanel();
    }

    @Override
    public void onStepEnd(int pc, String line, String state) {
        if (currentSession == null) {
            return;
        }
        ExecutionStep step = buildStep(pc, line, ExecutionStep.Phase.STEP_END, null);
        publish(step);
        analyticsService.recordStep(currentSession, step);

        Optional<Explanation> explanation = safeExplain(() ->
                explanationService.explainStepEnd(currentSession, step));
        Optional<Hint> hint = safeHint(() ->
                hintService.requestHint(currentSession, step, Hint.Level.GUIDE));
        if (hint.isPresent()) {
            analyticsService.recordHintRequested(
                    currentSession, step.getPc(), hint.get().getLevel().name());
        }

        panelState
                .lineNumber(pc + 1)
                .statement(line)
                .variablesText(formatVariables(step.getVariables()));
        if (explanation.isPresent()) {
            panelState.explanationText(explanation.get().getText());
        }
        panelState.hintText(hint.map(Hint::getText).orElse("No hint available."));
        applyProgressMetrics();
        publishPanel();
    }

    @Override
    public void onError(int pc, String line, String message) {
        if (currentSession == null) {
            return;
        }
        ExecutionStep step = buildStep(pc, line, ExecutionStep.Phase.ERROR, message);
        publish(step);
        analyticsService.recordError(currentSession, step);

        Optional<Explanation> explanation = safeExplain(() ->
                explanationService.explainError(currentSession, step));
        panelState
                .lineNumber(pc + 1)
                .statement(line)
                .variablesText(formatVariables(step.getVariables()));
        if (explanation.isPresent()) {
            panelState.explanationText(explanation.get().getText());
        } else {
            panelState.explanationText("Explanation unavailable.");
        }
        applyProgressMetrics();
        publishPanel();
    }

    @Override
    public void onExecutionComplete(int totalSteps, int variablesCount) {
        if (currentSession == null) {
            return;
        }
        ExecutionStep step = new ExecutionStep(
                totalSteps,
                "",
                ExecutionStep.Phase.SESSION_COMPLETE,
                engine.getVariableStore(),
                null,
                System.currentTimeMillis());
        publish(step);
        analyticsService.recordStep(currentSession, step);

        safeExplain(() -> explanationService.summarizeExecution(currentSession, totalSteps, variablesCount))
                .ifPresent(value -> panelState.explanationText(value.getText()));

        List<Quiz> postRunQuizzes = safePostRunQuiz(currentSession);
        if (!postRunQuizzes.isEmpty()) {
            analyticsService.recordQuizGenerated(currentSession);
            applyQuiz(postRunQuizzes.get(0));
        }

        analyticsService.flush();
        explanationService.onSessionEnd(currentSession);
        hintService.onSessionEnd(currentSession);
        quizService.onSessionEnd(currentSession);
        analyticsService.onSessionEnd(currentSession);

        applyProgressMetrics();
        publishPanel();
    }

    private List<Quiz> safePreviewQuiz(LearningSession session) {
        try {
            return quizService.generatePreviewQuiz(session);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Quiz> safePostRunQuiz(LearningSession session) {
        try {
            return quizService.generatePostRunQuiz(session);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Optional<Explanation> safeExplain(java.util.function.Supplier<Optional<Explanation>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Hint> safeHint(java.util.function.Supplier<Optional<Hint>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void applyQuiz(Quiz quiz) {
        if (quiz.getQuestions().isEmpty()) {
            return;
        }
        Quiz.Question question = quiz.getQuestions().get(0);
        panelState
                .quizQuestion(question.getPrompt())
                .quizOptions(DEFAULT_QUIZ_OPTIONS)
                .quizAnswer(DEFAULT_QUIZ_ANSWER);
    }

    private void applyProgressMetrics() {
        Map<String, Object> metrics = analyticsService.getSessionMetrics();
        panelState
                .totalSteps(intMetric(metrics, "stepCount"))
                .hintsGenerated(intMetric(metrics, "hintRequestCount"))
                .quizzesGenerated(intMetric(metrics, "quizGeneratedCount"))
                .errorsEncountered(intMetric(metrics, "errorCount"));
    }

    private int intMetric(Map<String, Object> metrics, String key) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private void publishPanelSnapshot(int pc, String line, Map<String, Integer> variables,
                                      String explanation, String hint) {
        panelState
                .lineNumber(pc + 1)
                .statement(line)
                .variablesText(formatVariables(variables));
        if (explanation != null) {
            panelState.explanationText(explanation);
        }
        if (hint != null) {
            panelState.hintText(hint);
        }
        applyProgressMetrics();
        publishPanel();
    }

    private void publishPanel() {
        if (panelListener == null) {
            return;
        }
        try {
            panelListener.onPanelUpdate(panelState.build());
        } catch (Exception ignored) {
            // UI updates must never affect execution
        }
    }

    private void notifySessionReset() {
        if (panelListener == null) {
            return;
        }
        try {
            panelListener.onSessionReset();
        } catch (Exception ignored) {
            // UI updates must never affect execution
        }
    }

    private ExecutionStep buildStep(int pc, String line, ExecutionStep.Phase phase, String errorMessage) {
        return new ExecutionStep(
                pc,
                line,
                phase,
                engine.getVariableStore(),
                errorMessage,
                System.currentTimeMillis());
    }

    private void publish(ExecutionStep step) {
        observedSteps.add(step);
    }

    private static String formatVariables(Map<String, Integer> variables) {
        if (variables == null || variables.isEmpty()) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        return sb.toString();
    }
}
