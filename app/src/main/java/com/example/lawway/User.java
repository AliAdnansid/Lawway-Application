package com.example.lawway;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String userType; // "Client" or "Lawyer"
    private @ServerTimestamp Timestamp createdAt;
    
    // Optional fields (can be null)
    private String phone;
    private String profileImageUrl;
    
    // Lawyer-specific fields (only used when userType == "Lawyer")
    private String specialization;
    private Integer experience; // years of experience
    private Double rating; // average rating
    private Integer totalCases; // total cases handled
    
    // Client-specific fields (only used when userType == "Client")
    private String address;

    // Default constructor (required for Firestore)
    public User() {
    }

    // Constructor for basic user creation
    public User(String userId, String fullName, String email, String userType) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Lawyer-specific getters and setters
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(Integer totalCases) {
        this.totalCases = totalCases;
    }

    // Client-specific getters and setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
