package com.example.roadsideassistance;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GarageListActivity extends AppCompatActivity {

    private RecyclerView rvGarages;
    private TextView tvEmptyState;
    private GarageAdapter adapter;
    private final List<GarageModel> garageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_garage_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvGarages = findViewById(R.id.rvGarages);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvGarages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GarageAdapter(garageList);
        rvGarages.setAdapter(adapter);

        loadStaticGarages();
    }

    private void loadStaticGarages() {
        garageList.clear();

        garageList.add(new GarageModel(
                "g1",
                "Apex Auto Garage & Towing Hub",
                "Highway Cross Road, Sector 5, Ahmedabad",
                "1.2 km away",
                "4.9",
                "+919876543210",
                "24/7 Heavy Towing & Repairs"
        ));

        garageList.add(new GarageModel(
                "g2",
                "Metro Express Tow & Service Point",
                "S.G. Highway, Opp. Commerce Plaza, Ahmedabad",
                "2.8 km away",
                "4.8",
                "+919123456780",
                "Quick Towing, Battery & Mechanical"
        ));

        garageList.add(new GarageModel(
                "g3",
                "Citywide 24/7 Roadside Garage",
                "Ring Road, Near Service Station, Ahmedabad",
                "4.1 km away",
                "4.7",
                "+919988776655",
                "Flatbed Towing & On-site Repair"
        ));

        garageList.add(new GarageModel(
                "g4",
                "National Highway Towing Station",
                "Vastral Cross Road, Ring Road, Ahmedabad",
                "5.5 km away",
                "5.0",
                "+919554433221",
                "Heavy Vehicle Towing & Recovery"
        ));

        garageList.add(new GarageModel(
                "g5",
                "Royal Motors & Emergency Towing",
                "Satellite Road, Near Star Mall, Ahmedabad",
                "6.3 km away",
                "4.8",
                "+919887766554",
                "Complete Roadside Assistance"
        ));

        adapter.notifyDataSetChanged();

        if (garageList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}
