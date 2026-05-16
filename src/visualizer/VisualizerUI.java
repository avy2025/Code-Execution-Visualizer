package visualizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * VisualizerUI provides a professional Swing interface for code execution.
 */
public class VisualizerUI extends JFrame {

    private JTextArea codeInput;
    private JTextArea logArea;
    private JTable variableTable;
    private DefaultTableModel tableModel;
    private JButton startButton;
    private JButton nextStepButton;
    private JButton backStepButton;
    private JButton autoPlayButton;
    private Timer autoPlayTimer;
    private FlowchartPanel flowchartPanel;
    private FlowchartGenerator flowchartGenerator;
    private ExecutionEngine engine;
    private CodeParser parser;
    private List<String> linesToExecute;
    private Object currentHighlight;
    private JComboBox<Language> languageSelector;

    // Dark Theme Colors
    private static final Color COLOR_BG = new Color(30, 30, 30);
    private static final Color COLOR_PANEL = new Color(37, 37, 38);
    private static final Color COLOR_TEXT = new Color(212, 212, 212);
    private static final Color COLOR_ACCENT = new Color(174, 11, 5); // Imperial Red
    private static final Color COLOR_BORDER = new Color(63, 63, 70);

    public VisualizerUI() {
        setTitle("Code Execution Visualizer Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        engine = new ExecutionEngine();
        parser = new CodeParser();
        
        setupUI();
        setupCallbacks();
    }

    private void setupUI() {
        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(COLOR_BG);

        // Header
        JLabel header = new JLabel("Code Execution Visualizer Pro", SwingConstants.LEFT);
        header.setFont(new Font("Inter", Font.BOLD, 24));
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 5, 10, 0));
        mainPanel.add(header, BorderLayout.NORTH);

        // Center split: Input and State Dashboard
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(550);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(3);
        mainSplit.setBackground(COLOR_BG);

        // --- Left: Code Input Section ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);
        JLabel inputLabel = new JLabel(" EDITOR");
        inputLabel.setFont(new Font("Inter", Font.BOLD, 12));
        inputLabel.setForeground(new Color(150, 150, 150));
        
        codeInput = new JTextArea();
        codeInput.setFont(new Font("Fira Code", Font.PLAIN, 15));
        codeInput.setCaretColor(Color.WHITE);
        codeInput.setMargin(new Insets(10, 10, 10, 10));
        codeInput.setBackground(new Color(25, 25, 25));
        codeInput.setForeground(new Color(220, 220, 220));
        codeInput.setText("// Try if conditions!\nint x = 5;\nint y = 10;\nif (x < y) {\n    x = x + 100;\n}\nint result = x * y;\n");
        
        JScrollPane inputScroll = new JScrollPane(codeInput);
        inputScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // --- Right: Dashboard Section ---
        JTabbedPane rightTabbedPane = new JTabbedPane();
        rightTabbedPane.setFont(new Font("Inter", Font.BOLD, 13));
        rightTabbedPane.setBackground(COLOR_PANEL);
        rightTabbedPane.setForeground(COLOR_TEXT);

        JPanel dashboardPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        dashboardPanel.setBackground(COLOR_BG);
        dashboardPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Variable Table
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBackground(COLOR_PANEL);
        tablePanel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        JLabel tableLabel = new JLabel("  VARIABLE INSPECTOR");
        tableLabel.setFont(new Font("Inter", Font.BOLD, 11));
        tableLabel.setForeground(new Color(150, 150, 150));
        tableLabel.setPreferredSize(new Dimension(0, 25));
        
        String[] columnNames = {"Variable", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0);
        variableTable = new JTable(tableModel);
        variableTable.setBackground(COLOR_PANEL);
        variableTable.setForeground(COLOR_TEXT);
        variableTable.setGridColor(COLOR_BORDER);
        variableTable.setFont(new Font("Inter", Font.PLAIN, 14));
        variableTable.setRowHeight(30);
        variableTable.getTableHeader().setBackground(new Color(45, 45, 48));
        variableTable.getTableHeader().setForeground(Color.WHITE);
        variableTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        
        JScrollPane tableScroll = new JScrollPane(variableTable);
        tableScroll.getViewport().setBackground(COLOR_PANEL);
        tableScroll.setBorder(null);
        tablePanel.add(tableLabel, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Execution Log
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBackground(COLOR_PANEL);
        logPanel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        JLabel logLabel = new JLabel("  EXECUTION LOG");
        logLabel.setFont(new Font("Inter", Font.BOLD, 11));
        logLabel.setForeground(new Color(150, 150, 150));
        logLabel.setPreferredSize(new Dimension(0, 25));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(25, 25, 25));
        logArea.setForeground(new Color(170, 170, 170));
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        dashboardPanel.add(tablePanel);
        dashboardPanel.add(logPanel);

        // Flowchart Panel
        flowchartPanel = new FlowchartPanel();
        flowchartPanel.setBackground(new Color(25, 25, 25));
        flowchartGenerator = new FlowchartGenerator();
        JScrollPane flowchartScroll = new JScrollPane(flowchartPanel);
        flowchartScroll.setBorder(null);

        rightTabbedPane.addTab("Dashboard", dashboardPanel);
        rightTabbedPane.addTab("Flowchart", flowchartScroll);
        rightTabbedPane.addTab("Learning Center", new LearningCenter());

        mainSplit.setLeftComponent(inputPanel);
        mainSplit.setRightComponent(rightTabbedPane);
        mainPanel.add(mainSplit, BorderLayout.CENTER);

        // --- Bottom: Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setOpaque(false);

        languageSelector = new JComboBox<>(Language.values());
        languageSelector.setFont(new Font("Inter", Font.BOLD, 13));
        languageSelector.setBackground(COLOR_PANEL);
        languageSelector.setForeground(Color.WHITE);
        
        startButton = createStyledButton("Reset", new Color(70, 70, 70));
        backStepButton = createStyledButton("← Back", new Color(60, 60, 60));
        nextStepButton = createStyledButton("Step →", COLOR_ACCENT);
        autoPlayButton = createStyledButton("▶ Play", new Color(46, 204, 113));
        
        backStepButton.setEnabled(false);
        nextStepButton.setEnabled(false);
        autoPlayButton.setEnabled(false);

        controlPanel.add(new JLabel("Lang:"));
        ((JLabel)controlPanel.getComponent(controlPanel.getComponentCount()-1)).setForeground(COLOR_TEXT);
        controlPanel.add(languageSelector);
        controlPanel.add(startButton);
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        controlPanel.add(backStepButton);
        controlPanel.add(nextStepButton);
        controlPanel.add(autoPlayButton);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Setup AutoPlay Timer
        autoPlayTimer = new Timer(800, e -> {
            if (nextStepButton.isEnabled()) {
                stepForward();
            } else {
                stopAutoPlay();
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker()),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupCallbacks() {
        startButton.addActionListener(e -> resetAndStart());
        nextStepButton.addActionListener(e -> stepForward());
        backStepButton.addActionListener(e -> stepBackward());
        autoPlayButton.addActionListener(e -> toggleAutoPlay());

        languageSelector.addActionListener(e -> {
            Language selected = (Language) languageSelector.getSelectedItem();
            if (selected == Language.PYTHON) {
                codeInput.setText("# Python code demo\nx = 10\ny = 20\nif x < y:\n    x = x + 100\nresult = x * y\n");
            } else {
                codeInput.setText("// Java code demo\nint x = 5;\nint y = 10;\nif (x < y) {\n    x = x + 100;\n}\nint result = x * y;\n");
            }
        });

        engine.setStepListener(new ExecutionEngine.StepListener() {
            @Override
            public void onStepStart(int pc, String line) {
                highlightLine(pc);
                logArea.append("PC " + pc + ": " + line + "\n");
                backStepButton.setEnabled(pc > 0);
            }

            @Override
            public void onStepEnd(int pc, String line, String state) {
                updateVariableTable(engine.getVariableStore());
            }

            @Override
            public void onError(int pc, String line, String message) {
                logArea.append("[ERROR at line " + (pc+1) + "] " + message + "\n");
            }

            @Override
            public void onExecutionComplete(int totalSteps, int variablesCount) {
                logArea.append("\nExecution finished.\n");
                nextStepButton.setEnabled(false);
                autoPlayButton.setEnabled(false);
                stopAutoPlay();
                clearHighlight();
            }
        });
    }

    private void resetAndStart() {
        stopAutoPlay();
        String code = codeInput.getText();
        Language lang = (Language) languageSelector.getSelectedItem();
        linesToExecute = parser.parseCode(code, lang);
        
        if (linesToExecute.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Empty code area.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        logArea.setText("Parser: " + linesToExecute.size() + " lines identified.\n");
        tableModel.setRowCount(0);
        
        // Update Flowchart
        try {
            FlowchartNode root = flowchartGenerator.generate(linesToExecute);
            flowchartPanel.setFlowchart(root);
        } catch (Exception ex) {
            logArea.append("[Flowchart Error] " + ex.getMessage() + "\n");
        }

        engine.prepare(linesToExecute, lang);
        
        nextStepButton.setEnabled(true);
        autoPlayButton.setEnabled(true);
        backStepButton.setEnabled(false);
        startButton.setText("Restart");
        clearHighlight();
    }

    private void stepForward() {
        boolean hasMore = engine.executeNextStep();
        if (hasMore) {
            flowchartPanel.setCurrentPC(engine.getPC());
        } else {
            engine.getStepListener().onExecutionComplete(engine.getPC(), engine.getVariableStore().size());
        }
    }

    private void stepBackward() {
        // To be implemented in ExecutionEngine
        logArea.append("Backward stepping coming soon...\n");
    }

    private void toggleAutoPlay() {
        if (autoPlayTimer.isRunning()) {
            stopAutoPlay();
        } else {
            autoPlayButton.setText("⏸ Pause");
            autoPlayButton.setBackground(new Color(231, 76, 60));
            autoPlayTimer.start();
        }
    }

    private void stopAutoPlay() {
        autoPlayTimer.stop();
        autoPlayButton.setText("▶ Play");
        autoPlayButton.setBackground(new Color(46, 204, 113));
    }

    private void updateVariableTable(Map<String, Integer> vars) {
        tableModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : vars.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void highlightLine(int lineIndex) {
        clearHighlight();
        try {
            int start = codeInput.getLineStartOffset(lineIndex);
            int end = codeInput.getLineEndOffset(lineIndex);
            Highlighter h = codeInput.getHighlighter();
            currentHighlight = h.addHighlight(start, end, 
                new DefaultHighlighter.DefaultHighlightPainter(new Color(174, 11, 5, 60)));
        } catch (Exception e) { }
    }

    private void clearHighlight() {
        if (currentHighlight != null) {
            codeInput.getHighlighter().removeHighlight(currentHighlight);
            currentHighlight = null;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new VisualizerUI().setVisible(true));
    }
}
