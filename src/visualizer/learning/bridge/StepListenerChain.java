package visualizer.learning.bridge;

import visualizer.ExecutionEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Composite {@link ExecutionEngine.StepListener} that fans out callbacks to multiple
 * listeners without modifying the execution engine.
 */
public final class StepListenerChain implements ExecutionEngine.StepListener {

    private final List<ExecutionEngine.StepListener> listeners;

    private StepListenerChain(List<ExecutionEngine.StepListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    /**
     * Creates a chain from the given listeners, invoked in declaration order.
     *
     * @param listeners one or more listeners (null entries are not permitted)
     * @return a composite listener
     */
    public static StepListenerChain of(ExecutionEngine.StepListener... listeners) {
        Objects.requireNonNull(listeners, "listeners");
        for (ExecutionEngine.StepListener listener : listeners) {
            Objects.requireNonNull(listener, "listener");
        }
        return new StepListenerChain(Arrays.asList(listeners));
    }

    @Override
    public void onStepStart(int pc, String line) {
        for (ExecutionEngine.StepListener listener : listeners) {
            listener.onStepStart(pc, line);
        }
    }

    @Override
    public void onStepEnd(int pc, String line, String state) {
        for (ExecutionEngine.StepListener listener : listeners) {
            listener.onStepEnd(pc, line, state);
        }
    }

    @Override
    public void onError(int pc, String line, String message) {
        for (ExecutionEngine.StepListener listener : listeners) {
            listener.onError(pc, line, message);
        }
    }

    @Override
    public void onExecutionComplete(int totalSteps, int variablesCount) {
        for (ExecutionEngine.StepListener listener : listeners) {
            listener.onExecutionComplete(totalSteps, variablesCount);
        }
    }
}
