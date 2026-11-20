package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardActiveRequest;
    private TextView tvRequestDetails;
    private TextView tvAssignedTechnician;

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
                                    String vehicleType = reqSnap.child("vehicleType").getValue(String.class);
                                    String issue = reqSnap.child("issue").getValue(String.class);
                                    String location = reqSnap.child("location").getValue(String.class);
                                    String rawAssignedTech = reqSnap.child("assignedTechnician").getValue(String.class);
                                    String assignedTech = TextUtils.isEmpty(rawAssignedTech) ? "Alex Johnson (Mechanic - 4.8★ - Ph: +91 9876543210)" : rawAssignedTech;

                                    String details = "Vehicle: " + (vehicleType != null ? vehicleType : "N/A") +
                                            "\nIssue: " + (issue != null ? issue : "N/A") +
                                            "\nLocation: " + (location != null ? location : "N/A") +
                                            "\nStatus: Pending";

                                    tvRequestDetails.setText(details);
                                    tvAssignedTechnician.setText("Assigned Technician: " + assignedTech);
                                    cardActiveRequest.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                        }
                        cardActiveRequest.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cardActiveRequest.setVisibility(View.GONE);
                    }
                });
    }
}
