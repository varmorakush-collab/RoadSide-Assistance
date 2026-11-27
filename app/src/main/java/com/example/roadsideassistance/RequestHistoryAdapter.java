package com.example.roadsideassistance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestHistoryAdapter extends RecyclerView.Adapter<RequestHistoryAdapter.ViewHolder> {

    private final List<BatteryRequest> requestList;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onEdit(BatteryRequest request, int position);
        void onCancel(BatteryRequest request, int position);
        void onFeedback(BatteryRequest request, int position);
        void onMarkCompleted(BatteryRequest request, int position);
    }

    public RequestHistoryAdapter(List<BatteryRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BatteryRequest request = requestList.get(position);
        holder.tvVehicleType.setText("Vehicle: " + (request.getVehicleType() != null ? request.getVehicleType() : "N/A"));
        holder.tvIssue.setText("Issue: " + (request.getIssue() != null ? request.getIssue() : "N/A"));
        holder.tvLocation.setText("Location: " + (request.getLocation() != null ? request.getLocation() : "N/A"));

        String fuel = request.getFuelType();
        String qty = request.getQuantity();
        String estPrice = request.getEstimatedPrice();
        if (fuel != null && !fuel.isEmpty()) {
            holder.tvFuelDetails.setText("Fuel: " + fuel + " (" + qty + ") • Est: " + (estPrice != null ? estPrice : "₹0.00"));
            holder.layoutFuelBox.setVisibility(View.VISIBLE);
        } else {
            holder.layoutFuelBox.setVisibility(View.GONE);
        }

        String tech = request.getAssignedTechnician();
        holder.tvTechnician.setText("Technician: " + (tech != null && !tech.isEmpty() ? tech : "Unassigned"));

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "PENDING";
        holder.tvStatusBadge.setText(status);

        if ("COMPLETED".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatusBadge.setBackgroundResource(R.color.success_green);
        } else if ("CANCELLED".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatusBadge.setBackgroundResource(R.color.error_red);
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_header_gradient);
        }

        if ("PENDING".equalsIgnoreCase(request.getStatus())) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
        } else {
            holder.layoutActionButtons.setVisibility(View.GONE);
        }

        if ("COMPLETED".equalsIgnoreCase(request.getStatus())) {
            holder.btnItemFeedback.setVisibility(View.VISIBLE);
        } else {
            holder.btnItemFeedback.setVisibility(View.GONE);
        }

        if (request.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.tvDate.setText("Date: " + sdf.format(new Date(request.getCreatedAt())));
        } else {
            holder.tvDate.setText("Date: N/A");
        }

        if (holder.btnItemMarkResolved != null) {
            holder.btnItemMarkResolved.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkCompleted(request, holder.getBindingAdapterPosition());
                }
            });
        }

        holder.btnItemEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(request, holder.getBindingAdapterPosition());
            }
        });

        holder.btnItemCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel(request, holder.getBindingAdapterPosition());
            }
        });

        holder.btnItemFeedback.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFeedback(request, holder.getBindingAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if ("PENDING".equalsIgnoreCase(request.getStatus())) {
                if (listener != null) {
                    listener.onMarkCompleted(request, holder.getBindingAdapterPosition());
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleType, tvFuelDetails, tvStatusBadge, tvIssue, tvLocation, tvTechnician, tvDate;
        LinearLayout layoutActionButtons, layoutFuelBox;
        MaterialButton btnItemEdit, btnItemCancel, btnItemFeedback, btnItemMarkResolved;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvFuelDetails = itemView.findViewById(R.id.tvFuelDetails);
            layoutFuelBox = itemView.findViewById(R.id.layoutFuelBox);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvIssue = itemView.findViewById(R.id.tvIssue);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTechnician = itemView.findViewById(R.id.tvTechnician);
            tvDate = itemView.findViewById(R.id.tvDate);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnItemEdit = itemView.findViewById(R.id.btnItemEdit);
            btnItemCancel = itemView.findViewById(R.id.btnItemCancel);
            btnItemFeedback = itemView.findViewById(R.id.btnItemFeedback);
            btnItemMarkResolved = itemView.findViewById(R.id.btnItemMarkResolved);
        }
    }
}
