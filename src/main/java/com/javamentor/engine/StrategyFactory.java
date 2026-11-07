package com.javamentor.engine;

import com.javamentor.agent.AuditorAgent;
import com.javamentor.agent.JavaMentorAgent;
import com.javamentor.client.GeminiApiClient;
import com.javamentor.client.GeminiConfig;
import com.javamentor.engine.strategy.GeminiStrategy;
import com.javamentor.engine.strategy.OfflineStrategy;

/**
 * Factory class (GOF Factory Pattern) producing the appropriate ResponseStrategy.
 */
public class StrategyFactory {

    public enum EngineMode {
        AUTO,
        ONLINE,
        OFFLINE
    }

    private final GeminiConfig geminiConfig;
    private final GeminiApiClient geminiClient;
    private final JavaMentorAgent mentorAgent;
    private final AuditorAgent auditorAgent;
    private final OfflineKnowledgeEngine offlineEngine;

    public StrategyFactory() {
        this.geminiConfig = new GeminiConfig();
        this.geminiClient = new GeminiApiClient(geminiConfig);
        this.mentorAgent = new JavaMentorAgent(geminiClient);
        this.auditorAgent = new AuditorAgent();

        KnowledgeRepository repo = new KnowledgeRepository();
        this.offlineEngine = new OfflineKnowledgeEngine(repo);
    }

    public ResponseStrategy createStrategy(EngineMode mode) {
        switch (mode) {
            case ONLINE:
                if (!geminiConfig.isConfigured()) {
                    throw new IllegalStateException("Cannot use ONLINE mode: GEMINI_API_KEY environment variable is not set.");
                }
                return new GeminiStrategy(mentorAgent, auditorAgent, true);

            case OFFLINE:
                return new OfflineStrategy(offlineEngine, auditorAgent);

            case AUTO:
            default:
                if (geminiConfig.isConfigured()) {
                    return new GeminiStrategy(mentorAgent, auditorAgent, true);
                } else {
                    return new OfflineStrategy(offlineEngine, auditorAgent);
                }
        }
    }

    public GeminiConfig getGeminiConfig() {
        return geminiConfig;
    }

    public AuditorAgent getAuditorAgent() {
        return auditorAgent;
    }

    public JavaMentorAgent getMentorAgent() {
        return mentorAgent;
    }

    public OfflineKnowledgeEngine getOfflineEngine() {
        return offlineEngine;
    }
}
