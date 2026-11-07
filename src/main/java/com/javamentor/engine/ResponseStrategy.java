package com.javamentor.engine;

import com.javamentor.model.MentorResponse;

/**
 * Strategy interface (GOF Strategy Pattern) for generating responses.
 */
public interface ResponseStrategy {

    /**
     * Executes the strategy to resolve a Java technical inquiry.
     *
     * @param query The user's query.
     * @return The structured MentorResponse adhering to the 3-phase pipeline.
     * @throws Exception If generation or communication fails.
     */
    MentorResponse execute(String query) throws Exception;

    /**
     * @return Descriptive name of this strategy.
     */
    String getName();

    /**
     * @return True if strategy is available and operational.
     */
    boolean isAvailable();
}
