package com.example.techanalysisapp3.model;

public class Brand {
    private final String key;
    private final int logoResId;
    private final String name;
    private final String shortDescription;
    private final String fullDescription;
    private final String olxUrl;

    public Brand(String key, int logoResId, String name,
                 String shortDescription, String fullDescription,
                 String olxUrl) {
        this.key = key;
        this.logoResId = logoResId;
        this.name = name;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.olxUrl = olxUrl;
    }

    public String getKey() { return key; }
    public int getLogoResId() { return logoResId; }
    public String getName() { return name; }
    public String getShortDescription() { return shortDescription; }
    public String getFullDescription() { return fullDescription; }
    public String getOlxUrl() { return olxUrl; }
}