package com.javamentor.ui;

import com.javamentor.agent.SyntheticStudentAgent;
import com.javamentor.engine.ChatbotEngine;
import com.javamentor.engine.StrategyFactory;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import static com.javamentor.ui.AnsiTheme.*;

/**
 * Intuitive Terminal Command-Line Interface (CLI).
 * Features rich visual styling, command shortcuts, session management,
 * and real-time multi-agent audit reporting.
 */
public class ConsoleUI {

    private final ChatbotEngine engine;
    private final Scanner scanner;
    private boolean running;

    public ConsoleUI(ChatbotEngine engine) {
        this.engine = engine;
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        this.running = true;
    }

    public void start() {
        printBanner();

        while (running) {
            System.out.print(bold(brightCyan("\nJavaMentor > ")));
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.startsWith("/")) {
                handleCommand(input);
            } else {
                handleQuery(input);
            }
        }

        printGoodbye();
    }

    private void printBanner() {
        System.out.println(brightCyan("================================================================================"));
        System.out.println(bold(brightCyan("          JAVA CONCEPT & CODE MENTOR - MULTI-STAGE AI ASSISTANT")));
        System.out.println(cyan("      Autonomous Multi-Stage Java AI Chatbot & Multi-Agent Training System"));
        System.out.println(brightCyan("================================================================================"));
        System.out.println("  * Active Engine Mode   : " + bold(engine.getCurrentMode().name()));
        System.out.println("  * Active Strategy      : " + engine.getActiveEngineDescription());
        System.out.println("  * Google Gemini Key    : "
                + (engine.isOnlineAvailable() ? brightGreen("CONFIGURED (Online Ready)") : yellow("NOT FOUND (Using Offline Engine)")));
        System.out.println("  * Multi-Agent Auditor  : " + (engine.isAuditDisplayEnabled() ? brightGreen("ENABLED") : yellow("DISABLED")));
        System.out.println("  * Type " + bold("/help") + " for commands, or enter any Java question to begin.");
        System.out.println(brightCyan("--------------------------------------------------------------------------------"));
    }

    private void handleCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        switch (cmd) {
            case "/help":
                printHelp();
                break;

            case "/mode":
                setMode(arg);
                break;

            case "/audit":
                toggleAudit(arg);
                break;

            case "/benchmark":
                runBenchmark(arg);
                break;

            case "/history":
                showHistory();
                break;

            case "/export":
                exportTranscript(arg);
                break;

            case "/clear":
                clearScreen();
                break;

            case "/exit":
            case "/quit":
                running = false;
                break;

            default:
                System.out.println(red("Unknown command: " + cmd + ". Type /help to see all available commands."));
        }
    }

    private void printHelp() {
        System.out.println(bold("\nAvailable Commands:"));
        System.out.println("  " + bold("/help") + "                 : Display this command reference.");
        System.out.println("  " + bold("/mode [auto|online|offline]") + " : Switch intelligence engine mode.");
        System.out.println("  " + bold("/benchmark [count]") + "  : Run autonomous Student-Agent training benchmark simulation.");
        System.out.println("  " + bold("/audit [on|off]") + "       : Toggle display of the AI Critic/Auditor 100-mark report.");
        System.out.println("  " + bold("/history") + "              : View queries and audit scores from current session.");
        System.out.println("  " + bold("/export [file.md]") + "    : Export complete conversation session to Markdown.");
        System.out.println("  " + bold("/clear") + "                : Clear the terminal console.");
        System.out.println("  " + bold("/exit") + " or " + bold("/quit") + "       : Exit the mentor session.");
        System.out.println("\nTip: Ask any technical Java question directly (e.g., 'How do I create a Thread with Runnable?')");
    }

    private void setMode(String arg) {
        if (arg.equalsIgnoreCase("online")) {
            if (!engine.isOnlineAvailable()) {
                System.out.println(red("[Error] Cannot set mode to ONLINE: GEMINI_API_KEY environment variable is missing."));
                System.out.println(yellow("Set it in PowerShell via: $env:GEMINI_API_KEY=\"your_key_here\""));
                return;
            }
            engine.setCurrentMode(StrategyFactory.EngineMode.ONLINE);
            System.out.println(green("[Mode Changed] Switched to ONLINE mode (Google Gemini API)."));
        } else if (arg.equalsIgnoreCase("offline")) {
            engine.setCurrentMode(StrategyFactory.EngineMode.OFFLINE);
            System.out.println(green("[Mode Changed] Switched to OFFLINE mode (Curated Pattern Engine)."));
        } else if (arg.equalsIgnoreCase("auto") || arg.isEmpty()) {
            engine.setCurrentMode(StrategyFactory.EngineMode.AUTO);
            System.out.println(green("[Mode Changed] Switched to AUTO mode (Uses Gemini if key present, else Offline)."));
        } else {
            System.out.println(red("Invalid mode: '" + arg + "'. Options are: auto, online, offline."));
        }
    }

    private void toggleAudit(String arg) {
        if (arg.equalsIgnoreCase("on")) {
            engine.setAuditDisplayEnabled(true);
            System.out.println(green("Auditor Agent display is now ENABLED."));
        } else if (arg.equalsIgnoreCase("off")) {
            engine.setAuditDisplayEnabled(false);
            System.out.println(yellow("Auditor Agent display is now DISABLED."));
        } else {
            engine.setAuditDisplayEnabled(!engine.isAuditDisplayEnabled());
            System.out.println(green("Auditor Agent display toggled to: " + (engine.isAuditDisplayEnabled() ? "ENABLED" : "DISABLED")));
        }
    }

    private void runBenchmark(String arg) {
        int count = 3; // default
        if (!arg.isEmpty()) {
            try {
                count = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                System.out.println(yellow("Invalid count. Running default 3 queries."));
            }
        }

        System.out.println(bold(brightCyan("\n--- Starting Autonomous Multi-Agent Training Benchmark ---")));
        System.out.println("The Synthetic Student Agent will quiz the Mentor Agent, and the Auditor will evaluate results.");

        try {
            List<MentorResponse> results = engine.runBenchmark(count);
            int totalScore = 0;
            System.out.println(bold(brightGreen("\n=== Benchmark Summary Results ===")));
            for (int i = 0; i < results.size(); i++) {
                MentorResponse resp = results.get(i);
                int score = resp.getAuditReport() != null ? resp.getAuditReport().getRubricScore() : 0;
                totalScore += score;
                System.out.printf("  [%d] Score: %d/100 | Time: %dms | Query: %s%n",
                        i + 1, score, resp.getResponseTimeMs(), resp.getQuery());
            }
            int avgScore = totalScore / Math.max(1, results.size());
            System.out.printf(bold("\nAverage Benchmark Score: %d/100 [%s]%n"),
                    avgScore, avgScore >= 75 ? brightGreen("EXCELLENT") : yellow("ACCEPTABLE"));
        } catch (Exception e) {
            System.out.println(red("[Benchmark Error] " + e.getMessage()));
        }
    }

    private void showHistory() {
        List<MentorResponse> history = engine.getSessionHistory();
        if (history.isEmpty()) {
            System.out.println(yellow("No queries processed in this session yet."));
            return;
        }

        System.out.println(bold("\nSession Query History (" + history.size() + " total):"));
        for (int i = 0; i < history.size(); i++) {
            MentorResponse resp = history.get(i);
            int score = resp.getAuditReport() != null ? resp.getAuditReport().getRubricScore() : 0;
            System.out.printf("  %d. [%d/100] %s (%s, %dms)%n",
                    i + 1, score, resp.getQuery(), resp.getSourceEngine(), resp.getResponseTimeMs());
        }
    }

    private void exportTranscript(String filename) {
        List<MentorResponse> history = engine.getSessionHistory();
        if (history.isEmpty()) {
            System.out.println(yellow("Cannot export: No session history available."));
            return;
        }

        if (filename == null || filename.isBlank()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            filename = "transcript-" + timestamp + ".md";
        }

        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            writer.write("# Java Concept & Code Mentor - Session Transcript\n\n");
            writer.write("Generated on: " + LocalDateTime.now() + "\n\n");
            writer.write("---\n\n");

            for (int i = 0; i < history.size(); i++) {
                MentorResponse resp = history.get(i);
                writer.write("## Dialogue " + (i + 1) + "\n\n");
                writer.write(resp.toCanonicalString());
                if (resp.getAuditReport() != null) {
                    writer.write("\n```\n" + resp.getAuditReport().toSummary() + "```\n");
                }
                writer.write("\n---\n\n");
            }

            System.out.println(brightGreen("Successfully exported session transcript to: " + filename));
        } catch (IOException e) {
            System.out.println(red("Failed to export transcript: " + e.getMessage()));
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        printBanner();
    }

    private void handleQuery(String query) {
        System.out.println(dim("Thinking and synthesizing 3-phase pedagogical response..."));
        try {
            MentorResponse response = engine.processQuery(query);
            renderResponse(response);
        } catch (Exception e) {
            System.out.println(red("\n[Error processing inquiry] " + e.getMessage()));
            if (e.getMessage() != null && e.getMessage().contains("GEMINI_API_KEY")) {
                System.out.println(yellow("Tip: You can switch to offline mode anytime using `/mode offline`."));
            }
        }
    }

    private void renderResponse(MentorResponse response) {
        System.out.println();
        System.out.println(brightCyan("================================================================================"));
        System.out.println(bold(brightCyan("[PHASE 1: CONCEPTUAL BREAKDOWN]")));
        System.out.println(brightCyan("================================================================================"));
        System.out.println(response.getPhase1Conceptual());

        System.out.println();
        System.out.println(brightYellow("================================================================================"));
        System.out.println(bold(brightYellow("[PHASE 2: ALGORITHMIC BLUEPRINT]")));
        System.out.println(brightYellow("================================================================================"));
        System.out.println(response.getPhase2Algorithmic());

        System.out.println();
        System.out.println(brightGreen("================================================================================"));
        System.out.println(bold(brightGreen("[PHASE 3: JAVA IMPLEMENTATION & EXECUTION]")));
        System.out.println(brightGreen("================================================================================"));
        System.out.println(response.getPhase3Code());

        // Display Multi-Agent Auditor Evaluation Card
        if (engine.isAuditDisplayEnabled() && response.getAuditReport() != null) {
            AuditReport audit = response.getAuditReport();
            System.out.println();
            System.out.println(magenta("--------------------------------------------------------------------------------"));
            System.out.println(bold(magenta("Autonomous Auditor AI Agent Inspection:")));
            System.out.printf("  Rubric Score: %s/100 | Status: %s | Response Time: %dms%n",
                    bold(String.valueOf(audit.getRubricScore())),
                    audit.isPassed() ? brightGreen("PASSED") : yellow("REFINED"),
                    response.getResponseTimeMs());
            System.out.println("  Source Engine: " + response.getSourceEngine());
            if (!audit.getFeedbackNotes().isEmpty()) {
                System.out.println("  Auditor Notes:");
                for (String note : audit.getFeedbackNotes()) {
                    System.out.println("    * " + note);
                }
            }
            System.out.println(magenta("--------------------------------------------------------------------------------"));
        }
    }

    private void printGoodbye() {
        System.out.println(brightCyan("\nThank you for using Java Concept & Code Mentor. Session terminated gracefully."));
    }
}
