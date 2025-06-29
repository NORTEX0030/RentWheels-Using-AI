package com.example.enhanceddynamicpricing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleModelAdapter extends RecyclerView.Adapter<VehicleModelAdapter.VehicleModelViewHolder> {

    private List<VehicleModel> vehicleModels;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(VehicleModel vehicleModel);
    }

    public VehicleModelAdapter(List<VehicleModel> vehicleModels) {
        this.vehicleModels = vehicleModels != null ? vehicleModels : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateModels(List<VehicleModel> newModels) {
        this.vehicleModels = newModels != null ? newModels : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_model, parent, false);
        return new VehicleModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleModelViewHolder holder, int position) {
        VehicleModel model = vehicleModels.get(position);
        holder.modelNameTextView.setText(model.getName());
        holder.priceTextView.setText("Price: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(model.getFinalPrice()) + "/day");
        holder.vehicleImageView.setImageResource(model.getImageResId());
        holder.fuelTypeTextView.setText("Fuel Type: " + model.getFuelType());
        holder.availabilityTextView.setText("Availability: " + (model.isAvailable() ? "Available" : "Not Available"));
        holder.availabilityTextView.setTextColor(model.isAvailable() ? 0xFF4CAF50 : 0xFFFF0000); // Green if available, red if not

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleModels.size();
    }

    static class VehicleModelViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImageView;
        TextView modelNameTextView;
        TextView fuelTypeTextView;
        TextView availabilityTextView;
        TextView priceTextView;

        VehicleModelViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleImageView = itemView.findViewById(R.id.vehicleImageView);
            modelNameTextView = itemView.findViewById(R.id.modelNameTextView);
            fuelTypeTextView = itemView.findViewById(R.id.fuelTypeTextView);
            availabilityTextView = itemView.findViewById(R.id.availabilityTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }
    }
}