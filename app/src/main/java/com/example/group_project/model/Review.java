package com.example.group_project.model;

import java.util.List;

public class Review {

    // Metadata
    private String reviewId;
    private String authorId;
    private long timestamp;

    // Location
    private String spotName;
    private String buildingName;
    private String roomNumber;
    private double latitude;
    private double longitude;

    // Review Content
    private float starRating;
    private String description;
    private List<String> imageUrls;

    // Traits
    private boolean isQuiet;
    private boolean isModeratelyLoud;
    private boolean isLoud;
    private boolean isVisible;
    private boolean isSecluded;

    public Review() {
    }

    public Review(String reviewId, String authorId, long timestamp, String spotName,
                  String buildingName, String roomNumber, double latitude, double longitude,
                  float starRating, String description, List<String> imageUrls,
                  boolean isQuiet, boolean isModeratelyLoud, boolean isLoud,
                  boolean isVisible, boolean isSecluded) {
        this.reviewId = reviewId;
        this.authorId = authorId;
        this.timestamp = timestamp;
        this.spotName = spotName;
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.starRating = starRating;
        this.description = description;
        this.imageUrls = imageUrls;
        this.isQuiet = isQuiet;
        this.isModeratelyLoud = isModeratelyLoud;
        this.isLoud = isLoud;
        this.isVisible = isVisible;
        this.isSecluded = isSecluded;
    }

    // Getters - Setters
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getSpotName() { return spotName;}
    public void setSpotName(String spotName) { this.spotName = spotName; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getStarRating() { return starRating; }
    public void setStarRating(float starRating) { this.starRating = starRating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public boolean isQuiet() { return isQuiet; }
    public void setQuiet(boolean quiet) { isQuiet = quiet; }

    public boolean isModeratelyLoud() { return isModeratelyLoud; }
    public void setModeratelyLoud(boolean moderatelyLoud) { isModeratelyLoud = moderatelyLoud; }

    public boolean isLoud() { return isLoud; }
    public void setLoud(boolean loud) { isLoud = loud; }

    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { isVisible = visible; }

    public boolean isSecluded() { return isSecluded; }
    public void setSecluded(boolean secluded) { isSecluded = secluded; }
}