package com.example.lawway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LawyerRequestsActivity extends AppCompatActivity {

    private ImageView ivBack, ivFilter;
    private EditText etSearch;
    private LinearLayout llRequests;
    private List<RequestItem> requestItems;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyer_requests);

        auth = FirebaseAuth.getInstance();
        initViews();
        setupClickListeners();
        loadRequestsFromDatabase();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivFilter = findViewById(R.id.ivFilter);
        etSearch = findViewById(R.id.etSearch);
        llRequests = findViewById(R.id.llRequests);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivFilter.setOnClickListener(v -> {
        });

        findViewById(R.id.llNavDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, LawyerDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.llNavCalendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, LawyerDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.llNavMessages).setOnClickListener(v -> {
        });

        findViewById(R.id.llNavAiAgent).setOnClickListener(v -> {
            Intent intent = new Intent(this, GeminiChatActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.llNavSettings).setOnClickListener(v -> {
        });
    }

    private void loadRequestsFromDatabase() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        llRequests.removeAllViews();
        TextView loadingText = new TextView(this);
        loadingText.setText("Loading requests...");
        loadingText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
        loadingText.setPadding(16, 16, 16, 16);
        llRequests.addView(loadingText);

        RequestHelper.getRequestsByLawyerId(user.getUid())
                .addOnSuccessListener(querySnapshot -> {
                    requestItems = new ArrayList<>();
                    
                    if (querySnapshot.isEmpty()) {
                        llRequests.removeAllViews();
                        TextView emptyText = new TextView(this);
                        emptyText.setText("No requests found");
                        emptyText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
                        emptyText.setPadding(16, 16, 16, 16);
                        llRequests.addView(emptyText);
                        return;
                    }

                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    int[] loadedCount = {0};
                    int totalCount = documents.size();

                    for (DocumentSnapshot doc : documents) {
                        Request request = RequestHelper.documentToRequest(doc);
                        if (request == null) continue;

                        loadRequestDetails(request, () -> {
                            loadedCount[0]++;
                            if (loadedCount[0] == totalCount) {
                                runOnUiThread(() -> {
                                    llRequests.removeAllViews();
                                    displayRequests();
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    llRequests.removeAllViews();
                    TextView errorText = new TextView(this);
                    errorText.setText("Error loading requests: " + e.getMessage());
                    errorText.setTextColor(getResources().getColor(R.color.status_declined, null));
                    errorText.setPadding(16, 16, 16, 16);
                    llRequests.addView(errorText);
                });
    }

    private void loadRequestDetails(Request request, Runnable onComplete) {
        String clientId = request.getClientId();
        String caseId = request.getCaseId();
        
        UserHelper.getUserById(clientId)
                .addOnSuccessListener(clientDoc -> {
                    User client = null;
                    if (clientDoc.exists()) {
                        client = UserHelper.documentToUser(clientDoc);
                    }

                    if (caseId != null && !caseId.isEmpty()) {
                        CaseHelper.getCaseById(caseId)
                                .addOnSuccessListener(caseDoc -> {
                                    Case caseObj = null;
                                    if (caseDoc.exists()) {
                                        caseObj = CaseHelper.documentToCase(caseDoc);
                                    }
                                    createRequestItem(request, client, caseObj);
                                    onComplete.run();
                                })
                                .addOnFailureListener(e -> {
                                    createRequestItem(request, client, null);
                                    onComplete.run();
                                });
                    } else {
                        createRequestItem(request, client, null);
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    createRequestItem(request, null, null);
                    onComplete.run();
                });
    }

    private void createRequestItem(Request request, User client, Case caseObj) {
        String clientName = client != null && client.getFullName() != null 
                ? client.getFullName() 
                : "Unknown Client";
        
        String category = caseObj != null && caseObj.getCategory() != null
                ? caseObj.getCategory().toUpperCase()
                : "GENERAL";
        
        String description = request.getMessage() != null 
                ? (request.getMessage().length() > 100 
                    ? request.getMessage().substring(0, 100) + "..." 
                    : request.getMessage())
                : "No message provided";
        
        String status = request.getStatus() != null 
                ? request.getStatus().toUpperCase() 
                : "PENDING";
        
        if (status.equals("REJECTED")) {
            status = "DECLINED";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateStr = "";
        if (request.getCreatedAt() != null) {
            if (status.equals("ACCEPTED") && request.getRespondedAt() != null) {
                dateStr = "Meeting: " + sdf.format(request.getRespondedAt().toDate());
            } else {
                dateStr = "Sent " + sdf.format(request.getCreatedAt().toDate());
            }
        }
        
        String actionType = "view_details";
        if (status.equals("ACCEPTED")) {
            actionType = "join_chat";
        } else if (status.equals("DECLINED")) {
            actionType = "find_alternative";
        }
        
        String profileImageUrl = client != null && client.getProfileImageUrl() != null
                ? client.getProfileImageUrl()
                : null;
        
        RequestItem item = new RequestItem(
            clientName,
            category,
            description,
            status,
            dateStr,
            actionType,
            profileImageUrl,
            request.getRequestId()
        );
        
        if (requestItems == null) {
            requestItems = new ArrayList<>();
        }
        requestItems.add(item);
    }

    private void displayRequests() {
        llRequests.removeAllViews();

        for (RequestItem item : requestItems) {
            View cardView = createRequestCard(item);
            llRequests.addView(cardView);
        }
    }

    private View createRequestCard(RequestItem item) {
        LinearLayout cardLayout = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16);
        cardLayout.setLayoutParams(cardParams);
        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardLayout.setPadding(16, 16, 16, 16);
        cardLayout.setBackgroundResource(R.drawable.bg_request_card);
        cardLayout.setClickable(true);
        cardLayout.setFocusable(true);

        ImageView avatar = new ImageView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(56, 56);
        avatar.setLayoutParams(avatarParams);
        avatar.setImageResource(R.drawable.ic_profile_picture);
        avatar.setBackgroundResource(R.drawable.bg_category_circle);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        if (item.profileImageUrl != null && !item.profileImageUrl.isEmpty()) {
            loadProfileImage(avatar, item.profileImageUrl);
        }
        
        cardLayout.addView(avatar);

        LinearLayout contentLayout = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f);
        contentParams.setMargins(16, 0, 16, 0);
        contentLayout.setLayoutParams(contentParams);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.addView(contentLayout);

        LinearLayout nameLayout = new LinearLayout(this);
        nameLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);
        nameLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        contentLayout.addView(nameLayout);

        TextView nameText = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f);
        nameText.setLayoutParams(nameParams);
        nameText.setText(item.name);
        nameText.setTextColor(getResources().getColor(R.color.dashboard_text_primary, null));
        nameText.setTextSize(16);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
        nameLayout.addView(nameText);

        TextView statusBadge = new TextView(this);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(8, 0, 0, 0);
        statusBadge.setLayoutParams(statusParams);
        statusBadge.setText(item.status);
        statusBadge.setTextColor(getResources().getColor(android.R.color.white, null));
        statusBadge.setTextSize(10);
        statusBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        statusBadge.setPadding(8, 4, 8, 4);
        
        int statusBgRes;
        if (item.status.equals("PENDING")) {
            statusBgRes = R.drawable.bg_status_pending;
        } else if (item.status.equals("ACCEPTED")) {
            statusBgRes = R.drawable.bg_status_accepted;
        } else {
            statusBgRes = R.drawable.bg_status_declined;
        }
        statusBadge.setBackgroundResource(statusBgRes);
        nameLayout.addView(statusBadge);

        TextView specialtyText = new TextView(this);
        specialtyText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        specialtyText.setText(item.specialty);
        specialtyText.setTextColor(getResources().getColor(R.color.dashboard_primary, null));
        specialtyText.setTextSize(12);
        specialtyText.setTypeface(null, android.graphics.Typeface.BOLD);
        specialtyText.setPadding(0, 4, 0, 0);
        contentLayout.addView(specialtyText);

        TextView descriptionText = new TextView(this);
        descriptionText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        descriptionText.setText(item.description);
        descriptionText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
        descriptionText.setTextSize(14);
        descriptionText.setPadding(0, 8, 0, 8);
        descriptionText.setMaxLines(2);
        descriptionText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        contentLayout.addView(descriptionText);

        if (!item.date.isEmpty()) {
            LinearLayout dateLayout = new LinearLayout(this);
            dateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            dateLayout.setOrientation(LinearLayout.HORIZONTAL);
            dateLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            contentLayout.addView(dateLayout);

            ImageView calendarIcon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(16, 16);
            iconParams.setMargins(0, 0, 4, 0);
            calendarIcon.setLayoutParams(iconParams);
            calendarIcon.setImageResource(R.drawable.ic_calendar);
            dateLayout.addView(calendarIcon);

            TextView dateText = new TextView(this);
            dateText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            dateText.setText(item.date);
            dateText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
            dateText.setTextSize(12);
            dateLayout.addView(dateText);
        }

        if (item.status.equals("DECLINED")) {
            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            infoLayout.setOrientation(LinearLayout.HORIZONTAL);
            infoLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            infoLayout.setPadding(0, 4, 0, 0);
            contentLayout.addView(infoLayout);

            ImageView infoIcon = new ImageView(this);
            LinearLayout.LayoutParams infoIconParams = new LinearLayout.LayoutParams(16, 16);
            infoIconParams.setMargins(0, 0, 4, 0);
            infoIcon.setLayoutParams(infoIconParams);
            infoIcon.setImageResource(R.drawable.ic_info_circle);
            infoLayout.addView(infoIcon);

            TextView infoText = new TextView(this);
            infoText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            infoText.setText(getString(R.string.lawyer_unavailable));
            infoText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
            infoText.setTextSize(12);
            infoLayout.addView(infoText);
        }

        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        actionLayout.setPadding(0, 8, 0, 0);
        contentLayout.addView(actionLayout);

        if (item.actionType.equals("join_chat")) {
            TextView joinButton = new TextView(this);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            joinButton.setLayoutParams(buttonParams);
            joinButton.setText(getString(R.string.join_chat));
            joinButton.setTextColor(getResources().getColor(android.R.color.white, null));
            joinButton.setTextSize(14);
            joinButton.setTypeface(null, android.graphics.Typeface.BOLD);
            joinButton.setPadding(16, 8, 16, 8);
            joinButton.setBackgroundResource(R.drawable.bg_join_chat_button);
            joinButton.setClickable(true);
            joinButton.setFocusable(true);
            actionLayout.addView(joinButton);
        } else if (item.actionType.equals("find_alternative")) {
            TextView findButton = new TextView(this);
            findButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            findButton.setText(getString(R.string.find_alternative));
            findButton.setTextColor(getResources().getColor(R.color.dashboard_primary, null));
            findButton.setTextSize(14);
            findButton.setClickable(true);
            findButton.setFocusable(true);
            actionLayout.addView(findButton);
        } else {
            LinearLayout viewDetailsLayout = new LinearLayout(this);
            viewDetailsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            viewDetailsLayout.setOrientation(LinearLayout.HORIZONTAL);
            viewDetailsLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            viewDetailsLayout.setClickable(true);
            viewDetailsLayout.setFocusable(true);
            actionLayout.addView(viewDetailsLayout);

            TextView viewDetailsText = new TextView(this);
            viewDetailsText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            viewDetailsText.setText(getString(R.string.view_details));
            viewDetailsText.setTextColor(getResources().getColor(R.color.dashboard_primary, null));
            viewDetailsText.setTextSize(14);
            viewDetailsLayout.addView(viewDetailsText);

            ImageView arrowIcon = new ImageView(this);
            LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(16, 16);
            arrowParams.setMargins(4, 0, 0, 0);
            arrowIcon.setLayoutParams(arrowParams);
            arrowIcon.setImageResource(R.drawable.ic_arrow_forward);
            viewDetailsLayout.addView(arrowIcon);
        }

        return cardLayout;
    }

    private void loadProfileImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static class RequestItem {
        String name;
        String specialty;
        String description;
        String status;
        String date;
        String actionType;
        String profileImageUrl;
        String requestId;

        RequestItem(String name, String specialty, String description, String status,
                   String date, String actionType, String profileImageUrl, String requestId) {
            this.name = name;
            this.specialty = specialty;
            this.description = description;
            this.status = status;
            this.date = date;
            this.actionType = actionType;
            this.profileImageUrl = profileImageUrl;
            this.requestId = requestId;
        }
    }
}
