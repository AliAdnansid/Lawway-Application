package com.example.lawway;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Case {
    private String caseId;
    private String clientId;
    private String title;
    private String description;
    private String category;
    private String status; // "pending", "active", "closed", "cancelled", "deleted"
    private String priority; // "low", "medium", "high"
    private @ServerTimestamp Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp closedAt;

    // Default constructor (required for Firestore)
    public Case() {
    }

    // Constructor for case creation
    public Case(String clientId, String title, String description, String category) {
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = "pending"; // Auto-set to pending
        this.priority = "medium"; // Default priority
    }

    // Getters and Setters
    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Timestamp closedAt) {
        this.closedAt = closedAt;
    }
}
