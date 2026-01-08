package com.example.lawway;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.etEmailL);
        password = findViewById(R.id.etPasswordL);
        loginBtn = findViewById(R.id.btnlogin);

        loginBtn.setOnClickListener(v -> {
            auth.signInWithEmailAndPassword(
                    email.getText().toString().trim(),
                    password.getText().toString().trim()
            ).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(this, DashboardActivity.class));
                } else {
                    Toast.makeText(this, "Login Failed",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
