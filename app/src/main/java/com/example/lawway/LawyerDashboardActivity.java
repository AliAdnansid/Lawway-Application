package com.example.lawway;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LawyerDashboardActivity extends AppCompatActivity {

    private ImageView ivProfile, ivSearch, ivNotification;
    private TextView tvLawyerName, tvMonthYear, tvTodayDate;
    private LinearLayout llCalendarGrid, llEvents;
    private Calendar currentCalendar;
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat todayFormat;
    private FirebaseAuth auth;
    private ListenerRegistration requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyer_dashboard);

        auth = FirebaseAuth.getInstance();
        currentCalendar = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        todayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        initViews();
        setupClickListeners();
        loadUserData();
        setupCalendar();
        loadTodayEvents();
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
        ivSearch = findViewById(R.id.ivSearch);
        ivNotification = findViewById(R.id.ivNotification);
        tvLawyerName = findViewById(R.id.tvLawyerName);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvTodayDate = findViewById(R.id.tvTodayDate);
        llCalendarGrid = findViewById(R.id.llCalendarGrid);
        llEvents = findViewById(R.id.llEvents);
    }

    private void setupClickListeners() {
        ivSearch.setOnClickListener(v -> {
        });

        ivNotification.setOnClickListener(v -> {
        });

        findViewById(R.id.ivPrevMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            setupCalendar();
        });

        findViewById(R.id.ivNextMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            setupCalendar();
        });

        findViewById(R.id.llNewEvent).setOnClickListener(v -> {
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
                                    tvLawyerName.setText(userObj.getFullName() + ", " + getString(R.string.esq));
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

    private void setupCalendar() {
        tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()));
        
        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        Calendar prevMonth = (Calendar) calendar.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        llCalendarGrid.removeAllViews();
        
        LinearLayout daysHeader = new LinearLayout(this);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(0, 0, 0, 12);
        daysHeader.setLayoutParams(headerParams);
        daysHeader.setOrientation(LinearLayout.HORIZONTAL);
        daysHeader.setWeightSum(7);
        
        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String day : dayNames) {
            TextView dayView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f);
            params.setMargins(2, 0, 2, 0);
            dayView.setLayoutParams(params);
            dayView.setText(day);
            dayView.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
            dayView.setTextSize(12);
            dayView.setTypeface(null, android.graphics.Typeface.BOLD);
            dayView.setGravity(android.view.Gravity.CENTER);
            dayView.setPadding(0, 8, 0, 8);
            daysHeader.addView(dayView);
        }
        llCalendarGrid.addView(daysHeader);
        
        int startDay = firstDayOfWeek - 1;
        LinearLayout weekRow = null;
        
        for (int i = startDay - 1; i >= 0; i--) {
            if (weekRow == null) {
                weekRow = createWeekRow();
            }
            LinearLayout dateContainer = createDateContainer(String.valueOf(daysInPrevMonth - i), true, false, "none");
            weekRow.addView(dateContainer);
        }
        
        Calendar today = Calendar.getInstance();
        boolean isCurrentMonth = currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR);
        
        for (int day = 1; day <= daysInMonth; day++) {
            if (weekRow == null || weekRow.getChildCount() >= 7) {
                if (weekRow != null && weekRow.getChildCount() > 0) {
                    llCalendarGrid.addView(weekRow);
                }
                weekRow = createWeekRow();
            }
            
            boolean isToday = isCurrentMonth && day == today.get(Calendar.DAY_OF_MONTH);
            boolean isSelected = day == 24 && currentCalendar.get(Calendar.MONTH) == Calendar.OCTOBER && 
                                currentCalendar.get(Calendar.YEAR) == 2023;
            
            String eventType = "none";
            if (day == 3 || day == 17 || day == 24) {
                eventType = "meeting";
            } else if (day == 8) {
                eventType = "court";
            }
            
            LinearLayout dateContainer = createDateContainer(String.valueOf(day), false, isSelected, eventType);
            weekRow.addView(dateContainer);
        }
        
        int remainingDays = 7 - weekRow.getChildCount();
        if (remainingDays > 0 && remainingDays < 7) {
            for (int day = 1; day <= remainingDays; day++) {
                LinearLayout dateContainer = createDateContainer(String.valueOf(day), true, false, "none");
                weekRow.addView(dateContainer);
            }
        }
        
        if (weekRow != null && weekRow.getChildCount() > 0) {
            llCalendarGrid.addView(weekRow);
        }
        
        Calendar todayCal = Calendar.getInstance();
        tvTodayDate.setText(getString(R.string.today, todayFormat.format(todayCal.getTime())));
    }
    
    private LinearLayout createWeekRow() {
        LinearLayout weekRow = new LinearLayout(this);
        LinearLayout.LayoutParams weekParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        weekParams.setMargins(0, 0, 0, 4);
        weekRow.setLayoutParams(weekParams);
        weekRow.setOrientation(LinearLayout.HORIZONTAL);
        weekRow.setWeightSum(7);
        weekRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        return weekRow;
    }
    
    private LinearLayout createDateContainer(String text, boolean isInactive, boolean isSelected, String eventType) {
        LinearLayout dateContainer = new LinearLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f);
        containerParams.setMargins(2, 4, 2, 4);
        dateContainer.setLayoutParams(containerParams);
        dateContainer.setOrientation(LinearLayout.VERTICAL);
        dateContainer.setGravity(android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.CENTER_VERTICAL);
        dateContainer.setPadding(0, 4, 0, 4);
        dateContainer.setMinimumHeight((int) (56 * getResources().getDisplayMetrics().density));
        
        TextView dateView = new TextView(this);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        dateParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        dateView.setLayoutParams(dateParams);
        dateView.setText(text);
        dateView.setTextSize(14);
        dateView.setTypeface(null, android.graphics.Typeface.NORMAL);
        dateView.setGravity(android.view.Gravity.CENTER);
        dateView.setPadding(10, 10, 10, 10);
        dateView.setMinWidth((int) (40 * getResources().getDisplayMetrics().density));
        dateView.setMinHeight((int) (40 * getResources().getDisplayMetrics().density));
        
        if (isSelected) {
            dateView.setBackgroundResource(R.drawable.bg_calendar_date_selected);
            dateView.setTextColor(getResources().getColor(R.color.calendar_date_selected_text, null));
        } else if (isInactive) {
            dateView.setBackgroundResource(R.drawable.bg_calendar_date);
            dateView.setTextColor(getResources().getColor(R.color.calendar_date_inactive, null));
        } else {
            dateView.setBackgroundResource(R.drawable.bg_calendar_date);
            dateView.setTextColor(getResources().getColor(R.color.calendar_date_text, null));
        }
        
        dateContainer.addView(dateView);
        
        if (!eventType.equals("none")) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density));
            dotParams.setMargins(0, 2, 0, 0);
            dotParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            dot.setLayoutParams(dotParams);
            dot.setScaleType(ImageView.ScaleType.FIT_CENTER);
            
            if (eventType.equals("court")) {
                dot.setImageResource(R.drawable.ic_event_court);
            } else if (eventType.equals("meeting")) {
                dot.setImageResource(R.drawable.ic_event_meeting);
            } else if (eventType.equals("personal")) {
                dot.setImageResource(R.drawable.ic_event_personal);
            }
            
            dateContainer.addView(dot);
        }
        
        dateContainer.setClickable(true);
        dateContainer.setFocusable(true);
        
        return dateContainer;
    }


    private void loadTodayEvents() {
        llEvents.removeAllViews();
        
        List<EventItem> events = new ArrayList<>();
        events.add(new EventItem("09:00", "Client Consultation", "Civil Litigation • 45m", "John Doe", "meeting", false));
        events.add(new EventItem("11:30", "Court Hearing", "Doe vs. Smith Corp • Courtroom 3B", "", "court", true));
        events.add(new EventItem("13:30", "", "", "", "available", false));
        events.add(new EventItem("15:00", "Contract Review", "Real Estate • 1h", "Mary Wright", "meeting", false));
        
        for (EventItem event : events) {
            View eventView = createEventView(event);
            llEvents.addView(eventView);
        }
    }

    private View createEventView(EventItem event) {
        if (event.type.equals("available")) {
            LinearLayout slotLayout = new LinearLayout(this);
            LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            slotParams.setMargins(0, 0, 0, 12);
            slotLayout.setLayoutParams(slotParams);
            slotLayout.setOrientation(LinearLayout.HORIZONTAL);
            slotLayout.setPadding(16, 16, 16, 16);
            slotLayout.setBackgroundResource(R.drawable.bg_available_slot);
            slotLayout.setGravity(android.view.Gravity.CENTER);
            slotLayout.setClickable(true);
            slotLayout.setFocusable(true);
            
            TextView slotText = new TextView(this);
            slotText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            slotText.setText("+ " + getString(R.string.available_slot));
            slotText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
            slotText.setTextSize(14);
            slotLayout.addView(slotText);
            
            return slotLayout;
        }
        
        LinearLayout eventLayout = new LinearLayout(this);
        LinearLayout.LayoutParams eventParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        eventParams.setMargins(0, 0, 0, 12);
        eventLayout.setLayoutParams(eventParams);
        eventLayout.setOrientation(LinearLayout.HORIZONTAL);
        eventLayout.setPadding(16, 16, 16, 16);
        eventLayout.setBackgroundResource(R.drawable.bg_event_card);
        eventLayout.setClickable(true);
        eventLayout.setFocusable(true);
        
        View colorBar = new View(this);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(4, LinearLayout.LayoutParams.MATCH_PARENT);
        colorBar.setLayoutParams(barParams);
        if (event.type.equals("court")) {
            colorBar.setBackgroundResource(R.drawable.bg_event_bar_court);
        } else {
            colorBar.setBackgroundResource(R.drawable.bg_event_bar_meeting);
        }
        eventLayout.addView(colorBar);
        
        LinearLayout contentLayout = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f);
        contentParams.setMargins(12, 0, 12, 0);
        contentLayout.setLayoutParams(contentParams);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        eventLayout.addView(contentLayout);
        
        TextView timeText = new TextView(this);
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        timeText.setText(event.time);
        timeText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
        timeText.setTextSize(12);
        timeText.setPadding(0, 0, 0, 4);
        contentLayout.addView(timeText);
        
        TextView titleText = new TextView(this);
        titleText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        titleText.setText(event.title);
        titleText.setTextColor(getResources().getColor(R.color.dashboard_text_primary, null));
        titleText.setTextSize(16);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setPadding(0, 0, 0, 4);
        contentLayout.addView(titleText);
        
        TextView detailsText = new TextView(this);
        detailsText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        detailsText.setText(event.details);
        detailsText.setTextColor(getResources().getColor(R.color.dashboard_text_secondary, null));
        detailsText.setTextSize(14);
        detailsText.setPadding(0, 0, 0, 8);
        contentLayout.addView(detailsText);
        
        if (event.hasPriority) {
            TextView priorityTag = new TextView(this);
            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            priorityTag.setLayoutParams(tagParams);
            priorityTag.setText(getString(R.string.high_priority));
            priorityTag.setTextColor(getResources().getColor(R.color.priority_tag_border, null));
            priorityTag.setTextSize(10);
            priorityTag.setTypeface(null, android.graphics.Typeface.BOLD);
            priorityTag.setPadding(6, 2, 6, 2);
            priorityTag.setBackgroundResource(R.drawable.bg_priority_tag);
            contentLayout.addView(priorityTag);
        }
        
        if (!event.participant.isEmpty()) {
            LinearLayout participantLayout = new LinearLayout(this);
            participantLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            participantLayout.setOrientation(LinearLayout.HORIZONTAL);
            participantLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            participantLayout.setPadding(0, 8, 0, 0);
            contentLayout.addView(participantLayout);
            
            ImageView participantAvatar = new ImageView(this);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(24, 24);
            avatarParams.setMargins(0, 0, 8, 0);
            participantAvatar.setLayoutParams(avatarParams);
            participantAvatar.setImageResource(R.drawable.ic_profile_picture);
            participantAvatar.setBackgroundResource(R.drawable.bg_category_circle);
            participantAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            participantLayout.addView(participantAvatar);
            
            TextView participantText = new TextView(this);
            participantText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            participantText.setText(event.participant);
            participantText.setTextColor(getResources().getColor(R.color.dashboard_text_primary, null));
            participantText.setTextSize(14);
            participantLayout.addView(participantText);
        }
        
        ImageView menuIcon = new ImageView(this);
        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        menuIcon.setLayoutParams(menuParams);
        menuIcon.setImageResource(R.drawable.ic_more_vert);
        menuIcon.setPadding(8, 8, 8, 8);
        menuIcon.setClickable(true);
        menuIcon.setFocusable(true);
        eventLayout.addView(menuIcon);
        
        return eventLayout;
    }

    private static class EventItem {
        String time;
        String title;
        String details;
        String participant;
        String type;
        boolean hasPriority;

        EventItem(String time, String title, String details, String participant, String type, boolean hasPriority) {
            this.time = time;
            this.title = title;
            this.details = details;
            this.participant = participant;
            this.type = type;
            this.hasPriority = hasPriority;
        }
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
            .whereEqualTo("lawyerId", user.getUid())
            .whereEqualTo("status", "pending")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    return;
                }
                
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (com.google.firebase.firestore.DocumentChange change : querySnapshot.getDocumentChanges()) {
                        if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            Request request = RequestHelper.documentToRequest(change.getDocument());
                            if (request != null) {
                                checkAndShowNewRequestNotification(request);
                            }
                        }
                    }
                }
            });
    }

    private void checkAndShowNewRequestNotification(Request request) {
        android.content.SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        String key = "new_request_" + request.getRequestId();
        if (!prefs.getBoolean(key, false)) {
            UserHelper.getUserById(request.getClientId())
                .addOnSuccessListener(clientDoc -> {
                    String clientName = "A client";
                    if (clientDoc.exists()) {
                        User client = UserHelper.documentToUser(clientDoc);
                        if (client != null && client.getFullName() != null) {
                            clientName = client.getFullName();
                        }
                    }
                    NotificationHelper.showRequestNotification(
                        this,
                        "New Request Received",
                        clientName + " has sent you a new case request",
                        request.getRequestId(),
                        "Lawyer"
                    );
                    prefs.edit().putBoolean(key, true).apply();
                });
        }
    }
}
