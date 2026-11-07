package com.javamentor.command;

import com.javamentor.engine.CodeExecutionEngine;
import com.javamentor.engine.StrategyFactory;
import com.javamentor.model.MentorResponse;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.javamentor.ui.AnsiTheme.*;

/**
 * Command Invoker & Registry adhering to the GOF Command Pattern.
 */
public class CommandInvoker {

    private final Map<String, ChatCommand> registry = new HashMap<>();

    public CommandInvoker() {
        registerDefaultCommands();
    }

    public void register(ChatCommand command) {
        registry.put(command.getKeyword().toLowerCase(Locale.ROOT), command);
    }

    public Map<String, ChatCommand> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }

    public boolean dispatch(String input, CommandContext context) {
        String clean = input.startsWith("/") ? input.substring(1) : input;
        String[] parts = clean.split("\\s+", 2);
        String keyword = parts[0].toLowerCase(Locale.ROOT);

        ChatCommand command = registry.get(keyword);
        if (command != null) {
            return command.execute(context);
        } else {
            System.out.println(red("Unknown command: /" + keyword + ". Type /help to see all available commands."));
            return true;
        }
    }

    private void registerDefaultCommands() {
        // /help
        register(new ChatCommand() {
            @Override public String getKeyword() { return "help"; }
            @Override public String getDescription() { return "Display this command reference."; }
            @Override public boolean execute(CommandContext ctx) {
                System.out.println(bold("\nAvailable Commands:"));
                for (ChatCommand cmd : registry.values()) {
                    System.out.printf("  %-30s : %s%n", bold("/" + cmd.getKeyword()), cmd.getDescription());
                }
                System.out.println("\nTip: Type any Java inquiry directly (e.g., 'How does HashMap handle collisions?')");
                return true;
            }
        });

        // /mode [online|offline|auto]
        register(new ChatCommand() {
            @Override public String getKeyword() { return "mode"; }
            @Override public String getDescription() { return "Switch engine mode: /mode [auto|online|offline]"; }
            @Override public boolean execute(CommandContext ctx) {
                String arg = ctx.getArgument().toLowerCase(Locale.ROOT);
                if (arg.equals("online")) {
                    if (!ctx.getEngine().isOnlineAvailable()) {
                        System.out.println(red("[Error] Cannot switch to ONLINE: GEMINI_API_KEY environment variable is missing."));
                        System.out.println(yellow("Set it in PowerShell via: $env:GEMINI_API_KEY=\"your_key_here\""));
                        return true;
                    }
                    ctx.getEngine().setCurrentMode(StrategyFactory.EngineMode.ONLINE);
                    System.out.println(green("[Mode Changed] Switched to ONLINE mode (Google Gemini API)."));
                } else if (arg.equals("offline")) {
                    ctx.getEngine().setCurrentMode(StrategyFactory.EngineMode.OFFLINE);
                    System.out.println(green("[Mode Changed] Switched to OFFLINE mode (Curated Pattern Engine)."));
                } else if (arg.equals("auto") || arg.isEmpty()) {
                    ctx.getEngine().setCurrentMode(StrategyFactory.EngineMode.AUTO);
                    System.out.println(green("[Mode Changed] Switched to AUTO mode (Gemini if key exists, else Offline)."));
                } else {
                    System.out.println(red("Invalid mode: '" + arg + "'. Options: auto, online, offline."));
                }
                return true;
            }
        });

        // /audit [on|off]
        register(new ChatCommand() {
            @Override public String getKeyword() { return "audit"; }
            @Override public String getDescription() { return "Toggle AI Critic 100-mark rubric report: /audit [on|off]"; }
            @Override public boolean execute(CommandContext ctx) {
                String arg = ctx.getArgument().toLowerCase(Locale.ROOT);
                if (arg.equals("on")) {
                    ctx.getEngine().setAuditDisplayEnabled(true);
                    System.out.println(green("Auditor Agent display is now ENABLED."));
                } else if (arg.equals("off")) {
                    ctx.getEngine().setAuditDisplayEnabled(false);
                    System.out.println(yellow("Auditor Agent display is now DISABLED."));
                } else {
                    ctx.getEngine().setAuditDisplayEnabled(!ctx.getEngine().isAuditDisplayEnabled());
                    System.out.println(green("Auditor Agent display toggled to: " + (ctx.getEngine().isAuditDisplayEnabled() ? "ENABLED" : "DISABLED")));
                }
                return true;
            }
        });

        // /benchmark [count]
        register(new ChatCommand() {
            @Override public String getKeyword() { return "benchmark"; }
            @Override public String getDescription() { return "Run autonomous student-agent training benchmark: /benchmark [count]"; }
            @Override public boolean execute(CommandContext ctx) {
                int count = 3;
                if (!ctx.getArgument().isEmpty()) {
                    try {
                        count = Integer.parseInt(ctx.getArgument());
                    } catch (NumberFormatException ignored) {}
                }
                System.out.println(bold(brightCyan("\n--- Starting Autonomous Multi-Agent Training Benchmark ---")));
                System.out.println("The Synthetic Student Agent will quiz the Mentor Agent, and the Auditor will evaluate results.");
                try {
                    List<MentorResponse> results = ctx.getEngine().runBenchmark(count);
                    int totalScore = 0;
                    System.out.println(bold(brightGreen("\n=== Benchmark Summary Results ===")));
                    for (int i = 0; i < results.size(); i++) {
                        MentorResponse resp = results.get(i);
                        int score = resp.getAuditReport() != null ? resp.getAuditReport().getRubricScore() : 0;
                        totalScore += score;
                        System.out.printf("  [%d] Score: %d/100 | Time: %dms | Query: %s%n",
                                i + 1, score, resp.getResponseTimeMs(), resp.getQuery());
                    }
                    int avg = totalScore / Math.max(1, results.size());
                    System.out.printf(bold("\nAverage Benchmark Score: %d/100 [%s]%n"),
                            avg, avg >= 75 ? brightGreen("EXCELLENT") : yellow("ACCEPTABLE"));
                } catch (Exception e) {
                    System.out.println(red("[Benchmark Error] " + e.getMessage()));
                }
                return true;
            }
        });

        // /run (Executes Phase 3 code in real-time)
        register(new ChatCommand() {
            @Override public String getKeyword() { return "run"; }
            @Override public String getDescription() { return "Compile and execute Phase 3 Java code of latest response."; }
            @Override public boolean execute(CommandContext ctx) {
                List<MentorResponse> history = ctx.getEngine().getSessionHistory();
                if (history.isEmpty()) {
                    System.out.println(yellow("No response generated yet to execute. Ask a Java question first."));
                    return true;
                }
                MentorResponse latest = history.get(history.size() - 1);
                String code = latest.getPhase3Code();
                System.out.println(bold(cyan("\nCompiling and executing Phase 3 Java code on-the-fly...")));
                CodeExecutionEngine execEngine = new CodeExecutionEngine();
                CodeExecutionEngine.ExecutionResult res = execEngine.compileAndExecute(code);
                System.out.println(res.toReport());
                return true;
            }
        });

        // /history
        register(new ChatCommand() {
            @Override public String getKeyword() { return "history"; }
            @Override public String getDescription() { return "View queries and audit scores from current session."; }
            @Override public boolean execute(CommandContext ctx) {
                List<MentorResponse> history = ctx.getEngine().getSessionHistory();
                if (history.isEmpty()) {
                    System.out.println(yellow("No queries processed in this session yet."));
                    return true;
                }
                System.out.println(bold("\nSession Query History (" + history.size() + " total):"));
                for (int i = 0; i < history.size(); i++) {
                    MentorResponse resp = history.get(i);
                    int score = resp.getAuditReport() != null ? resp.getAuditReport().getRubricScore() : 0;
                    System.out.printf("  %d. [%d/100] %s (%s, %dms)%n",
                            i + 1, score, resp.getQuery(), resp.getSourceEngine(), resp.getResponseTimeMs());
                }
                return true;
            }
        });

        // /export [filename]
        register(new ChatCommand() {
            @Override public String getKeyword() { return "export"; }
            @Override public String getDescription() { return "Export session transcript to Markdown: /export [file.md]"; }
            @Override public boolean execute(CommandContext ctx) {
                List<MentorResponse> history = ctx.getEngine().getSessionHistory();
                if (history.isEmpty()) {
                    System.out.println(yellow("Cannot export: No session history available."));
                    return true;
                }
                String filename = ctx.getArgument();
                if (filename.isBlank()) {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                    filename = "transcript-" + timestamp + ".md";
                }
                try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
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
                    System.out.println(brightGreen("Successfully exported session transcript to: " + filename));
                } catch (IOException e) {
                    System.out.println(red("Failed to export transcript: " + e.getMessage()));
                }
                return true;
            }
        });

        // /clear
        register(new ChatCommand() {
            @Override public String getKeyword() { return "clear"; }
            @Override public String getDescription() { return "Clear terminal console."; }
            @Override public boolean execute(CommandContext ctx) {
                ctx.clearScreen();
                return true;
            }
        });

        // /exit and /quit
        ChatCommand exitCmd = new ChatCommand() {
            @Override public String getKeyword() { return "exit"; }
            @Override public String getDescription() { return "Exit the mentor session."; }
            @Override public boolean execute(CommandContext ctx) {
                return false;
            }
        };
        register(exitCmd);
        register(new ChatCommand() {
            @Override public String getKeyword() { return "quit"; }
            @Override public String getDescription() { return "Exit the mentor session."; }
            @Override public boolean execute(CommandContext ctx) { return false; }
        });
    }
}

