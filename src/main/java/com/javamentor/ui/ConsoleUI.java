package com.javamentor.ui;

import com.javamentor.command.CommandContext;
import com.javamentor.command.CommandInvoker;
import com.javamentor.engine.ChatbotEngine;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.javamentor.ui.AnsiTheme.*;

/**
 * Intuitive Terminal Command-Line Interface (CLI).
 * Features rich visual styling, command shortcuts, session management,
 * and real-time multi-agent audit reporting.
 */
public class ConsoleUI {

    private final ChatbotEngine engine;
    private final CommandInvoker commandInvoker;
    private final Scanner scanner;
    private boolean running;

    public ConsoleUI(ChatbotEngine engine) {
        this.engine = engine;
        this.commandInvoker = new CommandInvoker();
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
                String[] parts = input.split("\\s+", 2);
                String arg = parts.length > 1 ? parts[1].trim() : "";
                CommandContext ctx = new CommandContext(engine, arg, this::clearScreen);
                running = commandInvoker.dispatch(input, ctx);
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

