package com.example.lawway;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Request {
    private String requestId;
    private String caseId;
    private String clientId;
    private String lawyerId;
    private String status; // "pending", "accepted", "rejected", "deleted"
    private String message;
    private @ServerTimestamp Timestamp createdAt;
    private Timestamp respondedAt;
    private String rejectedReason;

    // Default constructor (required for Firestore)
    public Request() {
    }

    // Constructor for request creation
    public Request(String caseId, String clientId, String lawyerId, String message) {
        this.caseId = caseId;
        this.clientId = clientId;
        this.lawyerId = lawyerId;
        this.message = message;
        this.status = "pending"; // Auto-set to pending
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

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

    public String getLawyerId() {
        return lawyerId;
    }

    public void setLawyerId(String lawyerId) {
        this.lawyerId = lawyerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }
}
