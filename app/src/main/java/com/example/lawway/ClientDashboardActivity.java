package com.example.lawway;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

public class ClientDashboardActivity extends AppCompatActivity {

    private ImageView ivProfile, ivNotification;
    private TextView tvGreeting, tvTagline;
    private EditText etSearch;
    private LinearLayout llMyChats, llRequests, llSaved, llDocuments;
    private LinearLayout llFamily, llCriminal, llCorporate, llRealEstate;
    private LinearLayout llNavHome, llNavSearch, llNavMessages, llNavProfile;
    private RelativeLayout rlAiBanner;

    private FirebaseAuth auth;
    private ListenerRegistration requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        auth = FirebaseAuth.getInstance();
        
        initViews();
        setupClickListeners();
        loadUserData();
        updateGreeting();
        requestNotificationPermission();
        initializeFCMToken();
        setupRequestListener();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestListener != null) {
            requestListener.remove();
        }
    }

    private void initViews() {
        ivProfile = findViewById(R.id.ivProfile);
        ivNotification = findViewById(R.id.ivNotification);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTagline = findViewById(R.id.tvTagline);
        etSearch = findViewById(R.id.etSearch);
        llMyChats = findViewById(R.id.llMyChats);
        llRequests = findViewById(R.id.llRequests);
        llSaved = findViewById(R.id.llSaved);
        llDocuments = findViewById(R.id.llDocuments);
        llFamily = findViewById(R.id.llFamily);
        llCriminal = findViewById(R.id.llCriminal);
        llCorporate = findViewById(R.id.llCorporate);
        llRealEstate = findViewById(R.id.llRealEstate);
        llNavHome = findViewById(R.id.llNavHome);
        llNavSearch = findViewById(R.id.llNavSearch);
        llNavMessages = findViewById(R.id.llNavMessages);
        llNavProfile = findViewById(R.id.llNavProfile);
        rlAiBanner = findViewById(R.id.rlAiBanner);
    }

    private void setupClickListeners() {
        rlAiBanner.setOnClickListener(v -> {
            Intent intent = new Intent(this, GeminiChatActivity.class);
            startActivity(intent);
        });

        llMyChats.setOnClickListener(v -> {
        });

        llRequests.setOnClickListener(v -> {
            Intent intent = new Intent(this, MeetingRequestsActivity.class);
            startActivity(intent);
        });

        llSaved.setOnClickListener(v -> {
        });

        llDocuments.setOnClickListener(v -> {
        });

        llFamily.setOnClickListener(v -> {
        });

        llCriminal.setOnClickListener(v -> {
        });

        llCorporate.setOnClickListener(v -> {
        });

        llRealEstate.setOnClickListener(v -> {
        });

        llNavHome.setOnClickListener(v -> {
        });

        llNavSearch.setOnClickListener(v -> {
        });

        llNavMessages.setOnClickListener(v -> {
        });

        llNavProfile.setOnClickListener(v -> {
        });

        ivNotification.setOnClickListener(v -> {
        });
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserHelper.getUserById(user.getUid())
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User userObj = UserHelper.documentToUser(documentSnapshot);
                            if (userObj != null) {
                                if (userObj.getFullName() != null) {
                                    updateGreetingWithName(userObj.getFullName());
                                }
                                if (userObj.getProfileImageUrl() != null && !userObj.getProfileImageUrl().isEmpty()) {
                                    loadProfileImage(userObj.getProfileImageUrl());
                                }
                            }
                        }
                    });
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            ivProfile.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning, "");
        } else if (hour < 17) {
            greeting = getString(R.string.good_afternoon, "");
        } else {
            greeting = getString(R.string.good_evening, "");
        }
        
        tvGreeting.setText(greeting);
    }

    private void updateGreetingWithName(String name) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning, name);
        } else if (hour < 17) {
            greeting = getString(R.string.good_afternoon, name);
        } else {
            greeting = getString(R.string.good_evening, name);
        }
        
        tvGreeting.setText(greeting);
    }

    private void initializeFCMToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        UserHelper.updateFcmToken(user.getUid(), token);
                    }
                }
            });
    }

    private void setupRequestListener() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        requestListener = db.collection("requests")
            .whereEqualTo("clientId", user.getUid())
            .whereEqualTo("status", "accepted")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    return;
                }
                
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (com.google.firebase.firestore.DocumentChange change : querySnapshot.getDocumentChanges()) {
                        if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED || 
                            change.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                            Request request = RequestHelper.documentToRequest(change.getDocument());
                            if (request != null) {
                                checkAndShowAcceptedNotification(request);
                            }
                        }
                    }
                }
            });
    }

    private void checkAndShowAcceptedNotification(Request request) {
        android.content.SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        String key = "accepted_" + request.getRequestId();
        if (!prefs.getBoolean(key, false)) {
            NotificationHelper.showAcceptedNotification(
                this,
                "Request Accepted",
                "Your case request has been accepted by the lawyer",
                request.getRequestId()
            );
            prefs.edit().putBoolean(key, true).apply();
        }
    }
}
