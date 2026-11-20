package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilCurrentPassword;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnUpdatePassword;
    private ProgressBar progressBar;
    private TextView tvBackToMain;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupTextWatchers();

        btnUpdatePassword.setOnClickListener(v -> handleChangePassword());
        tvBackToMain.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        progressBar = findViewById(R.id.progressBar);
        tvBackToMain = findViewById(R.id.tvBackToMain);
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilCurrentPassword.setError(null);
                tilNewPassword.setError(null);
                tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etCurrentPassword.addTextChangedListener(clearErrorWatcher);
        etNewPassword.addTextChangedListener(clearErrorWatcher);
        etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean isValid = true;
        View focusView = null;

        // Condition 1: Current Password Validation
        if (TextUtils.isEmpty(currentPassword)) {
            tilCurrentPassword.setError("Please enter your current password");
            focusView = etCurrentPassword;
            isValid = false;
        }

        // Condition 2: New Password Validation
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("Please enter a new password");
            if (focusView == null) focusView = etNewPassword;
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError("Password must be at least 6 characters long");
            if (focusView == null) focusView = etNewPassword;
            isValid = false;
        } else if (newPassword.equals(currentPassword)) {
            tilNewPassword.setError("New password must be different from current password");
            if (focusView == null) focusView = etNewPassword;
            isValid = false;
        }

        // Condition 3: Confirm Password Validation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your new password");
            if (focusView == null) focusView = etConfirmPassword;
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            if (focusView == null) focusView = etConfirmPassword;
            isValid = false;
        }

        if (!isValid) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        performPasswordChange(currentPassword, newPassword);
    }

    private void performPasswordChange(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            showErrorPopup("Authentication Error", "No logged-in user found. Please log in first.");
            return;
        }

        showLoading(true);

        String email = user.getEmail();

        // Re-authenticate user in Firebase Auth
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    showLoading(false);
                                    if (updateTask.isSuccessful()) {
                                        updateDatabasePasswordAndFinish(user.getUid(), newPassword);
                                    } else {
                                        Exception e = updateTask.getException();
                                        String msg = e != null ? e.getMessage() : "Failed to update password.";
                                        showErrorPopup("Update Failed", msg);
                                    }
                                });
                    } else {
                        showLoading(false);
                        tilCurrentPassword.setError("Current password is incorrect");
                        etCurrentPassword.requestFocus();
                        showErrorPopup("Update Failed", "The current password you entered is incorrect. Please try again.");
                    }
                });
    }

    private void updateDatabasePasswordAndFinish(String uid, String newPassword) {
        if (!TextUtils.isEmpty(uid)) {
            try {
                mDatabase.child("users").child(uid).child("password").setValue(newPassword);
            } catch (Exception ignored) {}
        }
        showSuccessPopup();
    }

    private void showSuccessPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Password Updated Successfully")
                .setMessage("Your password has been changed successfully!")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showErrorPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnUpdatePassword.setEnabled(false);
            btnUpdatePassword.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnUpdatePassword.setEnabled(true);
            btnUpdatePassword.setAlpha(1.0f);
        }
    }
}