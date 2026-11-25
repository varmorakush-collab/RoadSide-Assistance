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
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestFuelActivity extends AppCompatActivity {

    private TextInputLayout tilVehicleType;
    private TextInputLayout tilFuelType;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilLocation;

    private AutoCompleteTextView actvVehicleType;
    private AutoCompleteTextView actvFuelType;
    private TextInputEditText etQuantity;
    private TextInputEditText etLocation;

    private TextView tvEstimatedPrice;
    private MaterialButton btnSubmitRequest;
    private MaterialButton btnRequestAgain;
    private TextView tvBackToMain;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private BatteryRequest lastRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_fuel);

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
        checkForLastOrder();

        btnSubmitRequest.setOnClickListener(v -> handleSubmitRequest());
        btnRequestAgain.setOnClickListener(v -> prefillLastOrder());
        tvBackToMain.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tilVehicleType = findViewById(R.id.tilVehicleType);
        tilFuelType = findViewById(R.id.tilFuelType);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilLocation = findViewById(R.id.tilLocation);

        actvVehicleType = findViewById(R.id.actvVehicleType);
        actvFuelType = findViewById(R.id.actvFuelType);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);

        tvEstimatedPrice = findViewById(R.id.tvEstimatedPrice);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        btnRequestAgain = findViewById(R.id.btnRequestAgain);
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
                tilLocation.setError(null);
                calculateEstimatedPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        actvFuelType.addTextChangedListener(priceWatcher);
        etQuantity.addTextChangedListener(priceWatcher);
        actvVehicleType.addTextChangedListener(priceWatcher);
        etLocation.addTextChangedListener(priceWatcher);
    }

    private void checkForLastOrder() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        mDatabase.child("fuel_requests")
                .orderByChild("userId")
                .equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<BatteryRequest> requests = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                BatteryRequest req = snap.getValue(BatteryRequest.class);
                                if (req != null) {
                                    requests.add(req);
                                }
                            }
                        }
                        if (!requests.isEmpty()) {
                            Collections.sort(requests, (r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                            lastRequest = requests.get(0);
                            btnRequestAgain.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void prefillLastOrder() {
        if (lastRequest == null) return;

        if (lastRequest.getVehicleType() != null) actvVehicleType.setText(lastRequest.getVehicleType(), false);
        if (lastRequest.getFuelType() != null) actvFuelType.setText(lastRequest.getFuelType(), false);
        if (lastRequest.getQuantity() != null) etQuantity.setText(lastRequest.getQuantity());
        if (lastRequest.getLocation() != null) etLocation.setText(lastRequest.getLocation());

        calculateEstimatedPrice();
        Toast.makeText(this, "Last fuel order prefilled successfully!", Toast.LENGTH_SHORT).show();
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
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        tilVehicleType.setError(null);
        tilFuelType.setError(null);
        tilQuantity.setError(null);
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

        storeRequestInFirebase(vehicleType, fuelType, quantity, estimatedPrice, location);
    }

    private void storeRequestInFirebase(String vehicleType, String fuelType, String quantity, String estimatedPrice, String location) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showErrorPopup("Authentication Required", "Please log in again to submit a request.");
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "Unknown";
        String requestId = mDatabase.child("fuel_requests").push().getKey();

        if (requestId == null) {
            requestId = "req_" + System.currentTimeMillis();
        }

        String[] dummyTechnicians = {
                "Alex Johnson (Fuel Delivery - 4.8★ - Ph: +91 9876543210)",
                "Michael Smith (Roadside Tech - 4.9★ - Ph: +91 9123456780)",
                "David Williams (Fuel Specialist - 4.7★ - Ph: +91 9988776655)",
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
        requestMap.put("issue", "Fuel Delivery Request (" + fuelType + ": " + quantity + ")");
        requestMap.put("location", location);
        requestMap.put("status", "pending");
        requestMap.put("assignedTechnician", assignedTechnician);
        requestMap.put("createdAt", System.currentTimeMillis());

        mDatabase.child("fuel_requests").child(requestId).setValue(requestMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSuccessPopup();
                    } else {
                        Exception e = task.getException();
                        String msg = e != null ? e.getMessage() : "Failed to store request.";
                        showErrorPopup("Storage Failed", msg);
                    }
                })
                .addOnFailureListener(e -> showErrorPopup("Storage Failed", "Error: " + e.getMessage()));
    }

    private void showSuccessPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Fuel Request Submitted")
                .setMessage("Request submitted successfully.")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(RequestFuelActivity.this, MainActivity.class);
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
