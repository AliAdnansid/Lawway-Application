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

public class CaseHelper {
    private static final String COLLECTION_NAME = "cases";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ==================== CREATE ====================
    /**
     * Create a new case (status auto-set to "pending")
     * @param caseObj Case object to create
     * @return Task<DocumentReference>
     */
    public static Task<DocumentReference> createCase(Case caseObj) {
        caseObj.setStatus("pending"); // Auto-set to pending
        Map<String, Object> caseData = caseToMap(caseObj);
        caseData.put("createdAt", Timestamp.now());
        caseData.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .add(caseData);
    }

    // ==================== READ ====================
    /**
     * Get case by ID
     * @param caseId Case ID
     * @return Task<DocumentSnapshot>
     */
    public static Task<DocumentSnapshot> getCaseById(String caseId) {
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .get();
    }

    /**
     * Get case document reference
     * @param caseId Case ID
     * @return DocumentReference
     */
    public static DocumentReference getCaseReference(String caseId) {
        return db.collection(COLLECTION_NAME).document(caseId);
    }

    /**
     * Get all cases for a client
     * @param clientId Client ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getCasesByClientId(String clientId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("clientId", clientId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("status")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get active cases for a client (pending, active)
     * @param clientId Client ID
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getActiveCasesByClientId(String clientId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("clientId", clientId)
                .whereIn("status", java.util.Arrays.asList("pending", "active"))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get cases by status
     * @param status Case status
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getCasesByStatus(String status) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get cases by category
     * @param category Case category
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getCasesByCategory(String category) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("category", category)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get cases by priority
     * @param priority Case priority
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getCasesByPriority(String priority) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("priority", priority)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all cases (be careful with this - use pagination in production)
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getAllCases() {
        return db.collection(COLLECTION_NAME)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    // ==================== UPDATE ====================
    /**
     * Update case by ID
     * @param caseId Case ID
     * @param updates Map of fields to update
     * @return Task<Void>
     */
    public static Task<Void> updateCase(String caseId, Map<String, Object> updates) {
        updates.put("updatedAt", Timestamp.now());
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .update(updates);
    }

    /**
     * Update case with Case object
     * @param caseId Case ID
     * @param caseObj Case object with updated data
     * @return Task<Void>
     */
    public static Task<Void> updateCase(String caseId, Case caseObj) {
        Map<String, Object> updates = caseToMap(caseObj);
        updates.remove("caseId"); // Don't update the ID
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .update(updates);
    }

    /**
     * Update case status
     * @param caseId Case ID
     * @param status New status
     * @return Task<Void>
     */
    public static Task<Void> updateCaseStatus(String caseId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", Timestamp.now());
        
        // If closing the case, set closedAt timestamp
        if (status.equals("closed")) {
            updates.put("closedAt", Timestamp.now());
        }
        
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .update(updates);
    }

    /**
     * Update case priority
     * @param caseId Case ID
     * @param priority New priority
     * @return Task<Void>
     */
    public static Task<Void> updateCasePriority(String caseId, String priority) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("priority", priority);
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .update(updates);
    }

    // ==================== DELETE ====================
    /**
     * Soft delete case (set status to "deleted")
     * @param caseId Case ID
     * @return Task<Void>
     */
    public static Task<Void> deleteCase(String caseId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "deleted");
        updates.put("deletedAt", Timestamp.now());
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .update(updates);
    }

    /**
     * Hard delete case (permanently remove from database)
     * Use with caution!
     * @param caseId Case ID
     * @return Task<Void>
     */
    public static Task<Void> hardDeleteCase(String caseId) {
        return db.collection(COLLECTION_NAME)
                .document(caseId)
                .delete();
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Convert Case object to Map for Firestore
     * @param caseObj Case object
     * @return Map<String, Object>
     */
    private static Map<String, Object> caseToMap(Case caseObj) {
        Map<String, Object> map = new HashMap<>();
        if (caseObj.getCaseId() != null) map.put("caseId", caseObj.getCaseId());
        if (caseObj.getClientId() != null) map.put("clientId", caseObj.getClientId());
        if (caseObj.getTitle() != null) map.put("title", caseObj.getTitle());
        if (caseObj.getDescription() != null) map.put("description", caseObj.getDescription());
        if (caseObj.getCategory() != null) map.put("category", caseObj.getCategory());
        if (caseObj.getStatus() != null) map.put("status", caseObj.getStatus());
        if (caseObj.getPriority() != null) map.put("priority", caseObj.getPriority());
        if (caseObj.getClosedAt() != null) map.put("closedAt", caseObj.getClosedAt());
        
        return map;
    }

    /**
     * Convert DocumentSnapshot to Case object
     * @param document DocumentSnapshot
     * @return Case object
     */
    public static Case documentToCase(DocumentSnapshot document) {
        if (!document.exists()) return null;
        
        Case caseObj = new Case();
        caseObj.setCaseId(document.getId());
        caseObj.setClientId(document.getString("clientId"));
        caseObj.setTitle(document.getString("title"));
        caseObj.setDescription(document.getString("description"));
        caseObj.setCategory(document.getString("category"));
        caseObj.setStatus(document.getString("status"));
        caseObj.setPriority(document.getString("priority"));
        
        // Timestamps
        if (document.getTimestamp("createdAt") != null) {
            caseObj.setCreatedAt(document.getTimestamp("createdAt"));
        }
        if (document.getTimestamp("updatedAt") != null) {
            caseObj.setUpdatedAt(document.getTimestamp("updatedAt"));
        }
        if (document.getTimestamp("closedAt") != null) {
            caseObj.setClosedAt(document.getTimestamp("closedAt"));
        }
        
        return caseObj;
    }
}
