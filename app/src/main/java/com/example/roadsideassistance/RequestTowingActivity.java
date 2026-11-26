package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RequestTowingActivity extends AppCompatActivity {

    private TextInputLayout tilVehicleType;
    private TextInputLayout tilIssue;
    private TextInputLayout tilLocation;

    private AutoCompleteTextView actvVehicleType;
    private TextInputEditText etIssue;
    private TextInputEditText etLocation;

    private MaterialButton btnSubmitRequest;
    private TextView tvBackToMain;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_towing);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupVehicleTypeDropdown();
        setupTextWatchers();

        if (btnSubmitRequest != null) {
            btnSubmitRequest.setOnClickListener(v -> handleSubmitRequest());
        }
        if (tvBackToMain != null) {
            tvBackToMain.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        tilVehicleType = findViewById(R.id.tilVehicleType);
        tilIssue = findViewById(R.id.tilIssue);
        tilLocation = findViewById(R.id.tilLocation);

        actvVehicleType = findViewById(R.id.actvVehicleType);
        etIssue = findViewById(R.id.etIssue);
        etLocation = findViewById(R.id.etLocation);

        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        tvBackToMain = findViewById(R.id.tvBackToMain);
    }

    private void setupVehicleTypeDropdown() {
        if (actvVehicleType == null) return;
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        actvVehicleType.setAdapter(adapter);
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilVehicleType != null) tilVehicleType.setError(null);
                if (tilIssue != null) tilIssue.setError(null);
                if (tilLocation != null) tilLocation.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        if (actvVehicleType != null) actvVehicleType.addTextChangedListener(clearErrorWatcher);
        if (etIssue != null) etIssue.addTextChangedListener(clearErrorWatcher);
        if (etLocation != null) etLocation.addTextChangedListener(clearErrorWatcher);
    }

    private void handleSubmitRequest() {
        String vehicleType = actvVehicleType != null && actvVehicleType.getText() != null ? actvVehicleType.getText().toString().trim() : "";
        String issue = etIssue != null && etIssue.getText() != null ? etIssue.getText().toString().trim() : "";
        String location = etLocation != null && etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        if (tilVehicleType != null) tilVehicleType.setError(null);
        if (tilIssue != null) tilIssue.setError(null);
        if (tilLocation != null) tilLocation.setError(null);

        boolean isValid = true;
        View focusView = null;

        if (TextUtils.isEmpty(vehicleType)) {
            if (tilVehicleType != null) tilVehicleType.setError("Please select a vehicle type");
            focusView = actvVehicleType;
            isValid = false;
        }

        if (TextUtils.isEmpty(issue)) {
            if (tilIssue != null) tilIssue.setError("Please describe the issue");
            if (focusView == null) focusView = etIssue;
            isValid = false;
        } else if (issue.length() < 3) {
            if (tilIssue != null) tilIssue.setError("Issue description is too short");
            if (focusView == null) focusView = etIssue;
            isValid = false;
        }

        if (TextUtils.isEmpty(location)) {
            if (tilLocation != null) tilLocation.setError("Current location/destination is required");
            if (focusView == null) focusView = etLocation;
            isValid = false;
        } else if (location.length() < 3) {
            if (tilLocation != null) tilLocation.setError("Location address is too short");
            if (focusView == null) focusView = etLocation;
            isValid = false;
        }

        if (!isValid) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        storeRequestInFirebase(vehicleType, issue, location);
    }

    private void storeRequestInFirebase(String vehicleType, String issue, String location) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showErrorPopup("Authentication Required", "Please log in again to submit a request.");
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "Unknown";
        String requestId = mDatabase.child("towing_requests").push().getKey();

        if (requestId == null) {
            requestId = "tow_" + System.currentTimeMillis();
        }

        // Simulate Tow Truck Driver / Technician Assignment
        String[] dummyDrivers = {
                "Alex Johnson (Tow Truck #104 - 4.9★ - Ph: +91 9876543210)",
                "Michael Smith (Flatbed Tow Specialist - 4.8★ - Ph: +91 9123456780)",
                "David Williams (Heavy Tow Driver - 5.0★ - Ph: +91 9988776655)",
                "Robert Brown (Emergency Towing Tech - 4.7★ - Ph: +91 9554433221)"
        };
        int randomIndex = (int) (Math.random() * dummyDrivers.length);
        String assignedTechnician = dummyDrivers[randomIndex];

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", requestId);
        requestMap.put("userId", userId);
        requestMap.put("userEmail", userEmail);
        requestMap.put("vehicleType", vehicleType);
        requestMap.put("issue", issue);
        requestMap.put("location", location);
        requestMap.put("status", "pending");
        requestMap.put("assignedTechnician", assignedTechnician);
        requestMap.put("createdAt", System.currentTimeMillis());

        if (btnSubmitRequest != null) {
            btnSubmitRequest.setEnabled(false);
            btnSubmitRequest.setAlpha(0.6f);
        }

        mDatabase.child("towing_requests").child(requestId).setValue(requestMap)
                .addOnCompleteListener(task -> {
                    if (btnSubmitRequest != null) {
                        btnSubmitRequest.setEnabled(true);
                        btnSubmitRequest.setAlpha(1.0f);
                    }
                    if (task.isSuccessful()) {
                        showSuccessPopup();
                    } else {
                        Exception e = task.getException();
                        String msg = e != null ? e.getMessage() : "Failed to store request.";
                        showErrorPopup("Storage Failed", msg);
                    }
                })
                .addOnFailureListener(e -> {
                    if (btnSubmitRequest != null) {
                        btnSubmitRequest.setEnabled(true);
                        btnSubmitRequest.setAlpha(1.0f);
                    }
                    showErrorPopup("Storage Failed", "Error: " + e.getMessage());
                });
    }

    private void showSuccessPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Towing Request Submitted")
                .setMessage("Towing request submitted successfully.")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(RequestTowingActivity.this, MainActivity.class);
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
}
