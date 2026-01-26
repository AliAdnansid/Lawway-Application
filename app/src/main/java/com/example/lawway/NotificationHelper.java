package com.example.lawway;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationHelper {
    private static final String CHANNEL_ID = "lawway_notifications";
    private static final String CHANNEL_NAME = "Lawway Notifications";
    private static final int NOTIFICATION_ID_REQUEST = 1001;
    private static final int NOTIFICATION_ID_ACCEPTED = 1002;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for requests and case updates");
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showRequestNotification(Context context, String title, String message, String requestId, String userType) {
        createNotificationChannel(context);
        
        Intent intent;
        if (userType != null && userType.equals("Lawyer")) {
            intent = new Intent(context, LawyerRequestsActivity.class);
        } else {
            intent = new Intent(context, MeetingRequestsActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("requestId", requestId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_REQUEST, builder.build());
        }
    }

    public static void showAcceptedNotification(Context context, String title, String message, String requestId) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, MeetingRequestsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("requestId", requestId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_ACCEPTED, builder.build());
        }
    }

    public static void sendNotificationToUser(String userId, String title, String message, String requestId, String userType) {
        storeNotificationInFirestore(userId, title, message, requestId, userType);
    }

    private static void storeNotificationInFirestore(String userId, String title, String message, String requestId, String userType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        java.util.Map<String, Object> notificationData = new java.util.HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("requestId", requestId);
        notificationData.put("userType", userType);
        notificationData.put("type", "request");
        notificationData.put("read", false);
        notificationData.put("createdAt", com.google.firebase.Timestamp.now());
        
        db.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
            })
            .addOnFailureListener(e -> {
            });
    }
}
