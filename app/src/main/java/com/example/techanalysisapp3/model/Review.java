package com.example.techanalysisapp3.model;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.List;

public class Review {
    private String userId;
    private String olxTitle;
    private String olxTitleLower;
    private String analysisText;
    private List<String> imageUrls;
    private String olxUrl;
    private Date timestamp;
    private String documentId;
    @PropertyName("cityName")
    private String cityName;

    @PropertyName("latitude")
    private String latitude;

    @PropertyName("longitude")
    private String longitude;

    public Review() {
        // Blank constructor for firestore purposes
    }

    public String getUserId() { return userId; }
    public String getOlxTitle() { return olxTitle; }
    public String getOlxTitleLower() { return olxTitleLower; }
    public String getAnalysisText() { return analysisText; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getOlxUrl() { return olxUrl; }
    public Date getTimestamp() { return timestamp; }
    public String getDocumentId() { return documentId; }
    public String getCityName() { return cityName; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setOlxTitle(String olxTitle) {
        this.olxTitle = olxTitle;
        this.olxTitleLower = olxTitle != null ? olxTitle.toLowerCase() : null;
    }
    public void setOlxTitleLower(String olxTitleLower) { this.olxTitleLower = olxTitleLower; }
    public void setAnalysisText(String analysisText) { this.analysisText = analysisText; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setOlxUrl(String olxUrl) { this.olxUrl = olxUrl; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
}
