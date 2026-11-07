package com.javamentor.engine;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.javamentor.model.TopicContent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads the curated catalog of 20+ core Java topics from resources.
 */
public class KnowledgeRepository {

    private final List<TopicContent> topics;

    public KnowledgeRepository() {
        this.topics = loadCatalog();
    }

    private List<TopicContent> loadCatalog() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("offline-catalog.json")) {
            if (is == null) {
                System.err.println("[Warning] offline-catalog.json resource not found. Initializing empty repository.");
                return new ArrayList<>();
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<List<TopicContent>>() {}.getType();
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                List<TopicContent> loaded = gson.fromJson(reader, listType);
                return loaded != null ? loaded : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("[Warning] Failed to load offline-catalog.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TopicContent> getTopics() {
        return Collections.unmodifiableList(topics);
    }
}
