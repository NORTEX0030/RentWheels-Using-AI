package com.example.enhanceddynamicpricing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;


    public BookingAdapter() {
        this.bookings = new ArrayList<>();
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.vehicleName.setText(booking.getVehicleName());
        holder.vehicleType.setText(booking.getVehicleType());
        holder.price.setText("Price: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(booking.getPricePerDay()) + "/day");
        holder.vehicleImage.setImageResource(booking.getImageResId());
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImage;
        TextView vehicleName;
        TextView vehicleType;
        TextView price;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleImage = itemView.findViewById(R.id.bookingVehicleImage);
            vehicleName = itemView.findViewById(R.id.bookingVehicleName);
            vehicleType = itemView.findViewById(R.id.bookingVehicleType);
            price = itemView.findViewById(R.id.bookingPrice);
        }
    }
}