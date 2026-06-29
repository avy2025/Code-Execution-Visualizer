package visualizer;

import visualizer.learning.bridge.LearningPanelSnapshot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Dockable learning panel with live execution insights driven by the learning bridge.
 */
public class LearningCenter extends JPanel {

    private static final Color COLOR_BG = new Color(30, 30, 30);
    private static final Color COLOR_PANEL = new Color(37, 37, 38);
    private static final Color COLOR_TEXT = new Color(212, 212, 212);
    private static final Color COLOR_MUTED = new Color(150, 150, 150);
    private static final Color COLOR_BORDER = new Color(63, 63, 70);
    private static final Color COLOR_ACCENT = new Color(174, 11, 5);

    private final JLabel lineNumberLabel;
    private final JLabel statementLabel;
    private final JTextArea variablesArea;
    private final JTextArea explanationArea;
    private final JTextArea hintArea;
    private final JLabel quizQuestionLabel;
    private final ButtonGroup quizOptionGroup;
    private final JRadioButton[] quizOptionButtons = new JRadioButton[4];
    private final JLabel quizAnswerLabel;
    private final JButton revealAnswerButton;
    private final JLabel stepsLabel;
    private final JLabel hintsLabel;
    private final JLabel quizzesLabel;
    private final JLabel errorsLabel;

    /**
     * Creates the dockable learning panel.
     */
    public LearningCenter() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(340, 0));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_BG);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        lineNumberLabel = createValueLabel();
        statementLabel = createValueLabel();
        variablesArea = createTextArea(3);

        container.add(createSection("Current Step", buildCurrentStepPanel()));
        container.add(Box.createVerticalStrut(8));

        explanationArea = createTextArea(4);
        container.add(createSection("AI Explanation", wrapReadOnly(explanationArea)));
        container.add(Box.createVerticalStrut(8));

        hintArea = createTextArea(3);
        container.add(createSection("Hint", wrapReadOnly(hintArea)));
        container.add(Box.createVerticalStrut(8));

        quizQuestionLabel = createValueLabel();
        quizOptionGroup = new ButtonGroup();
        quizAnswerLabel = createValueLabel();
        revealAnswerButton = createAccentButton("Reveal Answer");
        revealAnswerButton.addActionListener(e -> quizAnswerLabel.setVisible(true));
        container.add(createSection("Quick Quiz", buildQuizPanel()));
        container.add(Box.createVerticalStrut(8));

        stepsLabel = createValueLabel();
        hintsLabel = createValueLabel();
        quizzesLabel = createValueLabel();
        errorsLabel = createValueLabel();
        container.add(createSection("Learning Progress", buildProgressPanel()));

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        resetPresentation();
    }

    /**
     * Applies a bridge-published snapshot to the panel.
     *
     * @param snapshot immutable view-model
     */
    public void applySnapshot(LearningPanelSnapshot snapshot) {
        lineNumberLabel.setText(String.valueOf(snapshot.getLineNumber()));
        statementLabel.setText(snapshot.getStatement().isEmpty() ? "—" : snapshot.getStatement());
        variablesArea.setText(snapshot.getVariablesText());
        explanationArea.setText(snapshot.getExplanationText());
        hintArea.setText(
                snapshot.getHintText() == null || snapshot.getHintText().isBlank()
                        ? "No hint available."
                        : snapshot.getHintText());

        quizQuestionLabel.setText(snapshot.getQuizQuestion());
        List<String> options = snapshot.getQuizOptions();
        for (int i = 0; i < quizOptionButtons.length; i++) {
            String option = i < options.size() ? options.get(i) : "—";
            quizOptionButtons[i].setText(option);
            quizOptionButtons[i].setVisible(true);
        }
        quizAnswerLabel.setText("Answer: " + snapshot.getQuizAnswer());
        quizAnswerLabel.setVisible(false);

        stepsLabel.setText("Total execution steps: " + snapshot.getTotalSteps());
        hintsLabel.setText("Hints generated: " + snapshot.getHintsGenerated());
        quizzesLabel.setText("Quizzes generated: " + snapshot.getQuizzesGenerated());
        errorsLabel.setText("Errors encountered: " + snapshot.getErrorsEncountered());
    }

    /**
     * Resets transient presentation state for a new session.
     */
    public void resetPresentation() {
        quizAnswerLabel.setVisible(false);
        quizOptionGroup.clearSelection();
        explanationArea.setText("Waiting for execution...");
        hintArea.setText("No hint available.");
    }

    /**
     * Shows a friendly fallback when the panel cannot render learning content.
     */
    public void showFriendlyError() {
        explanationArea.setText("Learning content is temporarily unavailable.");
        hintArea.setText("No hint available.");
    }

    private JPanel buildCurrentStepPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.add(createFieldRow("Line", lineNumberLabel));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createFieldRow("Statement", statementLabel));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createFieldLabel("Variables"));
        panel.add(wrapReadOnly(variablesArea));
        return panel;
    }

    private JPanel buildQuizPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.add(createFieldLabel("Question"));
        panel.add(quizQuestionLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(createFieldLabel("Options"));
        for (int i = 0; i < quizOptionButtons.length; i++) {
            quizOptionButtons[i] = createQuizOption("—");
            quizOptionGroup.add(quizOptionButtons[i]);
            panel.add(quizOptionButtons[i]);
        }
        panel.add(Box.createVerticalStrut(8));
        panel.add(revealAnswerButton);
        panel.add(Box.createVerticalStrut(6));
        quizAnswerLabel.setForeground(new Color(46, 204, 113));
        panel.add(quizAnswerLabel);
        return panel;
    }

    private JPanel buildProgressPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.add(stepsLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hintsLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(quizzesLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(errorsLabel);
        return panel;
    }

    private JPanel createSection(String title, JComponent content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(COLOR_PANEL);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(8, 8, 8, 8)));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), title);
        titledBorder.setTitleColor(COLOR_TEXT);
        titledBorder.setTitleFont(new Font("Inter", Font.BOLD, 12));
        section.setBorder(BorderFactory.createCompoundBorder(titledBorder, new EmptyBorder(8, 8, 8, 8)));
        section.add(content, BorderLayout.CENTER);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    private JPanel createFieldRow(String label, JComponent value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(createFieldLabel(label), BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 11));
        label.setForeground(COLOR_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("—");
        label.setFont(new Font("Inter", Font.PLAIN, 13));
        label.setForeground(COLOR_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Inter", Font.PLAIN, 13));
        area.setBackground(new Color(25, 25, 25));
        area.setForeground(COLOR_TEXT);
        area.setBorder(new EmptyBorder(6, 6, 6, 6));
        return area;
    }

    private JScrollPane wrapReadOnly(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scroll.getViewport().setBackground(new Color(25, 25, 25));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private JRadioButton createQuizOption(String text) {
        JRadioButton option = new JRadioButton(text);
        option.setOpaque(false);
        option.setFont(new Font("Inter", Font.PLAIN, 13));
        option.setForeground(COLOR_TEXT);
        option.setAlignmentX(Component.LEFT_ALIGNMENT);
        return option;
    }

    private JButton createAccentButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setBackground(COLOR_ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }
}
