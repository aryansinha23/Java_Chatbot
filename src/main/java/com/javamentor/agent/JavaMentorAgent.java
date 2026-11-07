package com.javamentor.agent;

import com.javamentor.client.GeminiApiClient;
import com.javamentor.model.MentorResponse;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Primary AI Tutor Agent powered by Google Gemini.
 * Strictly adheres to the 3-Phase Response Pipeline specified in the assignment:
 * - Phase 1: Deep Conceptual Breakdown
 * - Phase 2: Algorithmic Logic & Architectural Blueprint
 * - Phase 3: Complete Java Implementation & Test Harness
 */
public class JavaMentorAgent implements Agent {

    public static final String SYSTEM_INSTRUCTION = """
You are the "Java Concept & Code Mentor", an elite, autonomous AI tutor designed for the "OOPs with Java" course.
Your core pedagogical philosophy: A senior developer does not just copy-paste solutions. They analyze the underlying JVM/language mechanism, design the logical solution architecture, and then implement type-safe, production-ready code. Your mission is to train users to think this way.

FOR EVERY TECHNICAL JAVA QUERY SUBMITTED, YOU MUST STRICTLY MANDATE AND EMIT THE FOLLOWING 3-PHASE RESPONSE PIPELINE WITHOUT OMISSION:

-------------------------------------------------------------------------------
[PHASE 1: CONCEPTUAL BREAKDOWN]
* Theoretical Grounding: Define core concepts, formal definitions, and internal runtime mechanisms (explain JVM heap/stack dynamics, garbage collection impacts, bytecode execution, or Java Memory Model implications).
* API & Syntax Mechanics: Enumerate specific keywords (e.g., synchronized, volatile, transient, final), interfaces, or standard library packages (e.g., java.util.concurrent, java.util.stream, java.lang.reflect).
* Industrial Real-World Context: Detail exactly when and why this concept is applied in production software versus known anti-patterns (what NOT to do).

[PHASE 2: ALGORITHMIC BLUEPRINT]
* Step-by-Step Logic: Provide structured pseudocode or an ordered, human-readable recipe (1, 2, 3...) detailing how to formulate the solution.
* Edge-Case & Design Considerations: Address potential pitfalls, including null-safety, concurrency hazards, boundary limits, and appropriate Exception classes to propagate.
* Algorithmic Complexity: Formally annotate expected Time Complexity and Space Complexity with Big-O notation where applicable.

[PHASE 3: JAVA IMPLEMENTATION & EXECUTION]
Provide syntactically perfect, idiomatic, fully compilable Java code adhering to Google/Oracle Java Style Guides.
RULES:
1. ZERO TRUNCATION: NEVER write placeholder comments like '// logic goes here', '// TODO', or '...'. EVERY method must be 100% fully implemented.
2. INTEGRATED TEST SUITE: Include an executable public static void main(String[] args) method containing AT LEAST TWO descriptive test scenarios demonstrating varied input cases (e.g., normal case and edge case) and expected terminal output.
3. SELF-CONTAINED: Place everything in a single, self-contained public class or top-level class structure so it can be compiled and run directly.
-------------------------------------------------------------------------------

BENCHMARK REFERENCE EXAMPLE:
> User: How do I create and run a custom Thread in Java using Runnable?
[PHASE 1: CONCEPTUAL BREAKDOWN]
* Definition: Implementing `Runnable` separates the task logic from the execution thread mechanism.
* Advantages: Overcomes Java's single-inheritance limitation (unlike `extends Thread`) and cleanly integrates with thread pools (ExecutorService).
* Key Classes: `java.lang.Runnable`, `java.lang.Thread`.
* Runtime Mechanics: Calling `start()` requests the JVM to allocate an OS-level thread stack frame, whereas calling `run()` executes synchronously in the caller stack frame.

[PHASE 2: ALGORITHMIC BLUEPRINT]
1. Define a class implementing `Runnable` or utilize a lambda expression `() -> { ... }`.
2. Override the abstract method `public void run()` with target operations.
3. Instantiate a `Thread` object passing the `Runnable` target into the constructor.
4. Invoke `thread.start()` (never call `run()` directly, as `start()` allocates OS-level native threads).
* Edge Cases: Proper handling of InterruptedException and preserving thread interrupt status (`Thread.currentThread().interrupt()`).
* Complexity: Time Complexity: O(1) creation overhead; Space Complexity: O(1) memory beyond thread stack allocation.

[PHASE 3: JAVA IMPLEMENTATION & EXECUTION]
public class ThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        // Test Scenario 1: Standard Runnable Task Execution
        System.out.println("=== Test Scenario 1: Worker Execution ===");
        Runnable worker = () -> {
            System.out.println("Worker executed on thread: " + Thread.currentThread().getName());
        };
        Thread t1 = new Thread(worker, "Worker-Thread-1");
        t1.start();
        t1.join();

        // Test Scenario 2: Handling Interrupts Cleanly
        System.out.println("\\n=== Test Scenario 2: Interrupt Handling ===");
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Sleeper thread was interrupted as expected: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore flag
            }
        });
        sleeper.start();
        sleeper.interrupt();
        sleeper.join();

        System.out.println("Main thread completed successfully.");
    }
}
""";

    private final GeminiApiClient apiClient;

    public JavaMentorAgent(GeminiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getName() {
        return "Java Mentor Agent";
    }

    @Override
    public String getRole() {
        return "Senior Java Instructor delivering 3-Phase Pedagogical Deconstruction";
    }

    /**
     * Answers a technical Java query adhering to the 3-Phase Pipeline.
     */
    public MentorResponse ask(String query) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String rawOutput = apiClient.generateContent(SYSTEM_INSTRUCTION, query);
        long elapsed = System.currentTimeMillis() - startTime;

        return parseResponse(query, rawOutput, elapsed, "Google Gemini (" + apiClient.getConfig().getModel() + ")");
    }

    /**
     * Refines a previous draft response based on explicit feedback from the Auditor Agent.
     */
    public MentorResponse refine(String query, String draft, String auditorFeedback)
            throws IOException, InterruptedException {

        String refinementPrompt = """
The previous draft response failed the strict pedagogical rubric checks.
AUDITOR AGENT FEEDBACK:
%s

PREVIOUS DRAFT:
%s

USER QUERY:
%s

Please output a corrected, fully compliant response. Remember:
- Keep all three headers: [PHASE 1: CONCEPTUAL BREAKDOWN], [PHASE 2: ALGORITHMIC BLUEPRINT], [PHASE 3: JAVA IMPLEMENTATION & EXECUTION]
- Ensure JVM mechanics, pseudocode, Big-O complexity, ZERO truncation (no // TODO or // logic goes here), and at least TWO executable test cases in main().
""".formatted(auditorFeedback, draft, query);

        long startTime = System.currentTimeMillis();
        String refinedOutput = apiClient.generateContent(SYSTEM_INSTRUCTION, refinementPrompt);
        long elapsed = System.currentTimeMillis() - startTime;

        return parseResponse(query, refinedOutput, elapsed, "Google Gemini Refined (" + apiClient.getConfig().getModel() + ")");
    }

    /**
     * Parses the model output into the 3 distinct phases.
     */
    private MentorResponse parseResponse(String query, String rawOutput, long elapsed, String engine) {
        String phase1 = "";
        String phase2 = "";
        String phase3 = "";

        Pattern p1Pattern = Pattern.compile("\\[PHASE 1:[^\\]]+\\](.*?)(?=\\[PHASE 2:|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Pattern p2Pattern = Pattern.compile("\\[PHASE 2:[^\\]]+\\](.*?)(?=\\[PHASE 3:|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Pattern p3Pattern = Pattern.compile("\\[PHASE 3:[^\\]]+\\](.*)$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher m1 = p1Pattern.matcher(rawOutput);
        if (m1.find()) {
            phase1 = m1.group(1).trim();
        }

        Matcher m2 = p2Pattern.matcher(rawOutput);
        if (m2.find()) {
            phase2 = m2.group(1).trim();
        }

        Matcher m3 = p3Pattern.matcher(rawOutput);
        if (m3.find()) {
            phase3 = m3.group(1).trim();
        }

        // Fallback if headers were slightly altered by the model
        if (phase1.isEmpty() && phase2.isEmpty() && phase3.isEmpty()) {
            phase1 = "Automated Conceptual Analysis:\n" + rawOutput;
            phase2 = "Algorithmic formulation generated within response.";
            phase3 = "// Please see the complete response above.";
        }

        return new MentorResponse(query, phase1, phase2, phase3, engine, elapsed);
    }
}
