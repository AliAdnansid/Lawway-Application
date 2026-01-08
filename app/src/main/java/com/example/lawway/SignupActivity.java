package com.example.lawway;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    EditText email, password;
    Button signupBtn;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.etEmailS);
        password = findViewById(R.id.etPasswordS);
        signupBtn = findViewById(R.id.btnSignup);

        signupBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}

