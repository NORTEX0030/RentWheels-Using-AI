package com.example.enhanceddynamicpricing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VehicleTypeAdapter extends RecyclerView.Adapter<VehicleTypeAdapter.ViewHolder> {
    private List<VehicleType> vehicleTypes;
    private OnVehicleTypeClickListener listener;

    public interface OnVehicleTypeClickListener {
        void onVehicleTypeClick(String vehicleType);
    }

    public VehicleTypeAdapter(List<VehicleType> vehicleTypes, OnVehicleTypeClickListener listener) {
        this.vehicleTypes = vehicleTypes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VehicleType vehicleType = vehicleTypes.get(position);
        holder.vehicleTypeTextView.setText(vehicleType.getName());
        holder.vehicleTypeImageView.setImageResource(vehicleType.getDrawableResId());

        // Highlight selected vehicle type
        if (vehicleType.isSelected()) {
            holder.vehicleTypeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.purple_500));
        } else {
            holder.vehicleTypeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            // Deselect all other types
            for (VehicleType type : vehicleTypes) {
                type.setSelected(false);
            }
            vehicleType.setSelected(true);
            notifyDataSetChanged();
            listener.onVehicleTypeClick(vehicleType.getName());
        });
    }

    @Override
    public int getItemCount() {
        return vehicleTypes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView vehicleTypeTextView;
        ImageView vehicleTypeImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleTypeTextView = itemView.findViewById(R.id.vehicleTypeTextView);
            vehicleTypeImageView = itemView.findViewById(R.id.vehicleTypeImageView);
        }
    }
}