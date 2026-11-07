package com.javamentor.agent;

/**
 * Common contract for autonomous agents within the Java Tutor multi-agent architecture.
 */
public interface Agent {

    /**
     * @return The descriptive name of the agent.
     */
    String getName();

    /**
     * @return The functional role of the agent.
     */
    String getRole();
}
