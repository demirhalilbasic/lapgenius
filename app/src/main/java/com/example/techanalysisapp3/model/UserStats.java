package com.example.techanalysisapp3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class UserStats {
    @DocumentId
    private String userId;

    @PropertyName("username")
    private String username;

    // Number of weekly analyses from 'weekly_user_stats'
    @PropertyName("count")
    private int weeklyAnalysisCount;

    // Final number of analyses from collection 'users'
    @PropertyName("totalAnalysisCount")
    private int totalAnalysisCount;

    public UserStats() {
        // Blank constructor for firestore purposes
    }

    public String getUserId() {
        return userId;
    }

    public String getUid() { return userId; }

    public String getUsername() {
        return username;
    }

    public int getWeeklyAnalysisCount() {
        return weeklyAnalysisCount;
    }

    public int getTotalAnalysisCount() {
        return totalAnalysisCount;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUid(String uid) { this.userId = uid; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWeeklyAnalysisCount(int weeklyAnalysisCount) {
        this.weeklyAnalysisCount = weeklyAnalysisCount;
    }

    public void setTotalAnalysisCount(int totalAnalysisCount) {
        this.totalAnalysisCount = totalAnalysisCount;
    }
}
