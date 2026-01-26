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

    public static Task<DocumentReference> createRequest(Request request) {
        request.setStatus("pending");
        Map<String, Object> requestData = requestToMap(request);
        requestData.put("createdAt", Timestamp.now());
        
        Task<DocumentReference> task = db.collection(COLLECTION_NAME)
                .add(requestData);
        
        task.addOnSuccessListener(documentReference -> {
            if (request.getLawyerId() != null) {
                sendNewRequestNotification(request.getLawyerId(), request.getClientId(), documentReference.getId());
            }
        });
        
        return task;
    }
    
    private static void sendNewRequestNotification(String lawyerId, String clientId, String requestId) {
        UserHelper.getUserById(clientId)
            .addOnSuccessListener(clientDoc -> {
                String clientName = "A client";
                if (clientDoc.exists()) {
                    User client = UserHelper.documentToUser(clientDoc);
                    if (client != null && client.getFullName() != null) {
                        clientName = client.getFullName();
                    }
                }
                
                String title = "New Request Received";
                String message = clientName + " has sent you a new case request";
                
                NotificationHelper.sendNotificationToUser(lawyerId, title, message, requestId, "Lawyer");
            });
    }

    public static Task<DocumentSnapshot> getRequestById(String requestId) {
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .get();
    }

    public static DocumentReference getRequestReference(String requestId) {
        return db.collection(COLLECTION_NAME).document(requestId);
    }

    public static Task<QuerySnapshot> getRequestsByCaseId(String caseId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("caseId", caseId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<QuerySnapshot> getPendingRequestsByLawyerId(String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("lawyerId", lawyerId)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<QuerySnapshot> getRequestsByLawyerId(String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("lawyerId", lawyerId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("status")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<QuerySnapshot> getAcceptedRequestsByLawyerId(String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("lawyerId", lawyerId)
                .whereEqualTo("status", "accepted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<QuerySnapshot> getRequestsByClientId(String clientId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("clientId", clientId)
                .whereNotEqualTo("status", "deleted")
                .orderBy("status")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<QuerySnapshot> getRequestsByStatus(String status) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<QuerySnapshot> getRequestByCaseAndLawyer(String caseId, String lawyerId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("caseId", caseId)
                .whereEqualTo("lawyerId", lawyerId)
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    public static Task<QuerySnapshot> getAllRequests() {
        return db.collection(COLLECTION_NAME)
                .whereNotEqualTo("status", "deleted")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public static Task<Void> updateRequest(String requestId, Map<String, Object> updates) {
        updates.put("respondedAt", Timestamp.now());
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    public static Task<Void> updateRequest(String requestId, Request request) {
        Map<String, Object> updates = requestToMap(request);
        updates.remove("requestId");
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    public static Task<Void> acceptRequest(String requestId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("respondedAt", Timestamp.now());
        
        Task<Void> task = db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
        
        task.addOnSuccessListener(aVoid -> {
            getRequestById(requestId)
                .addOnSuccessListener(requestDoc -> {
                    if (requestDoc.exists()) {
                        Request request = documentToRequest(requestDoc);
                        if (request != null && request.getClientId() != null) {
                            sendAcceptedNotification(request.getClientId(), requestId);
                        }
                    }
                });
        });
        
        return task;
    }
    
    private static void sendAcceptedNotification(String clientId, String requestId) {
        UserHelper.getUserById(clientId)
            .addOnSuccessListener(clientDoc -> {
                String title = "Request Accepted";
                String message = "Your case request has been accepted by the lawyer";
                
                NotificationHelper.sendNotificationToUser(clientId, title, message, requestId, "Client");
            });
    }

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

    public static Task<Void> updateRequestStatus(String requestId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    public static Task<Void> deleteRequest(String requestId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "deleted");
        updates.put("deletedAt", Timestamp.now());
        updates.put("respondedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .update(updates);
    }

    public static Task<Void> hardDeleteRequest(String requestId) {
        return db.collection(COLLECTION_NAME)
                .document(requestId)
                .delete();
    }

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
        
        if (document.getTimestamp("createdAt") != null) {
            request.setCreatedAt(document.getTimestamp("createdAt"));
        }
        if (document.getTimestamp("respondedAt") != null) {
            request.setRespondedAt(document.getTimestamp("respondedAt"));
        }
        
        return request;
    }
}
