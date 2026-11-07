package com.javamentor;

import com.javamentor.command.CommandContext;
import com.javamentor.command.CommandInvoker;
import com.javamentor.engine.ChatbotEngine;
import com.javamentor.engine.CodeExecutionEngine;
import com.javamentor.engine.StrategyFactory;
import com.javamentor.model.MentorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying the GOF Command Pattern implementation and Code Execution Engine.
 */
public class CommandAndExecutionTest {

    @Test
    @DisplayName("Verify CommandInvoker registers default commands")
    void testCommandRegistration() {
        CommandInvoker invoker = new CommandInvoker();
        var registry = invoker.getRegistry();

        assertTrue(registry.containsKey("help"), "Must register help command");
        assertTrue(registry.containsKey("mode"), "Must register mode command");
        assertTrue(registry.containsKey("benchmark"), "Must register benchmark command");
        assertTrue(registry.containsKey("audit"), "Must register audit command");
        assertTrue(registry.containsKey("run"), "Must register run command");
        assertTrue(registry.containsKey("history"), "Must register history command");
        assertTrue(registry.containsKey("export"), "Must register export command");
        assertTrue(registry.containsKey("clear"), "Must register clear command");
        assertTrue(registry.containsKey("exit"), "Must register exit command");
    }

    @Test
    @DisplayName("Verify CommandInvoker execution dispatch")
    void testCommandDispatch() {
        ChatbotEngine engine = new ChatbotEngine();
        CommandInvoker invoker = new CommandInvoker();

        // Test mode command dispatch
        CommandContext ctx = new CommandContext(engine, "offline", null);
        boolean continueSession = invoker.dispatch("/mode offline", ctx);

        assertTrue(continueSession, "Mode command should keep session active");
        assertEquals(StrategyFactory.EngineMode.OFFLINE, engine.getCurrentMode());

        // Test exit command dispatch
        CommandContext exitCtx = new CommandContext(engine, "", null);
        boolean exitSession = invoker.dispatch("/exit", exitCtx);
        assertFalse(exitSession, "Exit command should signal termination");
    }

    @Test
    @DisplayName("Verify CodeExecutionEngine compiles and executes valid Java code")
    void testCodeExecutionEngineSuccess() {
        CodeExecutionEngine exec = new CodeExecutionEngine();
        String sampleCode = """
public class ExecutionTestDemo {
    public static void main(String[] args) {
        System.out.println("Execution engine verification test passed.");
    }
}
""";
        CodeExecutionEngine.ExecutionResult result = exec.compileAndExecute(sampleCode);

        assertTrue(result.isCompiled(), "Code must compile cleanly");
        assertTrue(result.isExecuted(), "Code must execute successfully");
        assertEquals(0, result.getExitCode());
        assertTrue(result.getConsoleOutput().contains("Execution engine verification test passed."));
    }

    @Test
    @DisplayName("Verify CodeExecutionEngine catches compilation errors")
    void testCodeExecutionEngineCompilationFailure() {
        CodeExecutionEngine exec = new CodeExecutionEngine();
        String invalidCode = """
public class InvalidSyntaxClass {
    public static void main(String[] args) {
        This line is invalid Java syntax and cannot compile!
    }
}
""";
        CodeExecutionEngine.ExecutionResult result = exec.compileAndExecute(invalidCode);

        assertFalse(result.isCompiled(), "Invalid code must fail compilation");
        assertFalse(result.isExecuted());
        assertFalse(result.getCompilerErrors().isBlank(), "Compiler errors must be reported");
    }
}
