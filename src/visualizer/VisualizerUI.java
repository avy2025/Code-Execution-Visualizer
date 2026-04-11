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
    private ExecutionEngine engine;
    private CodeParser parser;
    private List<String> linesToExecute;
    private Object currentHighlight;

    public VisualizerUI() {
        setTitle("Code Execution Visualizer Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        
        engine = new ExecutionEngine();
        parser = new CodeParser();
        
        setupUI();
        setupCallbacks();
    }

    private void setupUI() {
        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 242, 245));

        // Header
        JLabel header = new JLabel("Code Execution Visualizer", SwingConstants.CENTER);
        header.setFont(new Font("Inter", Font.BOLD, 28));
        header.setForeground(new Color(33, 37, 41));
        mainPanel.add(header, BorderLayout.NORTH);

        // Center split: Input and State Dashboard
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(500);
        mainSplit.setBorder(null);

        // --- Left: Code Input Section ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);
        JLabel inputLabel = new JLabel("Source Code:");
        inputLabel.setFont(new Font("Inter", Font.BOLD, 14));
        
        codeInput = new JTextArea();
        codeInput.setFont(new Font("Fira Code", Font.PLAIN, 15));
        codeInput.setMargin(new Insets(10, 10, 10, 10));
        codeInput.setBackground(Color.WHITE);
        codeInput.setText("// Try if conditions!\nint x = 5;\nint y = 10;\nif (x < y) {\n    x = x + 100;\n}\nint result = x * y;\n");
        
        JScrollPane inputScroll = new JScrollPane(codeInput);
        inputScroll.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224)));
        
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // --- Right: Dashboard Section ---
        JPanel dashboardPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        dashboardPanel.setOpaque(false);

        // Variable Table
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setOpaque(false);
        JLabel tableLabel = new JLabel("Variable Inspector:");
        tableLabel.setFont(new Font("Inter", Font.BOLD, 14));
        
        String[] columnNames = {"Variable", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0);
        variableTable = new JTable(tableModel);
        variableTable.setFont(new Font("Inter", Font.PLAIN, 14));
        variableTable.setRowHeight(25);
        variableTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 14));
        
        JScrollPane tableScroll = new JScrollPane(variableTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224)));
        tablePanel.add(tableLabel, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Execution Log
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setOpaque(false);
        JLabel logLabel = new JLabel("Execution Logs:");
        logLabel.setFont(new Font("Inter", Font.BOLD, 14));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(33, 37, 41));
        logArea.setForeground(new Color(248, 249, 250));
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        dashboardPanel.add(tablePanel);
        dashboardPanel.add(logPanel);

        mainSplit.setLeftComponent(inputPanel);
        mainSplit.setRightComponent(dashboardPanel);
        mainPanel.add(mainSplit, BorderLayout.CENTER);

        // --- Bottom: Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);

        startButton = createStyledButton("Start / Reset", new Color(52, 152, 219));
        nextStepButton = createStyledButton("Next Step →", new Color(46, 204, 113));
        nextStepButton.setEnabled(false);

        controlPanel.add(startButton);
        controlPanel.add(nextStepButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupCallbacks() {
        startButton.addActionListener(e -> resetAndStart());
        nextStepButton.addActionListener(e -> stepForward());

        engine.setStepListener(new ExecutionEngine.StepListener() {
            @Override
            public void onStepStart(int pc, String line) {
                highlightLine(pc);
                logArea.append("PC " + pc + ": " + line + "\n");
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
                clearHighlight();
            }
        });
    }

    private void resetAndStart() {
        String code = codeInput.getText();
        linesToExecute = parser.parseCode(code);
        
        if (linesToExecute.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Empty code area.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        logArea.setText("Parser: " + linesToExecute.size() + " lines identified.\n");
        tableModel.setRowCount(0);
        engine.prepare(linesToExecute);
        
        nextStepButton.setEnabled(true);
        startButton.setText("Restart");
        clearHighlight();
    }

    private void stepForward() {
        boolean hasMore = engine.executeNextStep();
        if (!hasMore) {
            engine.getStepListener().onExecutionComplete(engine.getPC(), engine.getVariableStore().size());
        }
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
                new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 0, 100)));
        } catch (Exception e) {
            // Probably an empty line or out of bounds
        }
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
