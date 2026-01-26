package com.example.lawway;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GeminiChatActivity extends AppCompatActivity {

    private ScrollView svChat;
    private LinearLayout llMessages;
    private EditText etMessage;
    private ImageView ivSend, ivBack, ivAdd, ivMic;
    private boolean isFirstMessage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_chat);

        initViews();
        setupClickListeners();
        addWelcomeMessage();
    }

    private void initViews() {
        svChat = findViewById(R.id.svChat);
        llMessages = findViewById(R.id.llMessages);
        etMessage = findViewById(R.id.etMessage);
        ivSend = findViewById(R.id.ivSend);
        ivBack = findViewById(R.id.ivBack);
        ivAdd = findViewById(R.id.ivAdd);
        ivMic = findViewById(R.id.ivMic);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                etMessage.setText("");
            }
        });

        ivAdd.setOnClickListener(v -> {
        });

        ivMic.setOnClickListener(v -> {
        });

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                etMessage.setText("");
                return true;
            }
            return false;
        });
    }

    private void addWelcomeMessage() {
        addDateSeparator();
        
        RelativeLayout messageLayout = new RelativeLayout(this);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        messageLayout.setPadding(0, 0, 0, 16);

        RelativeLayout profileLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams profileParams = new RelativeLayout.LayoutParams(40, 40);
        profileLayout.setId(View.generateViewId());
        profileLayout.setLayoutParams(profileParams);

        ImageView profileImg = new ImageView(this);
        RelativeLayout.LayoutParams profileImgParams = new RelativeLayout.LayoutParams(40, 40);
        profileImg.setId(View.generateViewId());
        profileImg.setLayoutParams(profileImgParams);
        profileImg.setImageResource(R.drawable.ic_lexi_profile);
        profileImg.setBackgroundResource(R.drawable.bg_category_circle);
        profileImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileLayout.addView(profileImg);

        View onlineStatus = new View(this);
        RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(10, 10);
        statusParams.addRule(RelativeLayout.ALIGN_BOTTOM, profileImg.getId());
        statusParams.addRule(RelativeLayout.ALIGN_END, profileImg.getId());
        onlineStatus.setLayoutParams(statusParams);
        onlineStatus.setBackgroundResource(R.drawable.bg_online_indicator);
        profileLayout.addView(onlineStatus);

        RelativeLayout.LayoutParams profileLayoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        profileLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        profileLayout.setLayoutParams(profileLayoutParams);
        messageLayout.addView(profileLayout);

        TextView messageText = new TextView(this);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.END_OF, profileLayout.getId());
        textParams.setMargins(56, 0, 16, 0);
        messageText.setId(View.generateViewId());
        messageText.setLayoutParams(textParams);
        messageText.setText("Hello! I'm your AI legal assistant. I can help explain legal terms, draft simple documents, or find you a lawyer. How can I help today?");
        messageText.setTextColor(getResources().getColor(R.color.chat_bot_text, null));
        messageText.setTextSize(14);
        messageText.setPadding(16, 12, 16, 12);
        messageText.setBackgroundResource(R.drawable.bg_chat_bubble_left);
        messageText.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.75));
        messageLayout.addView(messageText);

        llMessages.addView(messageLayout);
        scrollToBottom();
    }

    private void addDateSeparator() {
        TextView dateSeparator = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, 0, 0, 16);
        dateSeparator.setLayoutParams(params);
        dateSeparator.setText(getString(R.string.today));
        dateSeparator.setTextColor(getResources().getColor(R.color.chat_date_separator, null));
        dateSeparator.setTextSize(12);
        dateSeparator.setTypeface(null, android.graphics.Typeface.NORMAL);
        llMessages.addView(dateSeparator);
    }

    private void sendMessage(String message) {
        addUserMessage(message);

        GeminiHelper.sendMessageWithContext(
            GeminiConfig.API_KEY,
            "You are Lexi, a helpful AI legal assistant. Provide clear and accurate legal information in a friendly and professional manner. Format your responses with proper structure using line breaks.",
            message,
            new GeminiHelper.GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        addBotMessage(response);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(GeminiChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
    }

    private void addUserMessage(String message) {
        RelativeLayout messageLayout = new RelativeLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(48, 0, 0, 16);
        messageLayout.setLayoutParams(layoutParams);
        messageLayout.setGravity(android.view.Gravity.END);

        TextView messageText = new TextView(this);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        messageText.setId(View.generateViewId());
        messageText.setLayoutParams(textParams);
        messageText.setText(message);
        messageText.setTextColor(getResources().getColor(R.color.chat_user_text, null));
        messageText.setTextSize(14);
        messageText.setPadding(16, 12, 16, 12);
        messageText.setBackgroundResource(R.drawable.bg_chat_bubble_right);
        messageText.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.75));
        messageLayout.addView(messageText);

        TextView timestamp = new TextView(this);
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.BELOW, messageText.getId());
        timeParams.addRule(RelativeLayout.ALIGN_END, messageText.getId());
        timeParams.setMargins(0, 4, 0, 0);
        timestamp.setLayoutParams(timeParams);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timestamp.setText(getString(R.string.read_at, sdf.format(Calendar.getInstance().getTime())));
        timestamp.setTextColor(getResources().getColor(R.color.chat_text_secondary, null));
        timestamp.setTextSize(11);
        messageLayout.addView(timestamp);

        llMessages.addView(messageLayout);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        RelativeLayout messageLayout = new RelativeLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 16);
        messageLayout.setLayoutParams(layoutParams);

        RelativeLayout profileLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams profileParams = new RelativeLayout.LayoutParams(40, 40);
        profileLayout.setId(View.generateViewId());
        profileLayout.setLayoutParams(profileParams);

        ImageView profileImg = new ImageView(this);
        RelativeLayout.LayoutParams profileImgParams = new RelativeLayout.LayoutParams(40, 40);
        profileImg.setId(View.generateViewId());
        profileImg.setLayoutParams(profileImgParams);
        profileImg.setImageResource(R.drawable.ic_lexi_profile);
        profileImg.setBackgroundResource(R.drawable.bg_category_circle);
        profileImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileLayout.addView(profileImg);

        RelativeLayout.LayoutParams profileLayoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        profileLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        profileLayout.setLayoutParams(profileLayoutParams);
        messageLayout.addView(profileLayout);

        TextView nameText = new TextView(this);
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        nameParams.addRule(RelativeLayout.END_OF, profileLayout.getId());
        nameParams.setMargins(56, 0, 16, 4);
        nameText.setId(View.generateViewId());
        nameText.setLayoutParams(nameParams);
        nameText.setText(getString(R.string.lexi));
        nameText.setTextColor(getResources().getColor(R.color.chat_text_primary, null));
        nameText.setTextSize(12);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
        messageLayout.addView(nameText);

        TextView messageText = new TextView(this);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.END_OF, profileLayout.getId());
        textParams.setMargins(56, 0, 16, 0);
        textParams.addRule(RelativeLayout.BELOW, nameText.getId());
        messageText.setLayoutParams(textParams);
        messageText.setText(formatMessage(message));
        messageText.setTextColor(getResources().getColor(R.color.chat_bot_text, null));
        messageText.setTextSize(14);
        messageText.setPadding(16, 12, 16, 12);
        messageText.setBackgroundResource(R.drawable.bg_chat_bubble_left);
        messageText.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.75));
        messageText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        messageLayout.addView(messageText);

        llMessages.addView(messageLayout);
        scrollToBottom();
    }

    private Spanned formatMessage(String message) {
        String formatted = message
            .replace("\n\n", "<br><br>")
            .replace("\n", "<br>")
            .replace("**", "<b>").replace("**", "</b>")
            .replace("* ", "• ")
            .replace("•", "&bull;");
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(formatted);
        }
    }

    private void scrollToBottom() {
        svChat.post(() -> svChat.fullScroll(View.FOCUS_DOWN));
    }
}
