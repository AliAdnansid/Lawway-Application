package com.example.lawway;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RequestHelper {
    private static final String COLLECTION_NAME = "requests";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ==================== CREATE ====================
    /**
     * Create a new case request (status auto-set to "pending")
     * @param request Request object to create
     * @return Task<DocumentReference>
     */
    public static Task<DocumentReference> createRequest(Request request) {
        request.setStatus("pending"); // Auto-set to pending
        Map<String, Object> requestData = requestToMap(request);
        requestData.put("createdAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .add(requestData);
    }

    // ==================== READ ====================
    /**
     * Get request by ID
     * @param requestId Request ID
     * @return Task<DocumentSnapshot>
     */
    public static Task<DocumentSnapshot> getRequestById(String requestId) {
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .get();
    }

    /**
     * Get request document reference
     * @param requestId Request ID
     * @return DocumentReference
     */
    public static DocumentReference getRequestReference(String requestId) {
        return db.collection(COLLECTION_NAME).document(requestId);
    }

    /**
     * Get all requests for a case
     * @param caseId Case ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getRequestsByCaseId(String caseId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("caseId", caseId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all pending requests for a lawyer
     * @param lawyerId Lawyer ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getPendingRequestsByLawyerId(String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("lawyerId", lawyerId)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all requests for a lawyer (all statuses)
     * @param lawyerId Lawyer ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getRequestsByLawyerId(String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("lawyerId", lawyerId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("status")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all accepted requests for a lawyer
     * @param lawyerId Lawyer ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getAcceptedRequestsByLawyerId(String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("lawyerId", lawyerId)
                .whereEqualTo("status", "accepted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all requests sent by a client
     * @param clientId Client ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getRequestsByClientId(String clientId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("clientId", clientId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("status")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get requests by status
     * @param status Request status
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getRequestsByStatus(String status) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all requests for a specific case-lawyer combination
     * @param caseId Case ID
     * @param lawyerId Lawyer ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getRequestByCaseAndLawyer(String caseId, String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("caseId", caseId)
                .whereEqualTo("lawyerId", lawyerId)
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    /**
     * Get all requests (be careful with this - use pagination in production)
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getAllRequests() {
        return db.collection(COLLECTION_NAME)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    // ==================== UPDATE ====================
    /**
     * Update request by ID
     * @param requestId Request ID
     * @param updates Map of fields to update
     * @return Task<Void>
     */
    public static Task<Void> updateRequest(String requestId, Map<String, Object> updates) {
        updates.put("respondedAt", Timestamp.now());
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    /**
     * Update request with Request object
     * @param requestId Request ID
     * @param request Request object with updated data
     * @return Task<Void>
     */
    public static Task<Void> updateRequest(String requestId, Request request) {
        Map<String, Object> updates = requestToMap(request);
        updates.remove("requestId"); // Don't update the ID
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    /**
     * Accept a request (only updates request, not case directly)
     * @param requestId Request ID
     * @return Task<Void>
     */
    public static Task<Void> acceptRequest(String requestId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    /**
     * Reject a request
     * @param requestId Request ID
     * @param rejectedReason Reason for rejection (optional)
     * @return Task<Void>
     */
    public static Task<Void> rejectRequest(String requestId, String rejectedReason) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("respondedAt", Timestamp.now());
        if (rejectedReason != null && !rejectedReason.isEmpty()) {
            updates.put("rejectedReason", rejectedReason);
        }
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    /**
     * Update request status
     * @param requestId Request ID
     * @param status New status
     * @return Task<Void>
     */
    public static Task<Void> updateRequestStatus(String requestId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    // ==================== DELETE ====================
    /**
     * Soft delete request (set status to "deleted")
     * @param requestId Request ID
     * @return Task<Void>
     */
    public static Task<Void> deleteRequest(String requestId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "deleted");
        updates.put("deletedAt", Timestamp.now());
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    /**
     * Hard delete request (permanently remove from database)
     * Use with caution!
     * @param requestId Request ID
     * @return Task<Void>
     */
    public static Task<Void> hardDeleteRequest(String requestId) {
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .delete();
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Convert Request object to Map for Firestore
     * @param request Request object
     * @return Map<String, Object>
     */
    private static Map<String, Object> requestToMap(Request request) {
        Map<String, Object> map = new HashMap<>();
        if (request.getRequestId() != null) map.put("requestId", request.getRequestId());
        if (request.getCaseId() != null) map.put("caseId", request.getCaseId());
        if (request.getClientId() != null) map.put("clientId", request.getClientId());
        if (request.getLawyerId() != null) map.put("lawyerId", request.getLawyerId());
        if (request.getStatus() != null) map.put("status", request.getStatus());
        if (request.getMessage() != null) map.put("message", request.getMessage());
        if (request.getRejectedReason() != null) map.put("rejectedReason", request.getRejectedReason());
        if (request.getRespondedAt() != null) map.put("respondedAt", request.getRespondedAt());
        
        return map;
    }

    /**
     * Convert DocumentSnapshot to Request object
     * @param document DocumentSnapshot
     * @return Request object
     */
    public static Request documentToRequest(DocumentSnapshot document) {
        if (!document.exists()) return null;
        
        Request request = new Request();
        request.setRequestId(document.getId());
        request.setCaseId(document.getString("caseId"));
        request.setClientId(document.getString("clientId"));
        request.setLawyerId(document.getString("lawyerId"));
        request.setStatus(document.getString("status"));
        request.setMessage(document.getString("message"));
        request.setRejectedReason(document.getString("rejectedReason"));
        
        // Timestamps
        if (document.getTimestamp("createdAt") != null) {
            request.setCreatedAt(document.getTimestamp("createdAt"));
        }
        if (document.getTimestamp("respondedAt") != null) {
            request.setRespondedAt(document.getTimestamp("respondedAt"));
        }
        
        return request;
    }
}
