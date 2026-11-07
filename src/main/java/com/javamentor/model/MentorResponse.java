package com.javamentor.model;

/**
 * Encapsulates the complete response produced by the Java Tutor Chatbot,
 * adhering to the mandatory 3-Phase Response Pipeline.
 */
public class MentorResponse {

    private final String query;
    private final String phase1Conceptual;
    private final String phase2Algorithmic;
    private final String phase3Code;
    private final String sourceEngine; // e.g. "Google Gemini (Track B)" or "Knowledge Base (Track A)"
    private final long responseTimeMs;
    private AuditReport auditReport;

    public MentorResponse(String query, String phase1Conceptual, String phase2Algorithmic,
                          String phase3Code, String sourceEngine, long responseTimeMs) {
        this.query = query;
        this.phase1Conceptual = phase1Conceptual;
        this.phase2Algorithmic = phase2Algorithmic;
        this.phase3Code = phase3Code;
        this.sourceEngine = sourceEngine;
        this.responseTimeMs = responseTimeMs;
    }

    public String getQuery() {
        return query;
    }

    public String getPhase1Conceptual() {
        return phase1Conceptual;
    }

    public String getPhase2Algorithmic() {
        return phase2Algorithmic;
    }

    public String getPhase3Code() {
        return phase3Code;
    }

    public String getSourceEngine() {
        return sourceEngine;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public AuditReport getAuditReport() {
        return auditReport;
    }

    public void setAuditReport(AuditReport auditReport) {
        this.auditReport = auditReport;
    }

    /**
     * Formats the output matching the canonical benchmark format specified in the rubric.
     */
    public String toCanonicalString() {
        StringBuilder sb = new StringBuilder();
        sb.append("> User: ").append(query).append("\n\n");
        sb.append("[PHASE 1: CONCEPTUAL BREAKDOWN]\n");
        sb.append(phase1Conceptual.trim()).append("\n\n");
        sb.append("[PHASE 2: ALGORITHMIC BLUEPRINT]\n");
        sb.append(phase2Algorithmic.trim()).append("\n\n");
        sb.append("[PHASE 3: JAVA IMPLEMENTATION & EXECUTION]\n");
        sb.append(phase3Code.trim()).append("\n");
        return sb.toString();
    }
}
