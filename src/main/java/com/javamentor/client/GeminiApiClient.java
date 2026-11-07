package com.javamentor.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

/**
 * Cloud LLM REST Client for Google Gemini API implemented using java.net.http.HttpClient.
 * Features:
 * - Exponential backoff with jitter on HTTP 429 / 503
 * - Strict HTTP status code recovery
 * - Defensive JSON parsing guards
 * - Zero external heavy dependencies (uses Gson for JSON)
 */
public class GeminiApiClient {

    private final GeminiConfig config;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Random random;

    public GeminiApiClient(GeminiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new Gson();
        this.random = new Random();
    }

    public GeminiConfig getConfig() {
        return config;
    }

    /**
     * Sends a generation request with system instruction and user prompt to Gemini API.
     * Retries automatically on transient errors (429, 503, 500) using exponential backoff.
     *
     * @param systemInstruction The rigorous prompt governing tutor persona and 3-phase output.
     * @param userPrompt        The technical Java query from the user.
     * @return Raw text returned by the model.
     * @throws IOException          If communication fails after retries.
     * @throws InterruptedException If thread is interrupted during backoff.
     */
    public String generateContent(String systemInstruction, String userPrompt)
            throws IOException, InterruptedException {

        if (!config.isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured. "
                    + "Set the GEMINI_API_KEY environment variable before running in online mode.");
        }

        String requestJson = buildRequestBody(systemInstruction, userPrompt);
        String endpoint = config.getEndpointUrl();

        int attempt = 0;
        int maxRetries = config.getMaxRetries();
        long delayMs = config.getBackoffBaseDelayMs();

        while (true) {
            attempt++;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(config.getRequestTimeout())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return parseSuccessResponse(response.body());
                }

                // Handle rate limits or temporary service unavailability
                if ((statusCode == 429 || statusCode == 503 || statusCode == 500) && attempt <= maxRetries) {
                    long jitter = random.nextInt(500);
                    long sleepTime = delayMs + jitter;
                    System.err.printf("[Warning] Gemini API HTTP %d. Retrying attempt %d/%d after %d ms...%n",
                            statusCode, attempt, maxRetries, sleepTime);
                    Thread.sleep(sleepTime);
                    delayMs *= 2; // exponential backoff
                    continue;
                }

                // Non-retriable or retries exhausted
                handleHttpError(statusCode, response.body());

            } catch (IOException e) {
                if (attempt <= maxRetries) {
                    long jitter = random.nextInt(500);
                    long sleepTime = delayMs + jitter;
                    System.err.printf("[Warning] Network error: %s. Retrying attempt %d/%d after %d ms...%n",
                            e.getMessage(), attempt, maxRetries, sleepTime);
                    Thread.sleep(sleepTime);
                    delayMs *= 2;
                } else {
                    throw new IOException("Failed to connect to Gemini API after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Builds the Google Gemini REST JSON payload with system_instruction and contents.
     */
    private String buildRequestBody(String systemInstruction, String userPrompt) {
        JsonObject root = new JsonObject();

        // System Instruction
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            JsonObject sysInst = new JsonObject();
            JsonArray sysParts = new JsonArray();
            JsonObject sysText = new JsonObject();
            sysText.addProperty("text", systemInstruction);
            sysParts.add(sysText);
            sysInst.add("parts", sysParts);
            root.add("system_instruction", sysInst);
        }

        // Contents
        JsonArray contents = new JsonArray();
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userText = new JsonObject();
        userText.addProperty("text", userPrompt);
        userParts.add(userText);
        userContent.add("parts", userParts);
        contents.add(userContent);
        root.add("contents", contents);

        // Generation Config for deterministic, high quality pedagogical output
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("temperature", 0.2); // low temperature for precise code
        genConfig.addProperty("topP", 0.95);
        genConfig.addProperty("maxOutputTokens", 8192); // ensure zero truncation
        root.add("generationConfig", genConfig);

        return gson.toJson(root);
    }

    /**
     * Safely parses the Gemini API response JSON.
     */
    private String parseSuccessResponse(String responseBody) throws IOException {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

            if (!root.has("candidates")) {
                throw new IOException("Malformed Gemini response: missing 'candidates' array. Body: " + responseBody);
            }

            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates.isEmpty()) {
                throw new IOException("Gemini returned empty candidates. Content may have triggered safety filters.");
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            if (!firstCandidate.has("content")) {
                throw new IOException("Gemini candidate missing 'content' block. Body: " + responseBody);
            }

            JsonObject contentObj = firstCandidate.getAsJsonObject("content");
            JsonArray parts = contentObj.getAsJsonArray("parts");
            if (parts == null || parts.isEmpty()) {
                throw new IOException("Gemini response contained no content parts.");
            }

            StringBuilder fullText = new StringBuilder();
            for (JsonElement part : parts) {
                if (part.isJsonObject() && part.getAsJsonObject().has("text")) {
                    fullText.append(part.getAsJsonObject().get("text").getAsString());
                }
            }

            return fullText.toString();

        } catch (JsonSyntaxException e) {
            throw new IOException("Failed to parse Gemini JSON response: " + e.getMessage(), e);
        }
    }

    /**
     * Maps HTTP status codes to meaningful developer error messages.
     */
    private void handleHttpError(int statusCode, String body) throws IOException {
        String errorSnippet = extractErrorMessage(body);
        switch (statusCode) {
            case 400:
                throw new IOException("Gemini API Bad Request (HTTP 400): " + errorSnippet);
            case 401:
            case 403:
                throw new IOException("Gemini API Authentication Failed (HTTP " + statusCode + "): "
                        + "Please verify that your GEMINI_API_KEY environment variable is valid. Details: " + errorSnippet);
            case 404:
                throw new IOException("Gemini API Model Not Found (HTTP 404): "
                        + "Model '" + config.getModel() + "' is unavailable or URL is invalid. Details: " + errorSnippet);
            case 429:
                throw new IOException("Gemini API Quota Exceeded (HTTP 429): Rate limits exhausted. Details: " + errorSnippet);
            default:
                throw new IOException("Gemini API HTTP Error " + statusCode + ": " + errorSnippet);
        }
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "(empty response body)";
        }
        try {
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            if (obj.has("error") && obj.get("error").isJsonObject()) {
                JsonObject error = obj.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
        }
        return body.length() > 300 ? body.substring(0, 300) + "..." : body;
    }
}

