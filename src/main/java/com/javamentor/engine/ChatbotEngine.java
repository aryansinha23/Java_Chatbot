package com.javamentor.engine;

import com.javamentor.agent.AuditorAgent;
import com.javamentor.agent.SyntheticStudentAgent;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main Chatbot Orchestration Engine.
 * Coordinates user requests, strategy selection, multi-agent training simulations,
 * and conversation session history.
 */
public class ChatbotEngine {

    private final StrategyFactory factory;
    private final SyntheticStudentAgent studentAgent;
    private StrategyFactory.EngineMode currentMode;
    private boolean auditDisplayEnabled;
    private final List<MentorResponse> sessionHistory;

    public ChatbotEngine() {
        this.factory = new StrategyFactory();
        this.studentAgent = new SyntheticStudentAgent();
        this.currentMode = StrategyFactory.EngineMode.AUTO;
        this.auditDisplayEnabled = true;
        this.sessionHistory = new ArrayList<>();
    }

    public StrategyFactory.EngineMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(StrategyFactory.EngineMode mode) {
        this.currentMode = mode;
    }

    public boolean isAuditDisplayEnabled() {
        return auditDisplayEnabled;
    }

    public void setAuditDisplayEnabled(boolean enabled) {
        this.auditDisplayEnabled = enabled;
    }

    public boolean isOnlineAvailable() {
        return factory.getGeminiConfig().isConfigured();
    }

    public String getActiveEngineDescription() {
        ResponseStrategy strategy = factory.createStrategy(currentMode);
        return strategy.getName();
    }

    public List<MentorResponse> getSessionHistory() {
        return Collections.unmodifiableList(sessionHistory);
    }

    /**
     * Evaluates a user query through the active strategy.
     */
    public MentorResponse processQuery(String query) throws Exception {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty.");
        }

        ResponseStrategy strategy = factory.createStrategy(currentMode);
        MentorResponse response = strategy.execute(query);
        sessionHistory.add(response);
        return response;
    }

    /**
     * Executes an autonomous multi-agent training & evaluation benchmark.
     * The SyntheticStudentAgent generates questions, the Mentor Agent answers,
     * and the AuditorAgent grades each against the 100-mark rubric.
     */
    public List<MentorResponse> runBenchmark(int questionCount) throws Exception {
        List<SyntheticStudentAgent.SyntheticQuery> queries = studentAgent.getAllCurriculumQueries();
        int count = Math.min(questionCount, queries.size());
        List<MentorResponse> benchmarkResults = new ArrayList<>();

        System.out.printf("[Benchmark] Starting autonomous simulation with %d queries...%n", count);

        for (int i = 0; i < count; i++) {
            SyntheticStudentAgent.SyntheticQuery q = queries.get(i);
            System.out.printf("[Benchmark %d/%d] [%s] %s%n", i + 1, count, q.getTier(), q.getQuery());
            MentorResponse response = processQuery(q.getQuery());
            benchmarkResults.add(response);
        }

        return benchmarkResults;
    }

    public StrategyFactory getFactory() {
        return factory;
    }

    public SyntheticStudentAgent getStudentAgent() {
        return studentAgent;
    }
}
