package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnSignup;
    private ProgressBar progressBar;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupTextWatchers();

        btnSignup.setOnClickListener(v -> handleSignup());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilFullName.setError(null);
                tilEmail.setError(null);
                tilPhone.setError(null);
                tilPassword.setError(null);
                tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFullName.addTextChangedListener(clearErrorWatcher);
        etEmail.addTextChangedListener(clearErrorWatcher);
        etPhone.addTextChangedListener(clearErrorWatcher);
        etPassword.addTextChangedListener(clearErrorWatcher);
        etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void handleSignup() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Clear existing errors
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean isValid = true;
        View focusView = null;

        // Condition 1: Full Name Validation
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_empty_name));
            focusView = etFullName;
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError(getString(R.string.error_short_name));
            if (focusView == null) focusView = etFullName;
            isValid = false;
        }

        // Condition 2: Email Validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            if (focusView == null) focusView = etEmail;
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            if (focusView == null) focusView = etEmail;
            isValid = false;
        }

        // Condition 3: Phone Number Validation (10 digits)
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_empty_phone));
            if (focusView == null) focusView = etPhone;
            isValid = false;
        } else if (phone.length() != 10 || !phone.matches("\\d{10}")) {
            tilPhone.setError(getString(R.string.error_invalid_phone));
            if (focusView == null) focusView = etPhone;
            isValid = false;
        }

        // Condition 4: Password Validation (at least 6 characters)
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            if (focusView == null) focusView = etPassword;
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            if (focusView == null) focusView = etPassword;
            isValid = false;
        }

        // Condition 5: Confirm Password Validation (Must match Password)
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_empty_confirm_password));
            if (focusView == null) focusView = etConfirmPassword;
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            if (focusView == null) focusView = etConfirmPassword;
            isValid = false;
        }

        if (!isValid) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        // All validations passed -> Register user with Firebase Auth
        registerUserWithFirebase(fullName, email, phone, password);
    }

    private void registerUserWithFirebase(String fullName, String email, String phone, String password) {
        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            saveUserToDatabase(userId, fullName, email, phone);
                        } else {
                            showLoading(false);
                            Toast.makeText(SignupActivity.this, R.string.msg_signup_success, Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        showLoading(false);
                        Exception e = task.getException();
                        String errorMsg = e != null ? e.getMessage() : "Signup failed. Please try again.";
                        Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String fullName, String email, String phone) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("fullName", fullName);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("createdAt", System.currentTimeMillis());

        mDatabase.child("users").child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    Toast.makeText(SignupActivity.this, R.string.msg_signup_success, Toast.LENGTH_SHORT).show();
                    navigateToMain();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignup.setEnabled(false);
            btnSignup.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSignup.setEnabled(true);
            btnSignup.setAlpha(1.0f);
        }
    }
}