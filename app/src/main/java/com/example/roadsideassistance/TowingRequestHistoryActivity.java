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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TowingRequestHistoryActivity extends AppCompatActivity implements RequestHistoryAdapter.OnRequestActionListener {

    private RecyclerView rvTowingHistory;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private ChipGroup chipGroupFilters;

    private RequestHistoryAdapter adapter;
    private final List<BatteryRequest> allRequests = new ArrayList<>();
    private final List<BatteryRequest> filteredRequests = new ArrayList<>();
    private String currentFilterStatus = "all";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_towing_request_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvTowingHistory = findViewById(R.id.rvTowingHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        rvTowingHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestHistoryAdapter(filteredRequests, this);
        rvTowingHistory.setAdapter(adapter);

        setupFilters();
        fetchTowingRequests();
    }

    private void setupFilters() {
        if (chipGroupFilters != null) {
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
    }

    private void fetchTowingRequests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to view history", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        mDatabase.child("towing_requests")
                .orderByChild("userId")
                .equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        allRequests.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                BatteryRequest req = snap.getValue(BatteryRequest.class);
                                if (req != null) {
                                    allRequests.add(req);
                                }
                            }
                        }

                        Collections.sort(allRequests, (r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                        filterRequests(currentFilterStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TowingRequestHistoryActivity.this, "Failed to load history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            tvEmptyState.setText("No " + ("all".equalsIgnoreCase(status) ? "" : status) + " towing requests found");
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
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        actvEditVehicle.setAdapter(vehicleAdapter);

        if (request.getVehicleType() != null) actvEditVehicle.setText(request.getVehicleType(), false);
        if (request.getIssue() != null) etEditIssue.setText(request.getIssue());
        if (request.getLocation() != null) etEditLocation.setText(request.getLocation());

        new AlertDialog.Builder(this)
                .setTitle("Edit Towing Request")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newVehicle = actvEditVehicle.getText() != null ? actvEditVehicle.getText().toString().trim() : "";
                    String newIssue = etEditIssue.getText() != null ? etEditIssue.getText().toString().trim() : "";
                    String newLocation = etEditLocation.getText() != null ? etEditLocation.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(newVehicle) || TextUtils.isEmpty(newIssue) || TextUtils.isEmpty(newLocation)) {
                        Toast.makeText(TowingRequestHistoryActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("vehicleType", newVehicle);
                    updateMap.put("issue", newIssue);
                    updateMap.put("location", newLocation);

                    mDatabase.child("towing_requests")
                            .child(request.getRequestId())
                            .updateChildren(updateMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(TowingRequestHistoryActivity.this, "Towing request updated successfully", Toast.LENGTH_SHORT).show();
                                    request.setVehicleType(newVehicle);
                                    request.setIssue(newIssue);
                                    request.setLocation(newLocation);
                                    filterRequests(currentFilterStatus);
                                } else {
                                    Toast.makeText(TowingRequestHistoryActivity.this, "Failed to update request", Toast.LENGTH_SHORT).show();
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
                .setTitle("Cancel Towing Request")
                .setMessage("Are you sure you want to cancel this towing request?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    mDatabase.child("towing_requests")
                            .child(request.getRequestId())
                            .child("status")
                            .setValue("cancelled")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(TowingRequestHistoryActivity.this, "Towing request cancelled successfully", Toast.LENGTH_SHORT).show();
                                    request.setStatus("cancelled");
                                    filterRequests(currentFilterStatus);
                                } else {
                                    Toast.makeText(TowingRequestHistoryActivity.this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onFeedback(BatteryRequest request, int position) {
        if (request == null || request.getRequestId() == null) return;
        Intent intent = new Intent(TowingRequestHistoryActivity.this, FeedbackActivity.class);
        intent.putExtra("requestId", request.getRequestId());
        startActivity(intent);
    }

    @Override
    public void onMarkCompleted(BatteryRequest request, int position) {
        if (request == null || request.getRequestId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Mark Request as Resolved")
                .setMessage("Are you sure you want to mark this towing request as Resolved / Completed?")
                .setPositiveButton("Yes, Mark Resolved", (dialog, which) -> {
                    mDatabase.child("towing_requests")
                            .child(request.getRequestId())
                            .child("status")
                            .setValue("completed")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(TowingRequestHistoryActivity.this, "Towing request marked as resolved successfully!", Toast.LENGTH_SHORT).show();
                                    request.setStatus("completed");
                                    filterRequests(currentFilterStatus);
                                } else {
                                    Toast.makeText(TowingRequestHistoryActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onContactService(BatteryRequest request, int position) {
        if (request == null) return;
        String techInfo = request.getAssignedTechnician();
        String phoneNumber = "+919876543210"; // Default 24/7 Towing Helpline

        if (techInfo != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\+?\\d{10,12}").matcher(techInfo);
            if (matcher.find()) {
                phoneNumber = matcher.group();
            }
        }

        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            startActivity(dialIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open phone dialer", Toast.LENGTH_SHORT).show();
        }
    }
}
