package visualizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LearningCenter provides tips on improving code and fun memorization techniques.
 */
public class LearningCenter extends JPanel {

    private java.util.function.Consumer<String> codeLoader;

    public LearningCenter(java.util.function.Consumer<String> codeLoader) {
        this.codeLoader = codeLoader;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(30, 30, 30));
        
        JEditorPane content = new JEditorPane();
        content.setEditable(false);
        content.setContentType("text/html");
        content.setBackground(new Color(30, 30, 30));
        
        String htmlContent = "<html>" +
            "<body style='font-family: \"Inter\", sans-serif; padding: 25px; color: #d4d4d4; background-color: #1e1e1e;'>" +
            "    <div style='text-align: center; margin-bottom: 25px;'>" +
            "        <h1 style='color: #ae0b05; margin-bottom: 5px; font-size: 24px;'>🎓 Code Mastery Hub</h1>" +
            "        <p style='color: #888888; font-size: 13px;'>Master the art of writing and remembering clean code.</p>" +
            "    </div>" +
            "    <div style='background-color: #252526; padding: 15px; border-radius: 12px; margin-bottom: 20px; border-left: 5px solid #ae0b05;'>" +
            "        <h3 style='color: #ffffff; margin-top: 0; font-size: 16px;'>🚀 Clean Code Tips</h3>" +
            "        <div style='font-size: 12px;'>Focus on describing intent rather than just steps. Reusable components and single-responsibility functions are key.</div>" +
            "    </div>" +
            "</body>" +
            "</html>";
             
        content.setText(htmlContent);
        container.add(content);

        // Example Library
        JPanel libraryPanel = new JPanel(new GridLayout(0, 1, 5, 8));
        libraryPanel.setBackground(new Color(30, 30, 30));
        libraryPanel.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        JLabel libLabel = new JLabel("✨ EXPLORE EXAMPLES");
        libLabel.setFont(new Font("Inter", Font.BOLD, 12));
        libLabel.setForeground(new Color(150, 150, 150));
        libLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(libLabel);

        addExampleButton(libraryPanel, "Factorial (while)", "// Calculate 5!\nint n = 5;\nint fact = 1;\nwhile (n > 0) {\n    fact = fact * n;\n    n = n - 1;\n}\n");
        addExampleButton(libraryPanel, "Variable Swap", "// Swap two variables\nint a = 10;\nint b = 20;\nint temp = a;\na = b;\nb = temp;\n");
        addExampleButton(libraryPanel, "Sum of First N", "// Sum of first 5 numbers\nint n = 5;\nint sum = 0;\nint i = 1;\nwhile (i <= n) {\n    sum = sum + i;\n    i = i + 1;\n}\n");
        addExampleButton(libraryPanel, "If-Else Branching", "// Basic branching\nint score = 85;\nint threshold = 80;\nif (score > threshold) {\n    int passed = 1;\n}\n");

        container.add(libraryPanel);
        
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addExampleButton(JPanel panel, String label, String code) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setBackground(new Color(45, 45, 48));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> codeLoader.accept(code));
        panel.add(btn);
    }
}
