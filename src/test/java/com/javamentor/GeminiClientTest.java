package com.javamentor;

import com.javamentor.client.GeminiApiClient;
import com.javamentor.client.GeminiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Google Gemini API Client configuration and resilience parameters.
 */
public class GeminiClientTest {

    @Test
    @DisplayName("Verify GeminiConfig default parameters and security")
    void testGeminiConfigDefaults() {
        GeminiConfig config = new GeminiConfig("", "gemini-1.5-flash",
                java.time.Duration.ofSeconds(10), java.time.Duration.ofSeconds(30), 3, 1000);

        assertFalse(config.isConfigured(), "Empty key should mark config as not configured");
        assertEquals("gemini-1.5-flash", config.getModel());
        assertEquals(3, config.getMaxRetries());
        assertEquals(1000, config.getBackoffBaseDelayMs());
    }

    @Test
    @DisplayName("Verify GeminiApiClient throws IllegalStateException when unconfigured")
    void testClientThrowsWhenUnconfigured() {
        GeminiConfig config = new GeminiConfig("", "gemini-1.5-flash",
                java.time.Duration.ofSeconds(10), java.time.Duration.ofSeconds(30), 3, 1000);
        GeminiApiClient client = new GeminiApiClient(config);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            client.generateContent("System Prompt", "User query");
        });

        assertTrue(exception.getMessage().contains("GEMINI_API_KEY"),
                "Exception message must guide developer to set GEMINI_API_KEY");
    }

    @Test
    @DisplayName("Verify Endpoint URL construction with API key")
    void testEndpointUrl() {
        GeminiConfig config = new GeminiConfig("test-key-12345", "gemini-1.5-flash",
                java.time.Duration.ofSeconds(10), java.time.Duration.ofSeconds(30), 3, 1000);

        assertTrue(config.isConfigured());
        String url = config.getEndpointUrl();
        assertTrue(url.contains("gemini-1.5-flash"), "URL must contain specified model");
        assertTrue(url.contains("key=test-key-12345"), "URL must attach API key param");
    }
}

