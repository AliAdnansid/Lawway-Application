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

public class UserHelper {
    private static final String COLLECTION_NAME = "users";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ==================== CREATE ====================
    /**
     * Create a new user
     * @param user User object to create
     * @return Task<DocumentReference>
     */
    public static Task<DocumentReference> createUser(User user) {
        Map<String, Object> userData = userToMap(user);
        userData.put("createdAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .add(userData);
    }

    /**
     * Create a user with a specific document ID (userId)
     * @param userId The document ID to use (usually Firebase Auth UID)
     * @param user User object to create
     * @return Task<Void>
     */
    public static Task<Void> createUserWithId(String userId, User user) {
        user.setUserId(userId);
        Map<String, Object> userData = userToMap(user);
        userData.put("createdAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .set(userData);
    }

    // ==================== READ ====================
    /**
     * Get user by ID
     * @param userId User ID
     * @return Task<DocumentSnapshot>
     */
    public static Task<DocumentSnapshot> getUserById(String userId) {
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .get();
    }

    /**
     * Get user document reference
     * @param userId User ID
     * @return DocumentReference
     */
    public static DocumentReference getUserReference(String userId) {
        return db.collection(COLLECTION_NAME).document(userId);
    }

    /**
     * Get all users (be careful with this in production - use pagination)
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getAllUsers() {
        return db.collection(COLLECTION_NAME)
                .get();
    }

    /**
     * Get all lawyers
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getAllLawyers() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Lawyer")
                .whereNotEqualTo("status", "deleted") // Exclude soft-deleted
                .get();
    }

    /**
     * Get all clients
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getAllClients() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Client")
                .whereNotEqualTo("status", "deleted") // Exclude soft-deleted
                .get();
    }

    /**
     * Get lawyers by specialization
     * @param specialization Specialization to filter by
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> getLawyersBySpecialization(String specialization) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Lawyer")
                .whereEqualTo("specialization", specialization)
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    /**
     * Search lawyers by name
     * @param searchQuery Name to search for
     * @return Task<QuerySnapshot>
     */
    public static Task<QuerySnapshot> searchLawyersByName(String searchQuery) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Lawyer")
                .whereGreaterThanOrEqualTo("fullName", searchQuery)
                .whereLessThanOrEqualTo("fullName", searchQuery + "\uf8ff")
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    // ==================== UPDATE ====================
    /**
     * Update user by ID
     * @param userId User ID
     * @param updates Map of fields to update
     * @return Task<Void>
     */
    public static Task<Void> updateUser(String userId, Map<String, Object> updates) {
        updates.put("updatedAt", Timestamp.now());
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    /**
     * Update user with User object
     * @param userId User ID
     * @param user User object with updated data
     * @return Task<Void>
     */
    public static Task<Void> updateUser(String userId, User user) {
        Map<String, Object> updates = userToMap(user);
        updates.remove("userId"); // Don't update the ID
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    /**
     * Update lawyer-specific fields
     * @param userId User ID (must be a lawyer)
     * @param specialization Specialization
     * @param experience Years of experience
     * @return Task<Void>
     */
    public static Task<Void> updateLawyerProfile(String userId, String specialization, Integer experience) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("specialization", specialization);
        updates.put("experience", experience);
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    /**
     * Update client-specific fields
     * @param userId User ID (must be a client)
     * @param address Address
     * @param phone Phone number
     * @return Task<Void>
     */
    public static Task<Void> updateClientProfile(String userId, String address, String phone) {
        Map<String, Object> updates = new HashMap<>();
        if (address != null) updates.put("address", address);
        if (phone != null) updates.put("phone", phone);
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    // ==================== DELETE ====================
    /**
     * Soft delete user (set status to "deleted")
     * @param userId User ID
     * @return Task<Void>
     */
    public static Task<Void> deleteUser(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "deleted");
        updates.put("deletedAt", Timestamp.now());
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    /**
     * Hard delete user (permanently remove from database)
     * Use with caution!
     * @param userId User ID
     * @return Task<Void>
     */
    public static Task<Void> hardDeleteUser(String userId) {
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .delete();
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Convert User object to Map for Firestore
     * @param user User object
     * @return Map<String, Object>
     */
    private static Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user.getUserId() != null) map.put("userId", user.getUserId());
        if (user.getFullName() != null) map.put("fullName", user.getFullName());
        if (user.getEmail() != null) map.put("email", user.getEmail());
        if (user.getUserType() != null) map.put("userType", user.getUserType());
        if (user.getPhone() != null) map.put("phone", user.getPhone());
        if (user.getProfileImageUrl() != null) map.put("profileImageUrl", user.getProfileImageUrl());
        
        // Lawyer-specific fields
        if (user.getSpecialization() != null) map.put("specialization", user.getSpecialization());
        if (user.getExperience() != null) map.put("experience", user.getExperience());
        if (user.getRating() != null) map.put("rating", user.getRating());
        if (user.getTotalCases() != null) map.put("totalCases", user.getTotalCases());
        
        // Client-specific fields
        if (user.getAddress() != null) map.put("address", user.getAddress());
        
        return map;
    }

    /**
     * Convert DocumentSnapshot to User object
     * @param document DocumentSnapshot
     * @return User object
     */
    public static User documentToUser(DocumentSnapshot document) {
        if (!document.exists()) return null;
        
        User user = new User();
        user.setUserId(document.getId());
        user.setFullName(document.getString("fullName"));
        user.setEmail(document.getString("email"));
        user.setUserType(document.getString("userType"));
        user.setPhone(document.getString("phone"));
        user.setProfileImageUrl(document.getString("profileImageUrl"));
        
        // Lawyer-specific fields
        user.setSpecialization(document.getString("specialization"));
        if (document.get("experience") != null) {
            user.setExperience(document.getLong("experience").intValue());
        }
        if (document.get("rating") != null) {
            user.setRating(document.getDouble("rating"));
        }
        if (document.get("totalCases") != null) {
            user.setTotalCases(document.getLong("totalCases").intValue());
        }
        
        // Client-specific fields
        user.setAddress(document.getString("address"));
        
        // Timestamps
        if (document.getTimestamp("createdAt") != null) {
            user.setCreatedAt(document.getTimestamp("createdAt"));
        }
        
        return user;
    }
}
