package com.javamentor.engine;

import com.javamentor.model.MentorResponse;
import com.javamentor.model.TopicContent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Offline Pattern & Knowledge Engine (Track A).
 * Operates entirely offline and deterministically.
 * Features:
 * - Curated catalog matching
 * - Fuzzy string matching (Levenshtein distance)
 * - Fallback keywords and topic recommendation suggestions
 */
public class OfflineKnowledgeEngine {

    private final KnowledgeRepository repository;

    public OfflineKnowledgeEngine(KnowledgeRepository repository) {
        this.repository = repository;
    }

    public static class MatchResult {
        private final TopicContent topic;
        private final double score;

        public MatchResult(TopicContent topic, double score) {
            this.topic = topic;
            this.score = score;
        }

        public TopicContent getTopic() {
            return topic;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * Resolves the query against the offline catalog using keyword scoring and fuzzy matching.
     */
    public MentorResponse resolve(String query) {
        long start = System.currentTimeMillis();
        String normalized = query.toLowerCase(Locale.ROOT).trim();
        String[] tokens = normalized.split("[^a-zA-Z0-9_]+");

        List<MatchResult> matches = new ArrayList<>();

        for (TopicContent topic : repository.getTopics()) {
            double score = computeMatchScore(topic, normalized, tokens);
            matches.add(new MatchResult(topic, score));
        }

        matches.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());

        if (!matches.isEmpty() && matches.get(0).getScore() >= 1.5) {
            TopicContent best = matches.get(0).getTopic();
            long elapsed = System.currentTimeMillis() - start;
            return new MentorResponse(
                    query,
                    best.getPhase1Conceptual(),
                    best.getPhase2Algorithmic(),
                    best.getPhase3Code(),
                    "Knowledge Base Pattern Engine (Track A - " + best.getTitle() + ")",
                    elapsed
            );
        }

        // Low confidence: Generate a graceful response with recommendations
        List<String> recommendations = matches.stream()
                .limit(3)
                .map(m -> m.getTopic().getTitle() + " (" + m.getTopic().getCategory() + ")")
                .toList();

        long elapsed = System.currentTimeMillis() - start;
        return buildFallbackResponse(query, recommendations, elapsed);
    }

    private double computeMatchScore(TopicContent topic, String fullQuery, String[] tokens) {
        double score = 0.0;
        String title = topic.getTitle().toLowerCase(Locale.ROOT);
        String category = topic.getCategory().toLowerCase(Locale.ROOT);

        // Direct containment in query
        if (fullQuery.contains(title)) {
            score += 10.0;
        }
        if (fullQuery.contains(category)) {
            score += 3.0;
        }

        // Keywords matching
        for (String kw : topic.getKeywords()) {
            String lowerKw = kw.toLowerCase(Locale.ROOT);
            if (fullQuery.contains(lowerKw)) {
                score += 3.0;
            }
            for (String token : tokens) {
                if (token.equalsIgnoreCase(lowerKw)) {
                    score += 4.0;
                } else if (token.length() >= 4 && computeLevenshteinSimilarity(token, lowerKw) >= 0.75) {
                    score += 2.0; // Fuzzy string match
                }
            }
        }

        return score;
    }

    /**
     * Calculates fuzzy string similarity between 0.0 (no match) and 1.0 (exact match).
     */
    private double computeLevenshteinSimilarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private MentorResponse buildFallbackResponse(String query, List<String> recommendations, long elapsed) {
        String p1 = """
* Theoretical Grounding: The offline knowledge engine evaluated the query: "%s". In offline mode, queries are mapped against our curated 20+ core Java architecture topics.
* API & Syntax Mechanics: Java standard library packages: java.lang, java.util, java.util.concurrent, java.io, java.nio.
* Industrial Real-World Context: When querying specialized domains offline, consult related catalog topics or connect to Google Gemini (Track B) for open-domain inquiries.
""".formatted(query);

        StringBuilder p2 = new StringBuilder();
        p2.append("1. The exact query pattern was not recognized with high confidence in the offline store.\n");
        p2.append("2. Recommended matching topics you can explore:\n");
        for (String rec : recommendations) {
            p2.append("   * ").append(rec).append("\n");
        }
        p2.append("3. Type one of the suggested keywords (e.g., 'threads', 'streams', 'hashmap', 'cas', 'singleton').\n");
        p2.append("* Edge Cases: Offline catalog is deterministic. Switch to online mode via `/mode online` for open-domain Gemini inquiries.\n");
        p2.append("* Algorithmic Complexity: Lookup Complexity: O(T * K) where T=topics, K=keywords.");

        String p3 = """
public class MentorFallbackDemo {
    public static void main(String[] args) {
        // Test Scenario 1: Displaying Available System Capabilities
        System.out.println("=== Test Scenario 1: Offline Engine Status ===");
        System.out.println("Status: Offline Knowledge Engine is active.");
        System.out.println("Recommendation: Try queries like 'How do I run a thread with Runnable?' or 'HashMap collision'.");

        // Test Scenario 2: Verifying Environment Configuration
        System.out.println("\\n=== Test Scenario 2: Online Mode Check ===");
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            System.out.println("GEMINI_API_KEY detected! Switch to online mode using command: /mode online");
        } else {
            System.out.println("To enable full Google Gemini open-domain AI, set GEMINI_API_KEY environment variable.");
        }
    }
}
""";

        return new MentorResponse(query, p1, p2.toString(), p3, "Offline Engine (Fallback Suggestions)", elapsed);
    }
}

