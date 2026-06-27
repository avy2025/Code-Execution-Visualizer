package visualizer.learning.bridge;

import visualizer.ExecutionEngine;
import visualizer.learning.models.ExecutionStep;
import visualizer.learning.models.LearningSession;
import visualizer.learning.services.ExplanationService;
import visualizer.learning.services.HintService;
import visualizer.learning.services.LearningAnalyticsService;
import visualizer.learning.services.QuizService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Observes {@link ExecutionEngine.StepListener} callbacks and publishes
 * {@link ExecutionStep} events to the learning layer.
 * <p>
 * This bridge is read-only with respect to the engine: it never mutates engine
 * state or affects execution timing beyond the cost of in-memory event dispatch.
 */
public final class StepListenerBridge implements ExecutionEngine.StepListener {

    private final ExecutionEngine engine;
    private final ExplanationService explanationService;
    private final HintService hintService;
    private final QuizService quizService;
    private final LearningAnalyticsService analyticsService;

    private LearningSession currentSession;
    private final List<ExecutionStep> observedSteps = new ArrayList<>();

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
     * Starts a new learning session and notifies all services.
     *
     * @param session immutable session context for this run
     */
    public void beginSession(LearningSession session) {
        this.currentSession = Objects.requireNonNull(session, "session");
        observedSteps.clear();
        explanationService.onSessionStart(session);
        hintService.onSessionStart(session);
        quizService.onSessionStart(session);
        analyticsService.onSessionStart(session);
        quizService.generatePreviewQuiz(session);
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
        explanationService.explainStepStart(currentSession, step);
    }

    @Override
    public void onStepEnd(int pc, String line, String state) {
        if (currentSession == null) {
            return;
        }
        ExecutionStep step = buildStep(pc, line, ExecutionStep.Phase.STEP_END, null);
        publish(step);
        analyticsService.recordStep(currentSession, step);
        explanationService.explainStepEnd(currentSession, step);
    }

    @Override
    public void onError(int pc, String line, String message) {
        if (currentSession == null) {
            return;
        }
        ExecutionStep step = buildStep(pc, line, ExecutionStep.Phase.ERROR, message);
        publish(step);
        analyticsService.recordError(currentSession, step);
        explanationService.explainError(currentSession, step);
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
        explanationService.summarizeExecution(currentSession, totalSteps, variablesCount);
        quizService.generatePostRunQuiz(currentSession);
        analyticsService.flush();
        explanationService.onSessionEnd(currentSession);
        hintService.onSessionEnd(currentSession);
        quizService.onSessionEnd(currentSession);
        analyticsService.onSessionEnd(currentSession);
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
}
