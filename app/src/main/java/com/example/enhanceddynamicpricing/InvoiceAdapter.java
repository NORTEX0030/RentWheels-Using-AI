package com.example.enhanceddynamicpricing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private Context context;
    private static final double GST_RATE = 0.12; // 12% GST

    public void setBookings(List<Booking> bookings, Context context) {
        this.bookings = bookings;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        // Set vehicle name
        holder.vehicleNameText.setText(booking.getVehicleName());

        // Set rental dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fromDate = booking.getFromDate() != null ? dateFormat.format(booking.getFromDate()) : "N/A";
        String toDate = booking.getToDate() != null ? dateFormat.format(booking.getToDate()) : "N/A";
        holder.rentalDatesText.setText("From: " + fromDate + " - To: " + toDate);

        // Calculate total amount
        long days = 1;
        if (booking.getFromDate() != null && booking.getToDate() != null) {
            long diff = booking.getToDate().getTime() - booking.getFromDate().getTime();
            days = Math.max(1, diff / (1000 * 60 * 60 * 24));
        }
        double baseAmount = booking.getPricePerDay() * days;
        double gstAmount = baseAmount * GST_RATE;
        double totalAmount = baseAmount + gstAmount;
        holder.totalAmountText.setText("Total: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(totalAmount));

        // Set up download button
        holder.downloadInvoiceButton.setOnClickListener(v -> {
            InvoiceGenerator.generateInvoice(context, booking);
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView vehicleNameText;
        TextView rentalDatesText;
        TextView totalAmountText;
        Button downloadInvoiceButton;

        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleNameText = itemView.findViewById(R.id.vehicleNameText);
            rentalDatesText = itemView.findViewById(R.id.rentalDatesText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            downloadInvoiceButton = itemView.findViewById(R.id.downloadInvoiceButton);
        }
    }
}