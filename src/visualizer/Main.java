package visualizer;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launch the graphical UI
        SwingUtilities.invokeLater(() -> {
            VisualizerUI ui = new VisualizerUI();
            ui.setVisible(true);
        });
    }
}
