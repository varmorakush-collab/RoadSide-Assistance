package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RequestBatteryActivity extends AppCompatActivity {

    private TextInputLayout tilVehicleType;
    private TextInputLayout tilFuelType;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilIssue;
    private TextInputLayout tilLocation;

    private AutoCompleteTextView actvVehicleType;
    private AutoCompleteTextView actvFuelType;
    private TextInputEditText etQuantity;
    private TextInputEditText etIssue;
    private TextInputEditText etLocation;

    private TextView tvEstimatedPrice;
    private MaterialButton btnSubmitRequest;
    private ProgressBar progressBar;
    private TextView tvBackToMain;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_battery);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupDropdowns();
        setupTextWatchers();

        btnSubmitRequest.setOnClickListener(v -> handleSubmitRequest());
        tvBackToMain.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tilVehicleType = findViewById(R.id.tilVehicleType);
        tilFuelType = findViewById(R.id.tilFuelType);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilIssue = findViewById(R.id.tilIssue);
        tilLocation = findViewById(R.id.tilLocation);

        actvVehicleType = findViewById(R.id.actvVehicleType);
        actvFuelType = findViewById(R.id.actvFuelType);
        etQuantity = findViewById(R.id.etQuantity);
        etIssue = findViewById(R.id.etIssue);
        etLocation = findViewById(R.id.etLocation);

        tvEstimatedPrice = findViewById(R.id.tvEstimatedPrice);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        progressBar = findViewById(R.id.progressBar);
        tvBackToMain = findViewById(R.id.tvBackToMain);
    }

    private void setupDropdowns() {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        actvVehicleType.setAdapter(vehicleAdapter);

        String[] fuelTypes = getResources().getStringArray(R.array.fuel_types);
        ArrayAdapter<String> fuelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fuelTypes);
        actvFuelType.setAdapter(fuelAdapter);

        actvFuelType.setOnItemClickListener((parent, view, position, id) -> calculateEstimatedPrice());
    }

    private void setupTextWatchers() {
        TextWatcher priceWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilVehicleType.setError(null);
                tilFuelType.setError(null);
                tilQuantity.setError(null);
                tilIssue.setError(null);
                tilLocation.setError(null);
                calculateEstimatedPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        actvFuelType.addTextChangedListener(priceWatcher);
        etQuantity.addTextChangedListener(priceWatcher);
        actvVehicleType.addTextChangedListener(priceWatcher);
        etIssue.addTextChangedListener(priceWatcher);
        etLocation.addTextChangedListener(priceWatcher);
    }

    private void calculateEstimatedPrice() {
        String fuelType = actvFuelType.getText() != null ? actvFuelType.getText().toString().trim() : "";
        String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";

        double ratePerUnit = 0.0;
        if ("Petrol".equalsIgnoreCase(fuelType)) {
            ratePerUnit = 100.0;
        } else if ("Diesel".equalsIgnoreCase(fuelType)) {
            ratePerUnit = 90.0;
        } else if ("EV Charging".equalsIgnoreCase(fuelType)) {
            ratePerUnit = 15.0;
        } else if ("CNG".equalsIgnoreCase(fuelType)) {
            ratePerUnit = 80.0;
        }

        double quantity = 0.0;
        try {
            if (!TextUtils.isEmpty(qtyStr)) {
                quantity = Double.parseDouble(qtyStr);
            }
        } catch (NumberFormatException e) {
            quantity = 0.0;
        }

        double totalPrice = ratePerUnit * quantity;
        tvEstimatedPrice.setText(String.format(Locale.getDefault(), "₹%.2f", totalPrice));
    }

    private void handleSubmitRequest() {
        String vehicleType = actvVehicleType.getText() != null ? actvVehicleType.getText().toString().trim() : "";
        String fuelType = actvFuelType.getText() != null ? actvFuelType.getText().toString().trim() : "";
        String quantity = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String estimatedPrice = tvEstimatedPrice.getText() != null ? tvEstimatedPrice.getText().toString().trim() : "₹0.00";
        String issue = etIssue.getText() != null ? etIssue.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        tilVehicleType.setError(null);
        tilFuelType.setError(null);
        tilQuantity.setError(null);
        tilIssue.setError(null);
        tilLocation.setError(null);

        boolean isValid = true;
        View focusView = null;

        if (TextUtils.isEmpty(vehicleType)) {
            tilVehicleType.setError("Please select a vehicle type");
            focusView = actvVehicleType;
            isValid = false;
        }

        if (TextUtils.isEmpty(fuelType)) {
            tilFuelType.setError("Please select a fuel type");
            if (focusView == null) focusView = actvFuelType;
            isValid = false;
        }

        if (TextUtils.isEmpty(quantity)) {
            tilQuantity.setError("Please enter quantity");
            if (focusView == null) focusView = etQuantity;
            isValid = false;
        }

        if (TextUtils.isEmpty(issue)) {
            tilIssue.setError("Please describe the issue");
            if (focusView == null) focusView = etIssue;
            isValid = false;
        } else if (issue.length() < 3) {
            tilIssue.setError("Issue description is too short");
            if (focusView == null) focusView = etIssue;
            isValid = false;
        }

        if (TextUtils.isEmpty(location)) {
            tilLocation.setError("Current location is required");
            if (focusView == null) focusView = etLocation;
            isValid = false;
        } else if (location.length() < 3) {
            tilLocation.setError("Location address is too short");
            if (focusView == null) focusView = etLocation;
            isValid = false;
        }

        if (!isValid) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        storeRequestInFirebase(vehicleType, fuelType, quantity, estimatedPrice, issue, location);
    }

    private void storeRequestInFirebase(String vehicleType, String fuelType, String quantity, String estimatedPrice, String issue, String location) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showErrorPopup("Authentication Required", "Please log in again to submit a request.");
            return;
        }

        showLoading(true);

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "Unknown";
        String requestId = mDatabase.child("battery_requests").push().getKey();

        if (requestId == null) {
            requestId = "req_" + System.currentTimeMillis();
        }

        String[] dummyTechnicians = {
                "Alex Johnson (Mechanic - 4.8★ - Ph: +91 9876543210)",
                "Michael Smith (Roadside Tech - 4.9★ - Ph: +91 9123456780)",
                "David Williams (Battery Specialist - 4.7★ - Ph: +91 9988776655)",
                "Robert Brown (Senior Technician - 5.0★ - Ph: +91 9554433221)"
        };
        int randomIndex = (int) (Math.random() * dummyTechnicians.length);
        String assignedTechnician = dummyTechnicians[randomIndex];

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", requestId);
        requestMap.put("userId", userId);
        requestMap.put("userEmail", userEmail);
        requestMap.put("vehicleType", vehicleType);
        requestMap.put("fuelType", fuelType);
        requestMap.put("quantity", quantity);
        requestMap.put("estimatedPrice", estimatedPrice);
        requestMap.put("issue", issue);
        requestMap.put("location", location);
        requestMap.put("status", "pending");
        requestMap.put("assignedTechnician", assignedTechnician);
        requestMap.put("createdAt", System.currentTimeMillis());

        mDatabase.child("battery_requests").child(requestId).setValue(requestMap)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        showSuccessPopup();
                    } else {
                        Exception e = task.getException();
                        String msg = e != null ? e.getMessage() : "Failed to store request.";
                        showErrorPopup("Storage Failed", msg);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showErrorPopup("Storage Failed", "Error: " + e.getMessage());
                });
    }

    private void showSuccessPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Request Submitted")
                .setMessage("Request stored in Firebase successfully with timestamp and status.")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(RequestBatteryActivity.this, MainActivity.class);
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
            btnSubmitRequest.setEnabled(false);
            btnSubmitRequest.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmitRequest.setEnabled(true);
            btnSubmitRequest.setAlpha(1.0f);
        }
    }
}
