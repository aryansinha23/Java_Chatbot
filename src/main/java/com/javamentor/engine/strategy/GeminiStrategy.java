package com.javamentor.engine.strategy;

import com.javamentor.agent.AuditorAgent;
import com.javamentor.agent.JavaMentorAgent;
import com.javamentor.engine.ResponseStrategy;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;

/**
 * Strategy implementation using Google Gemini Cloud LLM (Track B).
 * Orchestrates a multi-agent feedback loop between JavaMentorAgent and AuditorAgent.
 */
public class GeminiStrategy implements ResponseStrategy {

    private final JavaMentorAgent mentorAgent;
    private final AuditorAgent auditorAgent;
    private final boolean autoRefine;

    public GeminiStrategy(JavaMentorAgent mentorAgent, AuditorAgent auditorAgent) {
        this(mentorAgent, auditorAgent, true);
    }

    public GeminiStrategy(JavaMentorAgent mentorAgent, AuditorAgent auditorAgent, boolean autoRefine) {
        this.mentorAgent = mentorAgent;
        this.auditorAgent = auditorAgent;
        this.autoRefine = autoRefine;
    }

    @Override
    public MentorResponse execute(String query) throws Exception {
        // Step 1: Mentor Agent generates draft response adhering to 3-phase prompt
        MentorResponse draft = mentorAgent.ask(query);

        // Step 2: Auditor Agent inspects draft against 100-mark rubric
        AuditReport report = auditorAgent.audit(draft);
        draft.setAuditReport(report);

        // Step 3: Multi-Agent Training / Self-Correction Loop
        if (!report.isPassed() && autoRefine) {
            System.out.println("[Auditor Agent Notice] Score: " + report.getRubricScore()
                    + "/100. Triggering multi-agent refinement loop...");
            try {
                MentorResponse refined = mentorAgent.refine(query, draft.toCanonicalString(), report.toSummary());
                AuditReport refinedReport = auditorAgent.audit(refined);
                refined.setAuditReport(refinedReport);
                return refined;
            } catch (Exception e) {
                System.err.println("[Warning] Refinement loop encountered an issue, returning audited draft: " + e.getMessage());
            }
        }

        return draft;
    }

    @Override
    public String getName() {
        return "Google Gemini API Strategy (Track B + Multi-Agent Auditor)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

