package visualizer.learning.ui;

import visualizer.learning.bridge.LearningPanelListener;
import visualizer.learning.bridge.LearningPanelSnapshot;
import visualizer.LearningCenter;

import javax.swing.SwingUtilities;
import java.util.Objects;

/**
 * Mediates between {@link visualizer.learning.bridge.StepListenerBridge} events
 * and the {@link LearningCenter} panel without blocking the Event Dispatch Thread.
 */
public final class LearningCenterController implements LearningPanelListener {

    private final LearningCenter panel;

    /**
     * Creates a controller for the given learning panel.
     *
     * @param panel dockable learning panel to update
     */
    public LearningCenterController(LearningCenter panel) {
        this.panel = Objects.requireNonNull(panel, "panel");
    }

    @Override
    public void onPanelUpdate(LearningPanelSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            try {
                panel.applySnapshot(snapshot);
            } catch (Exception ignored) {
                panel.showFriendlyError();
            }
        });
    }

    @Override
    public void onSessionReset() {
        SwingUtilities.invokeLater(() -> {
            try {
                panel.resetPresentation();
            } catch (Exception ignored) {
                panel.showFriendlyError();
            }
        });
    }
}
