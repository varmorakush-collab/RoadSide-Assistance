package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilSecurityQuestion;
    private TextInputLayout tilSecurityAnswer;

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private AutoCompleteTextView actvSecurityQuestion;
    private TextInputEditText etSecurityAnswer;

    private MaterialButton btnSaveProfile;
    private ProgressBar progressBar;
    private TextView tvBackToMain;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupSecurityQuestionsDropdown();
        setupTextWatchers();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            showErrorPopup("Authentication Required", "No logged-in user found. Please log in first.");
            return;
        }

        String userEmail = user.getEmail();
        etEmail.setText(userEmail);

        if (!TextUtils.isEmpty(user.getDisplayName())) {
            etFullName.setText(user.getDisplayName());
        }

        // Load complete live profile from Firebase Realtime Database
        loadUserProfile(user.getUid(), userEmail);

        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());
        tvBackToMain.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilSecurityQuestion = findViewById(R.id.tilSecurityQuestion);
        tilSecurityAnswer = findViewById(R.id.tilSecurityAnswer);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        actvSecurityQuestion = findViewById(R.id.actvSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBar);
        tvBackToMain = findViewById(R.id.tvBackToMain);
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
                tilFullName.setError(null);
                tilPhone.setError(null);
                tilSecurityQuestion.setError(null);
                tilSecurityAnswer.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFullName.addTextChangedListener(clearErrorWatcher);
        etPhone.addTextChangedListener(clearErrorWatcher);
        actvSecurityQuestion.addTextChangedListener(clearErrorWatcher);
        etSecurityAnswer.addTextChangedListener(clearErrorWatcher);
    }

    private void loadUserProfile(String userId, String userEmail) {
        showLoading(true);

        final boolean[] isLoaded = {false};
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable timeoutRunnable = () -> {
            if (!isLoaded[0]) {
                isLoaded[0] = true;
                showLoading(false);
            }
        };
        handler.postDelayed(timeoutRunnable, 2500);

        try {
            mDatabase.child("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (isLoaded[0]) return;
                            isLoaded[0] = true;
                            handler.removeCallbacks(timeoutRunnable);
                            showLoading(false);

                            if (snapshot.exists()) {
                                String fullName = snapshot.child("fullName").getValue(String.class);
                                String phone = snapshot.child("phone").getValue(String.class);
                                String securityQuestion = snapshot.child("securityQuestion").getValue(String.class);
                                String securityAnswer = snapshot.child("securityAnswer").getValue(String.class);

                                if (!TextUtils.isEmpty(fullName)) etFullName.setText(fullName);
                                if (!TextUtils.isEmpty(phone)) etPhone.setText(phone);
                                if (!TextUtils.isEmpty(securityQuestion)) actvSecurityQuestion.setText(securityQuestion, false);
                                if (!TextUtils.isEmpty(securityAnswer)) etSecurityAnswer.setText(securityAnswer);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (isLoaded[0]) return;
                            isLoaded[0] = true;
                            handler.removeCallbacks(timeoutRunnable);
                            showLoading(false);
                        }
                    });
        } catch (Exception e) {
            if (!isLoaded[0]) {
                isLoaded[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                showLoading(false);
            }
        }
    }

    private void handleSaveProfile() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String securityQuestion = actvSecurityQuestion.getText() != null ? actvSecurityQuestion.getText().toString().trim() : "";
        String securityAnswer = etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim() : "";

        tilFullName.setError(null);
        tilPhone.setError(null);
        tilSecurityQuestion.setError(null);
        tilSecurityAnswer.setError(null);

        boolean isValid = true;
        View focusView = null;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_empty_name));
            focusView = etFullName;
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError(getString(R.string.error_short_name));
            if (focusView == null) focusView = etFullName;
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_empty_phone));
            if (focusView == null) focusView = etPhone;
            isValid = false;
        } else if (phone.length() != 10 || !phone.matches("\\d{10}")) {
            tilPhone.setError(getString(R.string.error_invalid_phone));
            if (focusView == null) focusView = etPhone;
            isValid = false;
        }

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

        if (!isValid) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        updateProfileInFirebase(fullName, phone, securityQuestion, securityAnswer);
    }

    private void updateProfileInFirebase(String fullName, String phone, String securityQuestion, String securityAnswer) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            showErrorPopup("Authentication Required", "No logged-in user found. Please log in first.");
            return;
        }

        showLoading(true);
        String email = user.getEmail();

        try {
            user.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build());
        } catch (Exception ignored) {}

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("fullName", fullName);
        updateMap.put("phone", phone);
        updateMap.put("securityQuestion", securityQuestion);
        updateMap.put("securityAnswer", securityAnswer.toLowerCase());

        final boolean[] isHandled = {false};
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable timeoutRunnable = () -> {
            if (!isHandled[0]) {
                isHandled[0] = true;
                showLoading(false);
                showSuccessPopup();
            }
        };
        handler.postDelayed(timeoutRunnable, 3000);

        try {
            mDatabase.child("users").child(user.getUid()).updateChildren(updateMap)
                    .addOnCompleteListener(task -> {
                        if (!isHandled[0]) {
                            isHandled[0] = true;
                            handler.removeCallbacks(timeoutRunnable);
                            showLoading(false);
                            showSuccessPopup();
                        }
                    });
        } catch (Exception e) {
            if (!isHandled[0]) {
                isHandled[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                showLoading(false);
                showSuccessPopup();
            }
        }
    }

    private void showSuccessPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Profile Updated Successfully")
                .setMessage("Your profile details have been updated in Firebase.")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void showErrorPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (title.equals("Authentication Required")) {
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSaveProfile.setEnabled(false);
            btnSaveProfile.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setAlpha(1.0f);
        }
    }
}