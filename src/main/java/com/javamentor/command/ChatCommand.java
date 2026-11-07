package com.javamentor.command;

/**
 * Command Interface implementing the GOF Command Pattern for CLI commands.
 */
public interface ChatCommand {

    /**
     * @return The primary command identifier (e.g. "help", "mode", "run").
     */
    String getKeyword();

    /**
     * @return A brief description for help displays.
     */
    String getDescription();

    /**
     * Executes the command.
     *
     * @param context Execution context containing engine, UI, and arguments.
     * @return True to keep the session running, false to terminate.
     */
    boolean execute(CommandContext context);
}

