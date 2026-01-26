package com.example.lawway;

import android.app.NotificationManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            
            String requestId = null;
            String userType = null;
            if (remoteMessage.getData() != null) {
                requestId = remoteMessage.getData().get("requestId");
                userType = remoteMessage.getData().get("userType");
            }
            
            if (title != null && body != null) {
                if (userType != null && userType.equals("Lawyer")) {
                    NotificationHelper.showRequestNotification(this, title, body, requestId, userType);
                } else {
                    NotificationHelper.showAcceptedNotification(this, title, body, requestId);
                }
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            UserHelper.updateFcmToken(auth.getCurrentUser().getUid(), token);
        }
    }
}
