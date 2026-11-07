package com.javamentor.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Autonomous Synthetic Student Agent.
 * Generates structured Java inquiries across beginner, intermediate, and advanced levels
 * to test, train, and benchmark the Mentor Agent and Auditor Agent.
 */
public class SyntheticStudentAgent implements Agent {

    public enum DifficultyTier {
        NOVICE,
        INTERMEDIATE,
        SENIOR_ARCHITECT
    }

    public static class SyntheticQuery {
        private final String query;
        private final String topic;
        private final DifficultyTier tier;

        public SyntheticQuery(String query, String topic, DifficultyTier tier) {
            this.query = query;
            this.topic = topic;
            this.tier = tier;
        }

        public String getQuery() {
            return query;
        }

        public String getTopic() {
            return topic;
        }

        public DifficultyTier getTier() {
            return tier;
        }
    }

    private final List<SyntheticQuery> testCurriculum;
    private final Random random;

    public SyntheticStudentAgent() {
        this.random = new Random();
        this.testCurriculum = Arrays.asList(
                // Novice
                new SyntheticQuery(
                        "How do I create and run a custom Thread in Java using Runnable?",
                        "Multithreading",
                        DifficultyTier.NOVICE
                ),
                new SyntheticQuery(
                        "How do I use Polymorphism and Interface abstraction to implement a PaymentProcessor?",
                        "OOP Principles",
                        DifficultyTier.NOVICE
                ),
                new SyntheticQuery(
                        "How do I safely catch and handle custom Checked vs Unchecked Exceptions in Java?",
                        "Exception Handling",
                        DifficultyTier.NOVICE
                ),
                new SyntheticQuery(
                        "How do I sort a List of custom Employee objects using Comparable and Comparator in Java?",
                        "Collections & Sorting",
                        DifficultyTier.NOVICE
                ),

                // Intermediate
                new SyntheticQuery(
                        "How do I use Java 8 Stream API with filter, map, and Collectors.groupingBy?",
                        "Stream API & Lambdas",
                        DifficultyTier.INTERMEDIATE
                ),
                new SyntheticQuery(
                        "How does HashMap internally handle collisions and rehashing in Java 8+?",
                        "Collections Internals",
                        DifficultyTier.INTERMEDIATE
                ),
                new SyntheticQuery(
                        "How do I implement a Thread-Safe Producer-Consumer pattern using BlockingQueue?",
                        "java.util.concurrent",
                        DifficultyTier.INTERMEDIATE
                ),
                new SyntheticQuery(
                        "How do I use Java Generics with bounded wildcards (? extends T and ? super T / PECS)?",
                        "Generics",
                        DifficultyTier.INTERMEDIATE
                ),

                // Senior Architect
                new SyntheticQuery(
                        "How do I implement a Lock-Free Counter in Java using AtomicLong and CAS mechanics?",
                        "Java Memory Model & Atomics",
                        DifficultyTier.SENIOR_ARCHITECT
                ),
                new SyntheticQuery(
                        "How does the Double-Checked Locking Singleton pattern work with the volatile keyword and happens-before guarantee?",
                        "Concurrency & JMM",
                        DifficultyTier.SENIOR_ARCHITECT
                ),
                new SyntheticQuery(
                        "How do I implement a custom thread-safe Object Pool pattern in Java with boundary limits and timeouts?",
                        "Design Patterns & Concurrency",
                        DifficultyTier.SENIOR_ARCHITECT
                ),
                new SyntheticQuery(
                        "How do I asynchronously coordinate multiple CompletableFuture tasks with CompletableFuture.allOf and exceptional fallbacks?",
                        "Asynchronous Programming",
                        DifficultyTier.SENIOR_ARCHITECT
                )
        );
    }

    @Override
    public String getName() {
        return "Synthetic Student Agent";
    }

    @Override
    public String getRole() {
        return "Automated Inquirer & Benchmark Simulator for Training Verification";
    }

    public List<SyntheticQuery> getAllCurriculumQueries() {
        return Collections.unmodifiableList(testCurriculum);
    }

    public SyntheticQuery getRandomQuery() {
        return testCurriculum.get(random.nextInt(testCurriculum.size()));
    }

    public List<SyntheticQuery> getQueriesForTier(DifficultyTier tier) {
        return testCurriculum.stream()
                .filter(q -> q.getTier() == tier)
                .toList();
    }
}

