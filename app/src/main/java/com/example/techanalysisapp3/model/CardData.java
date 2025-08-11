package com.example.techanalysisapp3.model;

public class CardData {
    private final String title;
    private final String content;
    private final String imageUrl;

    // TEXT CARD
    public CardData(String title, String content) {
        this.title = title;
        this.content = content;
        this.imageUrl = null;
    }

    // IMAGE CARD
    public CardData(String imageUrl) {
        this.title = null;
        this.content = null;
        this.imageUrl = imageUrl;
    }

    public String getTitle()   { return title; }
    public String getContent() { return content; }
    public String getImageUrl(){ return imageUrl; }
    public boolean isImageCard(){ return imageUrl != null; }
}