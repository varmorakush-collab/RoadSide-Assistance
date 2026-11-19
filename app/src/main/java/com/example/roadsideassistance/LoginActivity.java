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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextView tvForgotPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvSignUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupTextWatchers();

        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(clearErrorWatcher);
        etPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void handleLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;
        View focusView = null;

        // Condition 1: Email Validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            focusView = etEmail;
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            if (focusView == null) focusView = etEmail;
            isValid = false;
        }

        // Condition 2: Password Validation
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            if (focusView == null) focusView = etPassword;
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            if (focusView == null) focusView = etPassword;
            isValid = false;
        }

        if (!isValid) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        // Perform Login with Firebase
        loginUserWithFirebase(email, password);
    }

    private void loginUserWithFirebase(String email, String password) {
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        checkEmailVerificationAndNavigate(firebaseUser);
                    } else {
                        Exception e = task.getException();
                        String errorMsg = e != null ? e.getMessage() : "Authentication failed. Please check credentials.";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkEmailVerificationAndNavigate(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "Login failed. User not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user.isEmailVerified()) {
            Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            // Email is not verified yet -> Prompt user with Verification Dialog
            new AlertDialog.Builder(this)
                    .setTitle("Email Verification Required")
                    .setMessage(R.string.error_email_not_verified)
                    .setPositiveButton("Resend Email", (dialog, which) -> {
                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, R.string.msg_verification_sent, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Continue Anyway", (dialog, which) -> {
                        Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        }
    }

    private void handleForgotPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address to reset password");
            etEmail.requestFocus();
            return;
        }

        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, R.string.msg_reset_email_sent, Toast.LENGTH_LONG).show();
                    } else {
                        Exception e = task.getException();
                        String errorMsg = e != null ? e.getMessage() : "Failed to send reset email.";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);
        }
    }
}