package com.javamentor.ui.gui;

import com.javamentor.engine.ChatbotEngine;
import com.javamentor.engine.CodeExecutionEngine;
import com.javamentor.engine.StrategyFactory;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;
import com.javamentor.model.TopicContent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Desktop Graphical User Interface (GUI) for Java Concept & Code Mentor.
 * Fulfills the "via Terminal CLI or GUI" requirement from the assignment specification.
 */
public class MentorGui extends JFrame {

    private final ChatbotEngine engine;
    private final CodeExecutionEngine codeExecutionEngine;

    private JComboBox<String> modeCombo;
    private JComboBox<String> sampleTopicsCombo;
    private JTextField queryInput;
    private JButton askButton;
    private JButton runCodeButton;
    private JButton copyCodeButton;
    private JButton exportButton;
    private JButton benchmarkButton;

    private JTextArea phase1Area;
    private JTextArea phase2Area;
    private JTextArea phase3Area;
    private JTextArea consoleArea;
    private JLabel statusLabel;
    private JLabel scoreLabel;

    private MentorResponse currentResponse;

    public MentorGui(ChatbotEngine engine) {
        super("Java Concept & Code Mentor - Multi-Stage AI Assistant");
        this.engine = engine;
        this.codeExecutionEngine = new CodeExecutionEngine();
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setMinimumSize(new Dimension(850, 650));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Control Panel
        mainPanel.add(buildTopPanel(), BorderLayout.NORTH);

        // Center Content Tabs
        mainPanel.add(buildCenterPanel(), BorderLayout.CENTER);

        // Bottom Status Bar
        mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // Row 1: Mode & Actions
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.add(new JLabel("Intelligence Engine:"));
        modeCombo = new JComboBox<>(new String[]{"AUTO (Gemini if key exists)", "ONLINE (Google Gemini API)", "OFFLINE (Knowledge Base)"});
        modeCombo.addActionListener(e -> {
            int idx = modeCombo.getSelectedIndex();
            if (idx == 1) {
                if (!engine.isOnlineAvailable()) {
                    JOptionPane.showMessageDialog(this,
                            "GEMINI_API_KEY environment variable is not set.\nPlease set it to use Online mode.",
                            "API Key Missing", JOptionPane.WARNING_MESSAGE);
                    modeCombo.setSelectedIndex(0);
                    return;
                }
                engine.setCurrentMode(StrategyFactory.EngineMode.ONLINE);
            } else if (idx == 2) {
                engine.setCurrentMode(StrategyFactory.EngineMode.OFFLINE);
            } else {
                engine.setCurrentMode(StrategyFactory.EngineMode.AUTO);
            }
            updateStatusText();
        });
        row1.add(modeCombo);

        benchmarkButton = new JButton("Run Multi-Agent Benchmark");
        benchmarkButton.addActionListener(e -> runBenchmarkDialog());
        row1.add(benchmarkButton);

        exportButton = new JButton("Export Session (.md)");
        exportButton.addActionListener(e -> exportTranscriptDialog());
        row1.add(exportButton);

        // Row 2: Preset samples dropdown
        JPanel row2 = new JPanel(new BorderLayout(6, 6));
        JPanel samplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        samplePanel.add(new JLabel("Sample Inquiries:"));
        sampleTopicsCombo = new JComboBox<>();
        sampleTopicsCombo.addItem("-- Select a core Java inquiry --");
        populateSampleTopics();
        sampleTopicsCombo.addActionListener(e -> {
            String selected = (String) sampleTopicsCombo.getSelectedItem();
            if (selected != null && !selected.startsWith("--")) {
                queryInput.setText("How do I implement " + selected + " in Java?");
            }
        });
        samplePanel.add(sampleTopicsCombo);
        row2.add(samplePanel, BorderLayout.NORTH);

        // Row 3: Query Input & Ask Button
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        queryInput = new JTextField();
        queryInput.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        queryInput.addActionListener(e -> submitQuery());

        askButton = new JButton("Ask Java Mentor");
        askButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        askButton.setBackground(new Color(25, 118, 210));
        askButton.setForeground(Color.WHITE);
        askButton.addActionListener(e -> submitQuery());

        inputPanel.add(queryInput, BorderLayout.CENTER);
        inputPanel.add(askButton, BorderLayout.EAST);
        row2.add(inputPanel, BorderLayout.SOUTH);

        panel.add(row1, BorderLayout.NORTH);
        panel.add(row2, BorderLayout.SOUTH);
        return panel;
    }

    private void populateSampleTopics() {
        String[] samples = {
                "Threads using Runnable",
                "Stream API with filter, map, and groupingBy",
                "OOP Polymorphism and Interface Abstraction",
                "HashMap collision resolution and treeification",
                "Thread-safe Producer-Consumer with BlockingQueue",
                "Double-Checked Locking Singleton with volatile",
                "Java Generics and Bounded Wildcards (PECS)",
                "Custom Checked vs Unchecked Exceptions",
                "Lock-free programming with AtomicInteger and CAS",
                "CompletableFuture asynchronous pipelining",
                "Java NIO.2 File Path and atomic streaming",
                "Reflection API dynamic method invocation",
                "Java Records canonical constructor validation",
                "Pattern matching for instanceof and switch",
                "JVM Memory Heap, Stack, and Garbage Collection"
        };
        for (String s : samples) {
            sampleTopicsCombo.addItem(s);
        }
    }

    private JComponent buildCenterPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Phase 1 Tab
        phase1Area = createTextArea();
        tabbedPane.addTab("Phase 1: Conceptual Breakdown", new JScrollPane(phase1Area));

        // Phase 2 Tab
        phase2Area = createTextArea();
        tabbedPane.addTab("Phase 2: Algorithmic Blueprint", new JScrollPane(phase2Area));

        // Phase 3 Tab (Code & Execution)
        JPanel phase3Panel = new JPanel(new BorderLayout(6, 6));
        phase3Area = createTextArea();
        phase3Area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        phase3Area.setBackground(new Color(30, 30, 30));
        phase3Area.setForeground(new Color(220, 220, 220));
        phase3Area.setCaretColor(Color.WHITE);

        JPanel codeToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        copyCodeButton = new JButton("Copy Code to Clipboard");
        copyCodeButton.addActionListener(e -> {
            String code = phase3Area.getText();
            if (!code.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
                JOptionPane.showMessageDialog(this, "Java code copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        codeToolbar.add(copyCodeButton);

        runCodeButton = new JButton("Execute Code Live (Integrated Test Suite)");
        runCodeButton.setBackground(new Color(46, 125, 50));
        runCodeButton.setForeground(Color.WHITE);
        runCodeButton.addActionListener(e -> executeCurrentCode());
        codeToolbar.add(runCodeButton);

        phase3Panel.add(codeToolbar, BorderLayout.NORTH);

        JSplitPane codeSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        codeSplit.setTopComponent(new JScrollPane(phase3Area));

        consoleArea = createTextArea();
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        consoleArea.setBackground(new Color(20, 20, 20));
        consoleArea.setForeground(new Color(130, 240, 130));
        JScrollPane consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setBorder(new TitledBorder("Live Execution Output"));
        codeSplit.setBottomComponent(consoleScroll);
        codeSplit.setResizeWeight(0.65);

        phase3Panel.add(codeSplit, BorderLayout.CENTER);
        tabbedPane.addTab("Phase 3: Java Implementation & Execution", phase3Panel);

        return tabbedPane;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(8, 4));
        bottom.setBorder(new EmptyBorder(4, 4, 4, 4));

        statusLabel = new JLabel();
        updateStatusText();
        bottom.add(statusLabel, BorderLayout.WEST);

        scoreLabel = new JLabel("Auditor Rubric Score: --/100");
        scoreLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        bottom.add(scoreLabel, BorderLayout.EAST);

        return bottom;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setMargin(new Insets(10, 10, 10, 10));
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        return area;
    }

    private void updateStatusText() {
        boolean online = engine.isOnlineAvailable();
        String mode = engine.getCurrentMode().name();
        statusLabel.setText(String.format("Mode: %s | Active: %s | Gemini Key: %s",
                mode, engine.getActiveEngineDescription(), online ? "Configured" : "Offline Catalog"));
    }

    private void submitQuery() {
        String query = queryInput.getText().trim();
        if (query.isBlank()) return;

        askButton.setEnabled(false);
        askButton.setText("Thinking...");

        SwingWorker<MentorResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected MentorResponse doInBackground() throws Exception {
                return engine.processQuery(query);
            }

            @Override
            protected void done() {
                askButton.setEnabled(true);
                askButton.setText("Ask Java Mentor");
                try {
                    currentResponse = get();
                    displayResponse(currentResponse);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MentorGui.this,
                            "Error processing query: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void displayResponse(MentorResponse response) {
        phase1Area.setText(response.getPhase1Conceptual());
        phase1Area.setCaretPosition(0);

        phase2Area.setText(response.getPhase2Algorithmic());
        phase2Area.setCaretPosition(0);

        phase3Area.setText(response.getPhase3Code());
        phase3Area.setCaretPosition(0);

        consoleArea.setText("Ready to run. Click 'Execute Code Live' to compile and execute the test harness.");

        AuditReport report = response.getAuditReport();
        if (report != null) {
            String badge = report.isPassed() ? "PASSED" : "REFINED";
            Color c = report.isPassed() ? new Color(46, 125, 50) : new Color(230, 81, 0);
            scoreLabel.setText(String.format("Auditor Rubric Score: %d/100 [%s] (%dms, %s)",
                    report.getRubricScore(), badge, response.getResponseTimeMs(), response.getSourceEngine()));
            scoreLabel.setForeground(c);
        }
    }

    private void executeCurrentCode() {
        if (currentResponse == null || currentResponse.getPhase3Code().isBlank()) {
            JOptionPane.showMessageDialog(this, "No Java code available to run.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        runCodeButton.setEnabled(false);
        runCodeButton.setText("Compiling & Executing...");

        SwingWorker<CodeExecutionEngine.ExecutionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CodeExecutionEngine.ExecutionResult doInBackground() {
                return codeExecutionEngine.compileAndExecute(currentResponse.getPhase3Code());
            }

            @Override
            protected void done() {
                runCodeButton.setEnabled(true);
                runCodeButton.setText("Execute Code Live (Integrated Test Suite)");
                try {
                    CodeExecutionEngine.ExecutionResult result = get();
                    consoleArea.setText(result.toReport());
                    consoleArea.setCaretPosition(0);
                } catch (Exception ex) {
                    consoleArea.setText("Execution failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void runBenchmarkDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter number of queries for autonomous Student-Agent training simulation:", "3");
        if (input == null || input.isBlank()) return;

        try {
            int count = Integer.parseInt(input.trim());
            benchmarkButton.setEnabled(false);
            benchmarkButton.setText("Simulating...");

            SwingWorker<List<MentorResponse>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<MentorResponse> doInBackground() throws Exception {
                    return engine.runBenchmark(count);
                }

                @Override
                protected void done() {
                    benchmarkButton.setEnabled(true);
                    benchmarkButton.setText("Run Multi-Agent Benchmark");
                    try {
                        List<MentorResponse> res = get();
                        int total = 0;
                        StringBuilder sb = new StringBuilder("=== Benchmark Simulation Results ===\n\n");
                        for (int i = 0; i < res.size(); i++) {
                            MentorResponse r = res.get(i);
                            int score = r.getAuditReport() != null ? r.getAuditReport().getRubricScore() : 0;
                            total += score;
                            sb.append(String.format("[%d] Score: %d/100 | %dms | %s\n", i + 1, score, r.getResponseTimeMs(), r.getQuery()));
                        }
                        int avg = total / Math.max(1, res.size());
                        sb.append(String.format("\nAverage Rubric Score: %d/100\n", avg));
                        JOptionPane.showMessageDialog(MentorGui.this, sb.toString(), "Benchmark Complete", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MentorGui.this, "Benchmark error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportTranscriptDialog() {
        List<MentorResponse> history = engine.getSessionHistory();
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No conversation history to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String defaultName = "transcript-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".md";
        chooser.setSelectedFile(new File(defaultName));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write("# Java Concept & Code Mentor - Session Transcript\n\n");
                writer.write("Generated on: " + LocalDateTime.now() + "\n\n---\n\n");
                for (int i = 0; i < history.size(); i++) {
                    MentorResponse resp = history.get(i);
                    writer.write("## Dialogue " + (i + 1) + "\n\n");
                    writer.write(resp.toCanonicalString());
                    if (resp.getAuditReport() != null) {
                        writer.write("\n```\n" + resp.getAuditReport().toSummary() + "```\n");
                    }
                    writer.write("\n---\n\n");
                }
                JOptionPane.showMessageDialog(this, "Exported successfully to " + file.getName(), "Exported", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void launch(ChatbotEngine engine) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            MentorGui gui = new MentorGui(engine);
            gui.setVisible(true);
        });
    }
}
