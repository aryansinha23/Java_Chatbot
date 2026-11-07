package com.javamentor.command;

import com.javamentor.engine.ChatbotEngine;

/**
 * Context passed to command execution encapsulating state and arguments.
 */
public class CommandContext {

    private final ChatbotEngine engine;
    private final String argument;
    private final Runnable clearScreenAction;

    public CommandContext(ChatbotEngine engine, String argument, Runnable clearScreenAction) {
        this.engine = engine;
        this.argument = argument != null ? argument.trim() : "";
        this.clearScreenAction = clearScreenAction;
    }

    public ChatbotEngine getEngine() {
        return engine;
    }

    public String getArgument() {
        return argument;
    }

    public void clearScreen() {
        if (clearScreenAction != null) {
            clearScreenAction.run();
        }
    }
}

