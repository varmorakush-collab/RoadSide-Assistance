package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHistoryActivity extends AppCompatActivity implements RequestHistoryAdapter.OnRequestActionListener {

    private RecyclerView rvRequests;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private ChipGroup chipGroupFilters;

    private final List<BatteryRequest> allRequests = new ArrayList<>();
    private final List<BatteryRequest> filteredRequests = new ArrayList<>();
    private RequestHistoryAdapter adapter;
    private String currentFilterStatus = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvRequests = findViewById(R.id.rvRequests);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestHistoryAdapter(filteredRequests, this);
        rvRequests.setAdapter(adapter);

        setupFilters();
        loadUserRequests();
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilterStatus = "all";
            } else if (checkedId == R.id.chipPending) {
                currentFilterStatus = "pending";
            } else if (checkedId == R.id.chipCompleted) {
                currentFilterStatus = "completed";
            } else if (checkedId == R.id.chipCancelled) {
                currentFilterStatus = "cancelled";
            }
            filterRequests(currentFilterStatus);
        });
    }

    private void loadUserRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Please log in to view request history");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = currentUser.getUid();

        FirebaseDatabase.getInstance().getReference().child("battery_requests")
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        allRequests.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot reqSnap : snapshot.getChildren()) {
                                BatteryRequest req = reqSnap.getValue(BatteryRequest.class);
                                if (req != null) {
                                    allRequests.add(req);
                                }
                            }
                        }

                        // Sort by latest first
                        Collections.sort(allRequests, (r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));

                        // Default filter: All
                        filterRequests(currentFilterStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Failed to load request history");
                    }
                });
    }

    private void filterRequests(String status) {
        filteredRequests.clear();
        if ("all".equalsIgnoreCase(status)) {
            filteredRequests.addAll(allRequests);
        } else {
            for (BatteryRequest req : allRequests) {
                if (req.getStatus() != null && req.getStatus().equalsIgnoreCase(status)) {
                    filteredRequests.add(req);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredRequests.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No " + (status.equals("all") ? "" : status) + " requests found");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(BatteryRequest request, int position) {
        if (request == null || request.getRequestId() == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_request, null);
        AutoCompleteTextView actvEditVehicle = dialogView.findViewById(R.id.actvEditVehicle);
        TextInputEditText etEditIssue = dialogView.findViewById(R.id.etEditIssue);
        TextInputEditText etEditLocation = dialogView.findViewById(R.id.etEditLocation);

        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        actvEditVehicle.setAdapter(arrayAdapter);

        if (request.getVehicleType() != null) actvEditVehicle.setText(request.getVehicleType(), false);
        if (request.getIssue() != null) etEditIssue.setText(request.getIssue());
        if (request.getLocation() != null) etEditLocation.setText(request.getLocation());

        new AlertDialog.Builder(this)
                .setTitle("Edit Battery Request")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newVehicle = actvEditVehicle.getText() != null ? actvEditVehicle.getText().toString().trim() : "";
                    String newIssue = etEditIssue.getText() != null ? etEditIssue.getText().toString().trim() : "";
                    String newLocation = etEditLocation.getText() != null ? etEditLocation.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(newVehicle) || TextUtils.isEmpty(newIssue) || TextUtils.isEmpty(newLocation)) {
                        Toast.makeText(RequestHistoryActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("vehicleType", newVehicle);
                    updateMap.put("issue", newIssue);
                    updateMap.put("location", newLocation);

                    FirebaseDatabase.getInstance().getReference().child("battery_requests")
                            .child(request.getRequestId())
                            .updateChildren(updateMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RequestHistoryActivity.this, "Request updated successfully", Toast.LENGTH_SHORT).show();
                                    request.setVehicleType(newVehicle);
                                    request.setIssue(newIssue);
                                    request.setLocation(newLocation);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(RequestHistoryActivity.this, "Failed to update request", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onCancel(BatteryRequest request, int position) {
        if (request == null || request.getRequestId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel this battery help request?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference().child("battery_requests")
                            .child(request.getRequestId())
                            .child("status")
                            .setValue("cancelled")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RequestHistoryActivity.this, "Request cancelled successfully", Toast.LENGTH_SHORT).show();
                                    request.setStatus("cancelled");
                                    filterRequests(currentFilterStatus);
                                } else {
                                    Toast.makeText(RequestHistoryActivity.this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onFeedback(BatteryRequest request, int position) {
        if (request == null || request.getRequestId() == null) return;
        Intent intent = new Intent(RequestHistoryActivity.this, FeedbackActivity.class);
        intent.putExtra("requestId", request.getRequestId());
        startActivity(intent);
    }

    @Override
    public void onMarkCompleted(BatteryRequest request, int position) {
        if (request == null || request.getRequestId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Debug: Mark Completed")
                .setMessage("Do you want to mark this request as Completed to test the feedback feature?")
                .setPositiveButton("Mark Completed", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference().child("battery_requests")
                            .child(request.getRequestId())
                            .child("status")
                            .setValue("completed")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RequestHistoryActivity.this, "Request marked as completed!", Toast.LENGTH_SHORT).show();
                                    request.setStatus("completed");
                                    filterRequests(currentFilterStatus);
                                } else {
                                    Toast.makeText(RequestHistoryActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
