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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RequestTowingActivity extends AppCompatActivity {

    private TextInputLayout tilVehicleType;
    private TextInputLayout tilIssue;
    private TextInputLayout tilLocation;

    private AutoCompleteTextView actvVehicleType;
    private TextInputEditText etIssue;
    private TextInputEditText etLocation;

    private MaterialButton btnSubmitRequest;
    private TextView tvBackToMain;

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

        initViews();
        setupVehicleTypeDropdown();
        setupTextWatchers();

        btnSubmitRequest.setOnClickListener(v -> handleSubmitRequest());
        tvBackToMain.setOnClickListener(v -> finish());
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
                tilVehicleType.setError(null);
                tilIssue.setError(null);
                tilLocation.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        actvVehicleType.addTextChangedListener(clearErrorWatcher);
        etIssue.addTextChangedListener(clearErrorWatcher);
        etLocation.addTextChangedListener(clearErrorWatcher);
    }

    private void handleSubmitRequest() {
        String vehicleType = actvVehicleType.getText() != null ? actvVehicleType.getText().toString().trim() : "";
        String issue = etIssue.getText() != null ? etIssue.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        tilVehicleType.setError(null);
        tilIssue.setError(null);
        tilLocation.setError(null);

        boolean isValid = true;
        View focusView = null;

        if (TextUtils.isEmpty(vehicleType)) {
            tilVehicleType.setError("Please select a vehicle type");
            focusView = actvVehicleType;
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
            tilLocation.setError("Current location/destination is required");
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

        // Static success popup without storing data in Firebase yet
        showSuccessPopup();
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
}
