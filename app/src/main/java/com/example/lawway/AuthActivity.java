package com.example.lawway;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTitle, tvAccountQuestion, tvAccountLink;
    private LinearLayout llAccountTypeToggle, llFullNameContainer;
    private Button btnSignUpToggle, btnLogInToggle;
    private Button btnClientToggle, btnLawyerToggle;
    private Button btnSubmit;
    private EditText etFullName, etEmail, etPassword;
    private ImageButton ibPasswordVisibility;
    private ImageView ivEmailCheck;

    // State variables
    private boolean isSignUpMode = true; // true = Sign Up, false = Log In
    private boolean isClientSelected = true; // true = Client, false = Lawyer
    private boolean isPasswordVisible = false;

    // Firebase
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        initViews();
        setupClickListeners();
        updateUI();

        // Check if user is already logged in
        if (auth.getCurrentUser() != null) {
            // User is already logged in, navigate to main activity
            // startActivity(new Intent(this, MainActivity.class));
            // finish();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvAccountQuestion = findViewById(R.id.tvAccountQuestion);
        tvAccountLink = findViewById(R.id.tvAccountLink);
        llAccountTypeToggle = findViewById(R.id.llAccountTypeToggle);
        llFullNameContainer = findViewById(R.id.llFullNameContainer);
        btnSignUpToggle = findViewById(R.id.btnSignUpToggle);
        btnLogInToggle = findViewById(R.id.btnLogInToggle);
        btnClientToggle = findViewById(R.id.btnClientToggle);
        btnLawyerToggle = findViewById(R.id.btnLawyerToggle);
        btnSubmit = findViewById(R.id.btnSubmit);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ibPasswordVisibility = findViewById(R.id.ibPasswordVisibility);
        ivEmailCheck = findViewById(R.id.ivEmailCheck);
    }

    private void setupClickListeners() {
        // Sign Up / Log In toggle
        btnSignUpToggle.setOnClickListener(v -> {
            isSignUpMode = true;
            updateUI();
        });

        btnLogInToggle.setOnClickListener(v -> {
            isSignUpMode = false;
            updateUI();
        });

        // Client / Lawyer toggle
        btnClientToggle.setOnClickListener(v -> {
            isClientSelected = true;
            updateAccountTypeToggle();
        });

        btnLawyerToggle.setOnClickListener(v -> {
            isClientSelected = false;
            updateAccountTypeToggle();
        });

        // Password visibility toggle
        ibPasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ibPasswordVisibility.setImageResource(R.drawable.ic_eye);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ibPasswordVisibility.setImageResource(R.drawable.ic_eye_off);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Submit button (Sign Up / Log In)
        btnSubmit.setOnClickListener(v -> {
            if (isSignUpMode) {
                handleSignUp();
            } else {
                handleLogIn();
            }
        });

        // Account question link (switch between Sign Up and Log In)
        tvAccountLink.setOnClickListener(v -> {
            isSignUpMode = !isSignUpMode;
            updateUI();
        });

        // Email validation
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    ivEmailCheck.setVisibility(View.VISIBLE);
                } else {
                    ivEmailCheck.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateUI() {
        if (isSignUpMode) {
            // Sign Up mode
            tvTitle.setText(R.string.join_lawway);
            btnSubmit.setText(R.string.create_account);
            tvAccountQuestion.setText(R.string.already_have_account);
            tvAccountLink.setText(R.string.log_in);

            // Show account type toggle and full name field
            llAccountTypeToggle.setVisibility(View.VISIBLE);
            llFullNameContainer.setVisibility(View.VISIBLE);

            // Update toggle buttons
            btnSignUpToggle.setBackgroundResource(R.drawable.bg_toggle_segment);
            btnSignUpToggle.setTextColor(getResources().getColor(R.color.auth_text_primary, null));
            btnSignUpToggle.setTypeface(null, android.graphics.Typeface.BOLD);

            btnLogInToggle.setBackgroundResource(android.R.color.transparent);
            btnLogInToggle.setTextColor(getResources().getColor(R.color.auth_text_secondary, null));
            btnLogInToggle.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            // Log In mode
            tvTitle.setText(R.string.welcome_back);
            btnSubmit.setText(R.string.log_in);
            tvAccountQuestion.setText(R.string.dont_have_account);
            tvAccountLink.setText(R.string.sign_up);

            // Hide account type toggle and full name field
            llAccountTypeToggle.setVisibility(View.GONE);
            llFullNameContainer.setVisibility(View.GONE);

            // Update toggle buttons
            btnLogInToggle.setBackgroundResource(R.drawable.bg_toggle_segment);
            btnLogInToggle.setTextColor(getResources().getColor(R.color.auth_text_primary, null));
            btnLogInToggle.setTypeface(null, android.graphics.Typeface.BOLD);

            btnSignUpToggle.setBackgroundResource(android.R.color.transparent);
            btnSignUpToggle.setTextColor(getResources().getColor(R.color.auth_text_secondary, null));
            btnSignUpToggle.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void updateAccountTypeToggle() {
        if (isClientSelected) {
            btnClientToggle.setBackgroundResource(R.drawable.bg_toggle_segment);
            btnClientToggle.setTextColor(getResources().getColor(R.color.auth_text_primary, null));
            btnClientToggle.setTypeface(null, android.graphics.Typeface.BOLD);

            btnLawyerToggle.setBackgroundResource(android.R.color.transparent);
            btnLawyerToggle.setTextColor(getResources().getColor(R.color.auth_text_secondary, null));
            btnLawyerToggle.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            btnLawyerToggle.setBackgroundResource(R.drawable.bg_toggle_segment);
            btnLawyerToggle.setTextColor(getResources().getColor(R.color.auth_text_primary, null));
            btnLawyerToggle.setTypeface(null, android.graphics.Typeface.BOLD);

            btnClientToggle.setBackgroundResource(android.R.color.transparent);
            btnClientToggle.setTextColor(getResources().getColor(R.color.auth_text_secondary, null));
            btnClientToggle.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void handleSignUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSubmit.setEnabled(false);

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Store user data in Firestore using UserHelper
                            String userType = isClientSelected ? "Client" : "Lawyer";
                            User userObj = new User(user.getUid(), fullName, email, userType);
                            
                            UserHelper.createUserWithId(user.getUid(), userObj)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        // Navigate to main activity
                                        // startActivity(new Intent(this, MainActivity.class));
                                        // finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        btnSubmit.setEnabled(true);
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                    }
                });
    }

    private void handleLogIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSubmit.setEnabled(false);

        // Sign in with Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        // Navigate to main activity
                        // startActivity(new Intent(this, MainActivity.class));
                        // finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                    }
                });
    }
}
