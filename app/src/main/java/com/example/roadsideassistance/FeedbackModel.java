package com.example.roadsideassistance;

public class FeedbackModel {
    private String feedbackId;
    private String requestId;
    private String userId;
    private float rating;
    private String comment;
    private long timestamp;

    public FeedbackModel() {
        // Default constructor for Firebase
    }

    public FeedbackModel(String feedbackId, String requestId, String userId, float rating, String comment, long timestamp) {
        this.feedbackId = feedbackId;
        this.requestId = requestId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
