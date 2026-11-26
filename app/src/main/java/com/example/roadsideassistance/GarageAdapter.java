package com.example.roadsideassistance;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GarageAdapter extends RecyclerView.Adapter<GarageAdapter.ViewHolder> {

    private final List<GarageModel> garageList;

    public GarageAdapter(List<GarageModel> garageList) {
        this.garageList = garageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_garage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GarageModel garage = garageList.get(position);
        holder.tvGarageName.setText(garage.getName() != null ? garage.getName() : "N/A");
        holder.tvServiceType.setText(garage.getServiceType() != null ? garage.getServiceType() : "General Repairs & Towing");
        holder.tvAddress.setText("Address: " + (garage.getAddress() != null ? garage.getAddress() : "N/A"));
        holder.tvDistance.setText("Distance: " + (garage.getDistance() != null ? garage.getDistance() : "N/A"));
        holder.tvRating.setText(garage.getRating() != null ? garage.getRating() + " ★" : "4.8 ★");

        holder.btnCallGarage.setOnClickListener(v -> {
            if (garage.getPhone() != null && !garage.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + garage.getPhone()));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return garageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGarageName, tvServiceType, tvAddress, tvDistance, tvRating;
        MaterialButton btnCallGarage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGarageName = itemView.findViewById(R.id.tvGarageName);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnCallGarage = itemView.findViewById(R.id.btnCallGarage);
        }
    }
}
