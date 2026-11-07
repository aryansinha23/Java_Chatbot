package com.javamentor.engine;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dynamic Java compilation and execution harness.
 * Verifies that Phase 3 source code compiles and executes its integrated test suite.
 */
public class CodeExecutionEngine {

    public static class ExecutionResult {
        private final boolean compiled;
        private final boolean executed;
        private final int exitCode;
        private final String compilerErrors;
        private final String consoleOutput;
        private final long durationMs;

        public ExecutionResult(boolean compiled, boolean executed, int exitCode,
                               String compilerErrors, String consoleOutput, long durationMs) {
            this.compiled = compiled;
            this.executed = executed;
            this.exitCode = exitCode;
            this.compilerErrors = compilerErrors;
            this.consoleOutput = consoleOutput;
            this.durationMs = durationMs;
        }

        public boolean isCompiled() {
            return compiled;
        }

        public boolean isExecuted() {
            return executed;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getCompilerErrors() {
            return compilerErrors;
        }

        public String getConsoleOutput() {
            return consoleOutput;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Java Execution Harness Report ===\n");
            sb.append(String.format("Compilation Status : %s\n", compiled ? "SUCCESS" : "FAILED"));
            if (!compiled) {
                sb.append("Compiler Output:\n").append(compilerErrors).append("\n");
                return sb.toString();
            }
            sb.append(String.format("Execution Status   : %s (Exit Code: %d, Time: %dms)\n",
                    executed ? "SUCCESS" : "FAILED", exitCode, durationMs));
            sb.append("Terminal Console Output:\n");
            sb.append(consoleOutput != null && !consoleOutput.isBlank() ? consoleOutput : "(No output emitted)\n");
            return sb.toString();
        }
    }

    /**
     * Extracts the class name from source code.
     */
    public String extractClassName(String sourceCode) {
        Pattern pattern = Pattern.compile("public\\s+(?:final\\s+)?class\\s+([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Fallback for non-public class
        Pattern fallback = Pattern.compile("class\\s+([A-Za-z0-9_]+)");
        Matcher fallbackMatcher = fallback.matcher(sourceCode);
        if (fallbackMatcher.find()) {
            return fallbackMatcher.group(1);
        }
        return "TempTestClass";
    }

    /**
     * Compiles and executes the provided Java source code.
     */
    public ExecutionResult compileAndExecute(String rawSourceCode) {
        long startTime = System.currentTimeMillis();

        // Strip markdown fences if present
        String cleanSource = stripMarkdown(rawSourceCode);
        String className = extractClassName(cleanSource);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("javamentor-run-");
            Path sourceFile = tempDir.resolve(className + ".java");
            Files.writeString(sourceFile, cleanSource, StandardCharsets.UTF_8);

            // Attempt compilation via JavaCompiler tool
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();

            boolean compiled;
            if (compiler != null) {
                int compileResult = compiler.run(null, null, new PrintStream(errStream),
                        sourceFile.toAbsolutePath().toString());
                compiled = (compileResult == 0);
            } else {
                // Fallback to running javac as a subprocess
                ProcessBuilder pbJavac = new ProcessBuilder("javac", sourceFile.getFileName().toString());
                pbJavac.directory(tempDir.toFile());
                pbJavac.redirectErrorStream(true);
                Process javacProc = pbJavac.start();
                String javacOutput = readStream(javacProc.getInputStream());
                compiled = javacProc.waitFor(10, TimeUnit.SECONDS) && javacProc.exitValue() == 0;
                if (!compiled) {
                    errStream.write(javacOutput.getBytes(StandardCharsets.UTF_8));
                }
            }

            if (!compiled) {
                long duration = System.currentTimeMillis() - startTime;
                return new ExecutionResult(false, false, -1, errStream.toString(StandardCharsets.UTF_8), "", duration);
            }

            // Execute the compiled class
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", tempDir.toAbsolutePath().toString(), className);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readStream(process.getInputStream());
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                output += "\n[Warning] Process timed out after 10 seconds.";
            }

            long duration = System.currentTimeMillis() - startTime;
            int exitVal = finished ? process.exitValue() : -1;
            return new ExecutionResult(true, finished && exitVal == 0, exitVal, "", output, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new ExecutionResult(false, false, -1, "Execution exception: " + e.getMessage(), "", duration);
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    private String stripMarkdown(String text) {
        if (text == null) return "";
        String cleaned = text.trim();
        if (cleaned.startsWith("```java")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String readStream(InputStream is) throws IOException {
        try (is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) deleteDirectory(f);
            }
        }
        dir.delete();
    }
}
