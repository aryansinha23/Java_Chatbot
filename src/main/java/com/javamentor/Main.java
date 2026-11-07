package com.javamentor;

import com.javamentor.engine.ChatbotEngine;
import com.javamentor.engine.StrategyFactory;
import com.javamentor.model.MentorResponse;
import com.javamentor.ui.ConsoleUI;

/**
 * Main application entrypoint for Java Concept & Code Mentor.
 */
public class Main {

    public static void main(String[] args) {
        ChatbotEngine engine = new ChatbotEngine();

        // Parse optional CLI arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printCliUsage();
                return;
            }

            if ("--mode".equalsIgnoreCase(arg) && i + 1 < args.length) {
                String modeStr = args[++i].toUpperCase();
                try {
                    engine.setCurrentMode(StrategyFactory.EngineMode.valueOf(modeStr));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid mode: " + modeStr + ". Using AUTO.");
                }
            }

            if ("--benchmark".equalsIgnoreCase(arg)) {
                int count = 3;
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    try {
                        count = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                try {
                    var results = engine.runBenchmark(count);
                    System.out.println("Autonomous benchmark executed with " + results.size() + " queries successfully.");
                } catch (Exception e) {
                    System.err.println("Benchmark failed: " + e.getMessage());
                    System.exit(1);
                }
                return;
            }

            if ("--query".equalsIgnoreCase(arg) && i + 1 < args.length) {
                String query = args[++i];
                try {
                    MentorResponse response = engine.processQuery(query);
                    System.out.println(response.toCanonicalString());
                    if (response.getAuditReport() != null) {
                        System.out.println(response.getAuditReport().toSummary());
                    }
                } catch (Exception e) {
                    System.err.println("Query execution failed: " + e.getMessage());
                    System.exit(1);
                }
                return;
            }
        }

        // Launch continuous interactive UI
        ConsoleUI ui = new ConsoleUI(engine);
        ui.start();
    }

    private static void printCliUsage() {
        System.out.println("Java Concept & Code Mentor - CLI Usage:");
        System.out.println("  java -jar java-concept-mentor.jar                 # Interactive mode");
        System.out.println("  java -jar java-concept-mentor.jar --mode OFFLINE  # Force offline knowledge engine");
        System.out.println("  java -jar java-concept-mentor.jar --mode ONLINE   # Force Google Gemini engine");
        System.out.println("  java -jar java-concept-mentor.jar --benchmark 5   # Run autonomous training benchmark");
        System.out.println("  java -jar java-concept-mentor.jar --query \"How do I use Stream API?\" # Single shot query");
    }
}
