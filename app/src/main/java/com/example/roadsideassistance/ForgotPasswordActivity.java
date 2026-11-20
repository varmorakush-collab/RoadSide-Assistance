package com.example.roadsideassistance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPasswordActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleGroupMethod;

    private LinearLayout layoutSecurityQuestion;
    private LinearLayout layoutOldPassword;

    private TextInputLayout tilEmail;
    private TextInputLayout tilSecurityQuestion;
    private TextInputLayout tilSecurityAnswer;
    private TextInputLayout tilOldPassword;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etEmail;
    private AutoCompleteTextView actvSecurityQuestion;
    private TextInputEditText etSecurityAnswer;
    private TextInputEditText etOldPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnResetPassword;
    private ProgressBar progressBar;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private boolean isSecurityQuestionMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupMethodToggle();
        setupSecurityQuestionsDropdown();
        setupTextWatchers();

        if (getIntent() != null && getIntent().hasExtra("email")) {
            String passedEmail = getIntent().getStringExtra("email");
            if (!TextUtils.isEmpty(passedEmail)) {
                etEmail.setText(passedEmail);
            }
        }

        btnResetPassword.setOnClickListener(v -> handleResetPassword());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void initViews() {
        toggleGroupMethod = findViewById(R.id.toggleGroupMethod);

        layoutSecurityQuestion = findViewById(R.id.layoutSecurityQuestion);
        layoutOldPassword = findViewById(R.id.layoutOldPassword);

        tilEmail = findViewById(R.id.tilEmail);
        tilSecurityQuestion = findViewById(R.id.tilSecurityQuestion);
        tilSecurityAnswer = findViewById(R.id.tilSecurityAnswer);
        tilOldPassword = findViewById(R.id.tilOldPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etEmail = findViewById(R.id.etEmail);
        actvSecurityQuestion = findViewById(R.id.actvSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupMethodToggle() {
        toggleGroupMethod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                clearErrors();
                if (checkedId == R.id.btnMethodQuestion) {
                    isSecurityQuestionMode = true;
                    layoutSecurityQuestion.setVisibility(View.VISIBLE);
                    layoutOldPassword.setVisibility(View.GONE);
                } else if (checkedId == R.id.btnMethodOldPassword) {
                    isSecurityQuestionMode = false;
                    layoutSecurityQuestion.setVisibility(View.GONE);
                    layoutOldPassword.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupSecurityQuestionsDropdown() {
        String[] questions = getResources().getStringArray(R.array.security_questions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, questions);
        actvSecurityQuestion.setAdapter(adapter);
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(clearErrorWatcher);
        actvSecurityQuestion.addTextChangedListener(clearErrorWatcher);
        etSecurityAnswer.addTextChangedListener(clearErrorWatcher);
        etOldPassword.addTextChangedListener(clearErrorWatcher);
        etNewPassword.addTextChangedListener(clearErrorWatcher);
        etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilSecurityQuestion.setError(null);
        tilSecurityAnswer.setError(null);
        tilOldPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void handleResetPassword() {
        clearErrors();

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        boolean isValid = true;
        View focusView = null;

        // Email validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            focusView = etEmail;
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            if (focusView == null) focusView = etEmail;
            isValid = false;
        }

        // Mode specific validation
        if (isSecurityQuestionMode) {
            String securityQuestion = actvSecurityQuestion.getText() != null ? actvSecurityQuestion.getText().toString().trim() : "";
            String securityAnswer = etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim() : "";

            if (TextUtils.isEmpty(securityQuestion)) {
                tilSecurityQuestion.setError(getString(R.string.error_empty_question));
                if (focusView == null) focusView = actvSecurityQuestion;
                isValid = false;
            }

            if (TextUtils.isEmpty(securityAnswer)) {
                tilSecurityAnswer.setError(getString(R.string.error_empty_answer));
                if (focusView == null) focusView = etSecurityAnswer;
                isValid = false;
            }
        } else {
            String oldPassword = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
            if (TextUtils.isEmpty(oldPassword)) {
                tilOldPassword.setError(getString(R.string.error_empty_old_password));
                if (focusView == null) focusView = etOldPassword;
                isValid = false;
            }
        }

        // New Password validation
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError(getString(R.string.error_empty_password));
            if (focusView == null) focusView = etNewPassword;
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError(getString(R.string.error_short_password));
            if (focusView == null) focusView = etNewPassword;
            isValid = false;
        }

        // Confirm Password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_empty_confirm_password));
            if (focusView == null) focusView = etConfirmPassword;
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
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

        if (isSecurityQuestionMode) {
            String securityQuestion = actvSecurityQuestion.getText().toString().trim();
            String securityAnswer = etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim() : "";
            resetPasswordViaSecurityQuestion(email, securityQuestion, securityAnswer, newPassword);
        } else {
            String oldPassword = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
            resetPasswordViaOldPassword(email, oldPassword, newPassword);
        }
    }

    private void resetPasswordViaSecurityQuestion(String email, String question, String answer, String newPassword) {
        showLoading(true);

        try {
            mDatabase.child("users")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean userFound = false;
                            boolean answerMatched = false;
                            DataSnapshot targetUserSnap = null;

                            if (snapshot.exists()) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String dbEmail = userSnap.child("email").getValue(String.class);
                                    if (dbEmail != null && dbEmail.trim().equalsIgnoreCase(email.trim())) {
                                        userFound = true;
                                        String dbAnswer = userSnap.child("securityAnswer").getValue(String.class);
                                        boolean answerMatches = (dbAnswer != null && dbAnswer.trim().equalsIgnoreCase(answer.trim()))
                                                || (dbAnswer == null && !TextUtils.isEmpty(answer));

                                        if (answerMatches) {
                                            answerMatched = true;
                                            targetUserSnap = userSnap;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (answerMatched && targetUserSnap != null) {
                                String oldPassword = targetUserSnap.child("password").getValue(String.class);
                                targetUserSnap.getRef().child("password").setValue(newPassword)
                                        .addOnCompleteListener(task -> {
                                            if (!TextUtils.isEmpty(oldPassword)) {
                                                mAuth.signInWithEmailAndPassword(email, oldPassword)
                                                        .addOnCompleteListener(signInTask -> {
                                                            if (signInTask.isSuccessful() && mAuth.getCurrentUser() != null) {
                                                                mAuth.getCurrentUser().updatePassword(newPassword)
                                                                        .addOnCompleteListener(updateTask -> {
                                                                            mAuth.signOut();
                                                                            showLoading(false);
                                                                            showSuccessPopup("Password reset successfully! You can now log in with your new password.");
                                                                        });
                                                            } else {
                                                                showLoading(false);
                                                                showSuccessPopup("Password reset successfully! You can now log in with your new password.");
                                                            }
                                                        });
                                            } else {
                                                showLoading(false);
                                                showSuccessPopup("Password reset successfully! You can now log in with your new password.");
                                            }
                                        });
                            } else if (userFound) {
                                showLoading(false);
                                tilSecurityAnswer.setError(getString(R.string.error_wrong_answer));
                                etSecurityAnswer.requestFocus();
                                showErrorPopup("Reset Failed", "The security question or answer you entered is incorrect. Please check your details and try again.");
                            } else {
                                showLoading(false);
                                tilEmail.setError(getString(R.string.error_user_not_found));
                                etEmail.requestFocus();
                                showErrorPopup("Email Not Registered", "No registered account found with email address: " + email + ". Please check your email or sign up first.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showLoading(false);
                            showErrorPopup("Reset Failed", "Database error: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            showLoading(false);
            showErrorPopup("Reset Failed", "An error occurred: " + e.getMessage());
        }
    }

    private void resetPasswordViaOldPassword(String email, String oldPassword, String newPassword) {
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, oldPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        mDatabase.child("users").child(user.getUid()).child("password").setValue(newPassword)
                                                .addOnCompleteListener(dbTask -> {
                                                    mAuth.signOut();
                                                    showLoading(false);
                                                    showSuccessPopup("Password reset successfully! You can now log in with your new password.");
                                                });
                                    });
                        } else {
                            showLoading(false);
                            showSuccessPopup("Password reset successfully! You can now log in with your new password.");
                        }
                    } else {
                        showLoading(false);
                        tilOldPassword.setError(getString(R.string.error_wrong_old_password));
                        etOldPassword.requestFocus();
                        showErrorPopup("Reset Failed", "The old password you entered is incorrect. Please check your credentials and try again.");
                    }
                });
    }

    private void showSuccessPopup(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Password Reset Successful")
                .setMessage(message)
                .setCancelable(false)
                .setIcon(R.drawable.ic_check_circle)
                .setPositiveButton("OK", (dialog, which) -> {
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
            btnResetPassword.setEnabled(false);
            btnResetPassword.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnResetPassword.setEnabled(true);
            btnResetPassword.setAlpha(1.0f);
        }
    }
}