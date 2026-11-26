package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

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
        MaterialButton btnRequestFuel = findViewById(R.id.btnRequestFuel);
        MaterialButton btnRequestTowing = findViewById(R.id.btnRequestTowing);
        View cardRequestBattery = findViewById(R.id.cardRequestBattery);
        View cardRequestFuel = findViewById(R.id.cardRequestFuel);
        View cardRequestTowing = findViewById(R.id.cardRequestTowing);

        MaterialButton btnNearbyGarages = findViewById(R.id.btnNearbyGarages);
        View cardNearbyGarages = findViewById(R.id.cardNearbyGarages);

        MaterialButton btnBatteryHistory = findViewById(R.id.btnBatteryHistory);
        MaterialButton btnFuelHistory = findViewById(R.id.btnFuelHistory);
        MaterialButton btnTowingHistory = findViewById(R.id.btnTowingHistory);
        View cardBatteryHistory = findViewById(R.id.cardBatteryHistory);
        View cardFuelHistory = findViewById(R.id.cardFuelHistory);
        View cardTowingHistory = findViewById(R.id.cardTowingHistory);

        MaterialButton btnViewProfile = findViewById(R.id.btnViewProfile);
        MaterialButton btnChangePassword = findViewById(R.id.btnChangePassword);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvUserInfo.setText("Logged in as: " + user.getEmail());
        } else {
            tvUserInfo.setText("Welcome to Roadside Assistance!");
        }

        View.OnClickListener batteryListener = v -> {
            Intent intent = new Intent(MainActivity.this, RequestBatteryActivity.class);
            startActivity(intent);
        };
        btnRequestBattery.setOnClickListener(batteryListener);
        if (cardRequestBattery != null) cardRequestBattery.setOnClickListener(batteryListener);

        View.OnClickListener fuelListener = v -> {
            Intent intent = new Intent(MainActivity.this, RequestFuelActivity.class);
            startActivity(intent);
        };
        btnRequestFuel.setOnClickListener(fuelListener);
        if (cardRequestFuel != null) cardRequestFuel.setOnClickListener(fuelListener);

        View.OnClickListener towingListener = v -> {
            Intent intent = new Intent(MainActivity.this, RequestTowingActivity.class);
            startActivity(intent);
        };
        btnRequestTowing.setOnClickListener(towingListener);
        if (cardRequestTowing != null) cardRequestTowing.setOnClickListener(towingListener);

        View.OnClickListener garagesListener = v -> {
            Intent intent = new Intent(MainActivity.this, GarageListActivity.class);
            startActivity(intent);
        };
        if (btnNearbyGarages != null) btnNearbyGarages.setOnClickListener(garagesListener);
        if (cardNearbyGarages != null) cardNearbyGarages.setOnClickListener(garagesListener);

        View.OnClickListener batteryHistoryListener = v -> {
            Intent intent = new Intent(MainActivity.this, BatteryRequestHistoryActivity.class);
            startActivity(intent);
        };
        btnBatteryHistory.setOnClickListener(batteryHistoryListener);
        if (cardBatteryHistory != null) cardBatteryHistory.setOnClickListener(batteryHistoryListener);

        View.OnClickListener fuelHistoryListener = v -> {
            Intent intent = new Intent(MainActivity.this, FuelRequestHistoryActivity.class);
            startActivity(intent);
        };
        btnFuelHistory.setOnClickListener(fuelHistoryListener);
        if (cardFuelHistory != null) cardFuelHistory.setOnClickListener(fuelHistoryListener);

        View.OnClickListener towingHistoryListener = v -> {
            Intent intent = new Intent(MainActivity.this, TowingRequestHistoryActivity.class);
            startActivity(intent);
        };
        if (btnTowingHistory != null) btnTowingHistory.setOnClickListener(towingHistoryListener);
        if (cardTowingHistory != null) cardTowingHistory.setOnClickListener(towingHistoryListener);

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
}
