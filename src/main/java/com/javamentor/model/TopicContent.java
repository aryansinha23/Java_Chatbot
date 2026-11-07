package com.javamentor.model;

import java.util.List;

/**
 * Data model representing a curated topic in the offline knowledge base.
 */
public class TopicContent {

    private String id;
    private String title;
    private String category;
    private List<String> keywords;
    private String phase1Conceptual;
    private String phase2Algorithmic;
    private String phase3Code;

    public TopicContent() {
    }

    public TopicContent(String id, String title, String category, List<String> keywords,
                        String phase1Conceptual, String phase2Algorithmic, String phase3Code) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.keywords = keywords;
        this.phase1Conceptual = phase1Conceptual;
        this.phase2Algorithmic = phase2Algorithmic;
        this.phase3Code = phase3Code;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getKeywords() {
        return keywords;
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
}

