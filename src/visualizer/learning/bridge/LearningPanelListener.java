package visualizer.learning.bridge;

/**
 * Receives learning panel updates published by {@link StepListenerBridge}.
 */
public interface LearningPanelListener {

    /**
     * Called when the bridge has new data for the Learning Center UI.
     *
     * @param snapshot immutable panel view-model
     */
    void onPanelUpdate(LearningPanelSnapshot snapshot);

    /**
     * Called when a new learning session begins and the panel should reset presentation state.
     */
    void onSessionReset();
}
