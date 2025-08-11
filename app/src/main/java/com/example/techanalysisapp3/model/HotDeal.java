package com.example.techanalysisapp3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.List;

public class HotDeal {
    @DocumentId
    private String id;  // Firestore document ID

    @PropertyName("olxTitle")
    private String olxTitle;

    private List<String> imageUrls;
    private String olxUrl;
    private String analysisText;
    private float rating;

    @PropertyName("username")
    private String username;

    private Date timestamp;

    @PropertyName("cityName")
    private String cityName;

    @PropertyName("latitude")
    private String latitude;

    @PropertyName("longitude")
    private String longitude;

    public HotDeal() {}

    // Document ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOlxTitle() { return olxTitle; }
    public void setOlxTitle(String olxTitle) { this.olxTitle = olxTitle; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getOlxUrl() { return olxUrl; }
    public void setOlxUrl(String olxUrl) { this.olxUrl = olxUrl; }

    public String getAnalysisText() { return analysisText; }
    public void setAnalysisText(String analysisText) { this.analysisText = analysisText; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
}