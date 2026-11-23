package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardActiveRequest;
    private TextView tvRequestDetails;
    private TextView tvAssignedTechnician;

    private String activeRequestId = null;
    private String activeVehicleType = null;
    private String activeIssue = null;
    private String activeLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvUserInfo = findViewById(R.id.tvUserInfo);
        MaterialButton btnRequestBattery = findViewById(R.id.btnRequestBattery);
        MaterialButton btnViewProfile = findViewById(R.id.btnViewProfile);
        MaterialButton btnChangePassword = findViewById(R.id.btnChangePassword);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        cardActiveRequest = findViewById(R.id.cardActiveRequest);
        tvRequestDetails = findViewById(R.id.tvRequestDetails);
        tvAssignedTechnician = findViewById(R.id.tvAssignedTechnician);
        MaterialButton btnEditRequest = findViewById(R.id.btnEditRequest);
        MaterialButton btnCancelRequest = findViewById(R.id.btnCancelRequest);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvUserInfo.setText("Logged in as: " + user.getEmail());
            loadActiveRequest(user.getUid());
        } else {
            tvUserInfo.setText("Welcome to Roadside Assistance!");
        }

        btnRequestBattery.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RequestBatteryActivity.class);
            startActivity(intent);
        });

        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCancelRequest.setOnClickListener(v -> handleCancelRequest());
        btnEditRequest.setOnClickListener(v -> handleEditRequest());
    }

    private void loadActiveRequest(String userId) {
        FirebaseDatabase.getInstance().getReference().child("battery_requests")
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot reqSnap : snapshot.getChildren()) {
                                String status = reqSnap.child("status").getValue(String.class);
                                if ("pending".equalsIgnoreCase(status)) {
                                    activeRequestId = reqSnap.getKey();
                                    activeVehicleType = reqSnap.child("vehicleType").getValue(String.class);
                                    activeIssue = reqSnap.child("issue").getValue(String.class);
                                    activeLocation = reqSnap.child("location").getValue(String.class);
                                    String rawAssignedTech = reqSnap.child("assignedTechnician").getValue(String.class);
                                    String assignedTech = TextUtils.isEmpty(rawAssignedTech) ? "Alex Johnson (Mechanic - 4.8★ - Ph: +91 9876543210)" : rawAssignedTech;

                                    String details = "Vehicle: " + (activeVehicleType != null ? activeVehicleType : "N/A") +
                                            "\nIssue: " + (activeIssue != null ? activeIssue : "N/A") +
                                            "\nLocation: " + (activeLocation != null ? activeLocation : "N/A") +
                                            "\nStatus: Pending";

                                    tvRequestDetails.setText(details);
                                    tvAssignedTechnician.setText("Assigned Technician: " + assignedTech);
                                    cardActiveRequest.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                        }
                        activeRequestId = null;
                        cardActiveRequest.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        activeRequestId = null;
                        cardActiveRequest.setVisibility(View.GONE);
                    }
                });
    }

    private void handleCancelRequest() {
        if (activeRequestId == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel this battery help request?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference().child("battery_requests")
                            .child(activeRequestId)
                            .child("status")
                            .setValue("cancelled")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Request cancelled successfully", Toast.LENGTH_SHORT).show();
                                    cardActiveRequest.setVisibility(View.GONE);
                                    activeRequestId = null;
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void handleEditRequest() {
        if (activeRequestId == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_request, null);
        AutoCompleteTextView actvEditVehicle = dialogView.findViewById(R.id.actvEditVehicle);
        TextInputEditText etEditIssue = dialogView.findViewById(R.id.etEditIssue);
        TextInputEditText etEditLocation = dialogView.findViewById(R.id.etEditLocation);

        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        actvEditVehicle.setAdapter(adapter);

        if (activeVehicleType != null) actvEditVehicle.setText(activeVehicleType, false);
        if (activeIssue != null) etEditIssue.setText(activeIssue);
        if (activeLocation != null) etEditLocation.setText(activeLocation);

        new AlertDialog.Builder(this)
                .setTitle("Edit Battery Request")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newVehicle = actvEditVehicle.getText() != null ? actvEditVehicle.getText().toString().trim() : "";
                    String newIssue = etEditIssue.getText() != null ? etEditIssue.getText().toString().trim() : "";
                    String newLocation = etEditLocation.getText() != null ? etEditLocation.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(newVehicle) || TextUtils.isEmpty(newIssue) || TextUtils.isEmpty(newLocation)) {
                        Toast.makeText(MainActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("vehicleType", newVehicle);
                    updateMap.put("issue", newIssue);
                    updateMap.put("location", newLocation);

                    FirebaseDatabase.getInstance().getReference().child("battery_requests")
                            .child(activeRequestId)
                            .updateChildren(updateMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Request updated successfully", Toast.LENGTH_SHORT).show();
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (currentUser != null) {
                                        loadActiveRequest(currentUser.getUid());
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to update request", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
