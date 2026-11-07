package com.javamentor;

import com.javamentor.agent.AuditorAgent;
import com.javamentor.agent.SyntheticStudentAgent;
import com.javamentor.engine.ChatbotEngine;
import com.javamentor.engine.KnowledgeRepository;
import com.javamentor.engine.OfflineKnowledgeEngine;
import com.javamentor.engine.StrategyFactory;
import com.javamentor.model.AuditReport;
import com.javamentor.model.MentorResponse;
import com.javamentor.model.TopicContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit & Integration Tests verifying compliance with the 3-Phase Response Pipeline
 * and the 100-mark evaluation rubric.
 */
public class ResponsePipelineTest {

    private KnowledgeRepository repository;
    private OfflineKnowledgeEngine offlineEngine;
    private AuditorAgent auditorAgent;
    private ChatbotEngine chatbotEngine;

    @BeforeEach
    void setUp() {
        this.repository = new KnowledgeRepository();
        this.offlineEngine = new OfflineKnowledgeEngine(repository);
        this.auditorAgent = new AuditorAgent();
        this.chatbotEngine = new ChatbotEngine();
        this.chatbotEngine.setCurrentMode(StrategyFactory.EngineMode.OFFLINE);
    }

    @Test
    @DisplayName("Verify Offline Catalog loads at least 20 core Java topics")
    void testCatalogTopicCount() {
        List<TopicContent> topics = repository.getTopics();
        assertNotNull(topics, "Catalog topics list must not be null");
        assertTrue(topics.size() >= 20, "Catalog must contain at least 20 curated topics. Found: " + topics.size());
    }

    @Test
    @DisplayName("Verify Canonical Benchmark Query matches Runnable Thread topic")
    void testCanonicalBenchmarkQuery() {
        String query = "How do I create and run a custom Thread in Java using Runnable?";
        MentorResponse response = offlineEngine.resolve(query);

        assertNotNull(response);
        assertEquals(query, response.getQuery());

        // Check Phase 1
        String p1 = response.getPhase1Conceptual();
        assertTrue(p1.contains("Runnable"), "Phase 1 must mention Runnable");
        assertTrue(p1.toLowerCase().contains("jvm") || p1.toLowerCase().contains("stack"),
                "Phase 1 must cover JVM runtime dynamics");

        // Check Phase 2
        String p2 = response.getPhase2Algorithmic();
        assertTrue(p2.contains("1.") || p2.toLowerCase().contains("step"), "Phase 2 must contain structured steps");
        assertTrue(p2.contains("Complexity") || p2.contains("O("), "Phase 2 must include Complexity");

        // Check Phase 3
        String p3 = response.getPhase3Code();
        assertTrue(p3.contains("class "), "Phase 3 must contain a compilable class");
        assertTrue(p3.contains("public static void main"), "Phase 3 must contain main method");
        assertTrue(p3.contains("Test Scenario 1") && p3.contains("Test Scenario 2"),
                "Phase 3 must demonstrate at least two test scenarios");

        // Verify Zero Truncation
        assertFalse(p3.contains("// logic goes here"), "Phase 3 must have zero truncation");
        assertFalse(p3.contains("// TODO"), "Phase 3 must have zero truncation");
    }

    @Test
    @DisplayName("Verify AuditorAgent flags truncation violations")
    void testAuditorRejectsTruncatedCode() {
        String query = "Dummy query";
        String p1 = "Theoretical Grounding: JVM heap allocation and garbage collection dynamics.";
        String p2 = "1. Step one.\n* Algorithmic Complexity: O(1) Time.";
        String p3WithTodo = """
public class DummyDemo {
    public static void main(String[] args) {
        // TODO: add code here
        // logic goes here
    }
}
""";
        MentorResponse truncatedResponse = new MentorResponse(query, p1, p2, p3WithTodo, "TestEngine", 10);
        AuditReport report = auditorAgent.audit(truncatedResponse);

        assertNotNull(report);
        assertFalse(report.isZeroTruncation(), "Auditor must detect truncation placeholders");
        assertFalse(report.isPassed(), "Auditor must reject response containing placeholders");
        assertTrue(report.getFeedbackNotes().stream().anyMatch(note -> note.contains("Zero Truncation Violation")),
                "Auditor must log specific truncation feedback note");
    }

    @Test
    @DisplayName("Verify SyntheticStudentAgent provides queries across all tiers")
    void testSyntheticStudentAgentTiers() {
        SyntheticStudentAgent student = new SyntheticStudentAgent();
        List<SyntheticStudentAgent.SyntheticQuery> allQueries = student.getAllCurriculumQueries();

        assertTrue(allQueries.size() >= 10, "Student agent curriculum should have at least 10 queries");

        List<SyntheticStudentAgent.SyntheticQuery> novice = student.getQueriesForTier(SyntheticStudentAgent.DifficultyTier.NOVICE);
        List<SyntheticStudentAgent.SyntheticQuery> intermediate = student.getQueriesForTier(SyntheticStudentAgent.DifficultyTier.INTERMEDIATE);
        List<SyntheticStudentAgent.SyntheticQuery> architect = student.getQueriesForTier(SyntheticStudentAgent.DifficultyTier.SENIOR_ARCHITECT);

        assertFalse(novice.isEmpty(), "Novice tier must have queries");
        assertFalse(intermediate.isEmpty(), "Intermediate tier must have queries");
        assertFalse(architect.isEmpty(), "Architect tier must have queries");
    }

    @Test
    @DisplayName("Verify Fuzzy String Matching on slightly misspelled query")
    void testFuzzyMatchingOnMisspelledQuery() {
        // "threds" instead of "threads"
        String misspelled = "threds and runable in java";
        MentorResponse response = offlineEngine.resolve(misspelled);

        assertNotNull(response);
        assertTrue(response.getPhase1Conceptual().toLowerCase().contains("runnable")
                || response.getPhase3Code().toLowerCase().contains("thread"),
                "Fuzzy matching should resolve to Threads & Runnable topic");
    }
}

