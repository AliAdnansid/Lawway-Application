package com.example.lawway;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GeminiChatExample extends AppCompatActivity {

    public void askGemini(String question) {
        GeminiHelper.sendMessage(GeminiConfig.API_KEY, question, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(GeminiChatExample.this, "Response received!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GeminiChatExample.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

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
}
