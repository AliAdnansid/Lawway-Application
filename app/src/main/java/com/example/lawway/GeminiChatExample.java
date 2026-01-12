package com.example.lawway;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Example usage of GeminiHelper in Activities with XML layouts
 * 
 * How to use in your Activity:
 * 1. In your XML layout, add views like TextView, EditText, Button
 * 2. In onCreate, use findViewById() to get references
 * 3. Call askGemini() from button click or anywhere you need
 * 
 * Example XML layout:
 * <TextView android:id="@+id/tvResponse" ... />
 * <EditText android:id="@+id/etQuestion" ... />
 * <Button android:id="@+id/btnAsk" android:onClick="onAskClick" ... />
 */
public class GeminiChatExample extends AppCompatActivity {

    // Example: TextView from XML layout
    // TextView tvResponse;
    // EditText etQuestion;

    /**
     * Simple usage - works with any Activity that has XML layout
     * Just call this method from button click or anywhere
     */
    public void askGemini(String question) {
        GeminiHelper.sendMessage(GeminiConfig.API_KEY, question, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                // Update UI on main thread (required for XML views)
                runOnUiThread(() -> {
                    // Update your TextView from XML layout
                    // tvResponse.setText(response);
                    Toast.makeText(GeminiChatExample.this, "Response received!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                // Handle error on main thread
                runOnUiThread(() -> {
                    // Show error to user
                    Toast.makeText(GeminiChatExample.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Example with legal context (recommended for law app)
     */
    public void askGeminiWithLegalContext(String question) {
        String systemInstruction = "You are a helpful legal assistant. Provide clear and accurate legal information.";
        
        GeminiHelper.sendMessageWithContext(
            GeminiConfig.API_KEY,
            systemInstruction,
            question,
            new GeminiHelper.GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Update your TextView from XML
                        // tvResponse.setText(response);
                        Toast.makeText(GeminiChatExample.this, "Response received!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(GeminiChatExample.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }

    /*
    // Example: How to use in your Activity with XML layout
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // Your XML layout
        
        // Get views from XML
        tvResponse = findViewById(R.id.tvResponse);
        etQuestion = findViewById(R.id.etQuestion);
        Button btnAsk = findViewById(R.id.btnAsk);
        
        // Button click
        btnAsk.setOnClickListener(v -> {
            String question = etQuestion.getText().toString();
            askGeminiWithLegalContext(question);
        });
    }
    */
}
