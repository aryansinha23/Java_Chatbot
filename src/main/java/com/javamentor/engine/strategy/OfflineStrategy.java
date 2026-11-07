package com.javamentor.engine.strategy;

import com.javamentor.agent.AuditorAgent;
import com.javamentor.engine.OfflineKnowledgeEngine;
import com.javamentor.engine.ResponseStrategy;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;

/**
 * Strategy implementation using the offline Knowledge Base Pattern Engine (Track A).
 */
public class OfflineStrategy implements ResponseStrategy {

    private final OfflineKnowledgeEngine offlineEngine;
    private final AuditorAgent auditorAgent;

    public OfflineStrategy(OfflineKnowledgeEngine offlineEngine, AuditorAgent auditorAgent) {
        this.offlineEngine = offlineEngine;
        this.auditorAgent = auditorAgent;
    }

    @Override
    public MentorResponse execute(String query) {
        MentorResponse response = offlineEngine.resolve(query);
        AuditReport report = auditorAgent.audit(response);
        response.setAuditReport(report);
        return response;
    }

    @Override
    public String getName() {
        return "Offline Knowledge Base Strategy (Track A)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

