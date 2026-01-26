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

    public static Task<DocumentReference> createUser(User user) {
        Map<String, Object> userData = userToMap(user);
        userData.put("createdAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .add(userData);
    }

    public static Task<Void> createUserWithId(String userId, User user) {
        user.setUserId(userId);
        Map<String, Object> userData = userToMap(user);
        userData.put("createdAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .set(userData);
    }

    public static Task<DocumentSnapshot> getUserById(String userId) {
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .get();
    }

    public static DocumentReference getUserReference(String userId) {
        return db.collection(COLLECTION_NAME).document(userId);
    }

    public static Task<QuerySnapshot> getAllUsers() {
        return db.collection(COLLECTION_NAME)
                .get();
    }

    public static Task<QuerySnapshot> getAllLawyers() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Lawyer")
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    public static Task<QuerySnapshot> getAllClients() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Client")
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    public static Task<QuerySnapshot> getLawyersBySpecialization(String specialization) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Lawyer")
                .whereEqualTo("specialization", specialization)
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    public static Task<QuerySnapshot> searchLawyersByName(String searchQuery) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userType", "Lawyer")
                .whereGreaterThanOrEqualTo("fullName", searchQuery)
                .whereLessThanOrEqualTo("fullName", searchQuery + "\uf8ff")
                .whereNotEqualTo("status", "deleted")
                .get();
    }

    public static Task<Void> updateUser(String userId, Map<String, Object> updates) {
        updates.put("updatedAt", Timestamp.now());
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    public static Task<Void> updateUser(String userId, User user) {
        Map<String, Object> updates = userToMap(user);
        updates.remove("userId");
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    public static Task<Void> updateLawyerProfile(String userId, String specialization, Integer experience) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("specialization", specialization);
        updates.put("experience", experience);
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    public static Task<Void> updateClientProfile(String userId, String address, String phone) {
        Map<String, Object> updates = new HashMap<>();
        if (address != null) updates.put("address", address);
        if (phone != null) updates.put("phone", phone);
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    public static Task<Void> deleteUser(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "deleted");
        updates.put("deletedAt", Timestamp.now());
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }

    public static Task<Void> hardDeleteUser(String userId) {
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .delete();
    }

    private static Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user.getUserId() != null) map.put("userId", user.getUserId());
        if (user.getFullName() != null) map.put("fullName", user.getFullName());
        if (user.getEmail() != null) map.put("email", user.getEmail());
        if (user.getUserType() != null) map.put("userType", user.getUserType());
        if (user.getPhone() != null) map.put("phone", user.getPhone());
        if (user.getProfileImageUrl() != null) map.put("profileImageUrl", user.getProfileImageUrl());
        if (user.getSpecialization() != null) map.put("specialization", user.getSpecialization());
        if (user.getExperience() != null) map.put("experience", user.getExperience());
        if (user.getRating() != null) map.put("rating", user.getRating());
        if (user.getTotalCases() != null) map.put("totalCases", user.getTotalCases());
        if (user.getAddress() != null) map.put("address", user.getAddress());
        if (user.getFcmToken() != null) map.put("fcmToken", user.getFcmToken());
        
        return map;
    }

    public static User documentToUser(DocumentSnapshot document) {
        if (!document.exists()) return null;
        
        User user = new User();
        user.setUserId(document.getId());
        user.setFullName(document.getString("fullName"));
        user.setEmail(document.getString("email"));
        user.setUserType(document.getString("userType"));
        user.setPhone(document.getString("phone"));
        user.setProfileImageUrl(document.getString("profileImageUrl"));
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
        user.setAddress(document.getString("address"));
        user.setFcmToken(document.getString("fcmToken"));
        if (document.getTimestamp("createdAt") != null) {
            user.setCreatedAt(document.getTimestamp("createdAt"));
        }
        
        return user;
    }

    public static Task<Void> updateFcmToken(String userId, String fcmToken) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", fcmToken);
        updates.put("updatedAt", Timestamp.now());
        
        return db.collection(COLLECTION_NAME)
                .document(userId)
                .update(updates);
    }
}
