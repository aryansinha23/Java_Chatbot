package com.javamentor.client;

import java.time.Duration;

/**
 * Configuration for Google Gemini Generative AI REST Client.
 * Adheres strictly to Git security hygiene: never stores or commits hardcoded keys;
 * loads from environment variable GEMINI_API_KEY.
 */
public class GeminiConfig {

    public static final String ENV_API_KEY = "GEMINI_API_KEY";
    public static final String DEFAULT_MODEL = "gemini-1.5-flash";
    public static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final String apiKey;
    private final String model;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final int maxRetries;
    private final long backoffBaseDelayMs;

    public GeminiConfig() {
        this(resolveApiKey(), resolveModel(), Duration.ofSeconds(15), Duration.ofSeconds(45), 3, 1000);
    }

    public GeminiConfig(String apiKey, String model, Duration connectTimeout,
                        Duration requestTimeout, int maxRetries, long backoffBaseDelayMs) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = (model != null && !model.isBlank()) ? model.trim() : DEFAULT_MODEL;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        this.maxRetries = maxRetries;
        this.backoffBaseDelayMs = backoffBaseDelayMs;
    }

    private static String resolveApiKey() {
        String env = System.getenv(ENV_API_KEY);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = System.getProperty("gemini.api.key");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return "";
    }

    private static String resolveModel() {
        String env = System.getenv("GEMINI_MODEL");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = System.getProperty("gemini.model");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return DEFAULT_MODEL;
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getBackoffBaseDelayMs() {
        return backoffBaseDelayMs;
    }

    public String getEndpointUrl() {
        return BASE_URL + model + ":generateContent?key=" + apiKey;
    }
}
