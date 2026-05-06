package com.example.group_project.model;

import java.util.List;

public class Review {

    private String reviewId;
    private String spotId;
    private String authorId;
    private String userId;
    private String username;
    private String userEmail;
    private long timestamp;
    private long clientCreatedAt;

    private String spotName;
    private String buildingName;
    private String roomNumber;

    private double starRating;
    private String description;
    private List<String> traits;

    private boolean quiet;
    private boolean moderatelyLoud;
    private boolean loud;
    private boolean visible;
    private boolean secluded;

    public Review() {
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getClientCreatedAt() {
        return clientCreatedAt;
    }

    public void setClientCreatedAt(long clientCreatedAt) {
        this.clientCreatedAt = clientCreatedAt;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getStarRating() {
        return starRating;
    }

    public void setStarRating(double starRating) {
        this.starRating = starRating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTraits() {
        return traits;
    }

    public void setTraits(List<String> traits) {
        this.traits = traits;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isModeratelyLoud() {
        return moderatelyLoud;
    }

    public void setModeratelyLoud(boolean moderatelyLoud) {
        this.moderatelyLoud = moderatelyLoud;
    }

    public boolean isLoud() {
        return loud;
    }

    public void setLoud(boolean loud) {
        this.loud = loud;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSecluded() {
        return secluded;
    }

    public void setSecluded(boolean secluded) {
        this.secluded = secluded;
    }
}
