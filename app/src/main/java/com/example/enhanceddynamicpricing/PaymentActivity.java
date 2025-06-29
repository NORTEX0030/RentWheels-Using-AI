package com.example.enhanceddynamicpricing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private double baseAmount;
    private double gstAmount;
    private double totalAmount;
    private List<Booking> toPayBookings;
    private static final String TAG = "PaymentActivity";
    private static final String RAZORPAY_KEY_ID = "rzp_test_LvBB1IVbBr06Dz"; // Replace with your Razorpay Key ID
    private static final double GST_RATE = 0.12; // 12% GST

    private TextView baseAmountText;
    private TextView gstAmountText;
    private TextView totalAmountText;
    private ProgressBar paymentProgress;
    private Button completePaymentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        baseAmountText = findViewById(R.id.baseAmountText);
        gstAmountText = findViewById(R.id.gstAmountText);
        totalAmountText = findViewById(R.id.totalAmountText);
        paymentProgress = findViewById(R.id.paymentProgress);
        completePaymentButton = findViewById(R.id.completePaymentButton);

        // Preload Razorpay Checkout
        Checkout.preload(getApplicationContext());

        // Get data from Intent
        baseAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
        toPayBookings = (List<Booking>) getIntent().getSerializableExtra("toPayBookings");

        if (toPayBookings == null || toPayBookings.isEmpty()) {
            Toast.makeText(this, "Error: No bookings to pay", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calculate GST and total amount
        gstAmount = baseAmount * GST_RATE;
        totalAmount = baseAmount + gstAmount;

        // Display the amounts
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        baseAmountText.setText("₹" + numberFormat.format(baseAmount));
        gstAmountText.setText("₹" + numberFormat.format(gstAmount));
        totalAmountText.setText("₹" + numberFormat.format(totalAmount));

        // Automatically start payment
        Toast.makeText(this, "After Successful Payment You Will Receive Confirmation Soon", Toast.LENGTH_LONG).show();
        startPayment(totalAmount);
    }

    private void startPayment(double amount) {
        Checkout checkout = new Checkout();
        checkout.setKeyID(RAZORPAY_KEY_ID);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Rentwheels");
            options.put("description", "Vehicle Booking Payment for " + toPayBookings.size() + " vehicle(s)");
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", (int) (amount * 100)); // Convert to paise

            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            options.put("prefill.email", "user@example.com");
            options.put("prefill.contact", "9876543210");

            Log.d(TAG, "Payment options: " + options.toString());
            checkout.open(this, options);
        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay checkout: " + e.getMessage(), e);
            Toast.makeText(this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        Log.d(TAG, "Payment successful, Payment ID: " + paymentId);
        // Update bookings in the database
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            for (Booking booking : toPayBookings) {
                booking.setStatus("Placed");
                db.bookingDao().update(booking);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Payment Successful! Booking Confirmed.", Toast.LENGTH_SHORT).show();
                // Navigate back to BookingsFragment with a flag to show Placed Orders
                Intent intent = new Intent();
                intent.putExtra("showPlacedOrders", true);
                setResult(RESULT_OK, intent);
                finish();
            });
        }).start();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Log.e(TAG, "Payment failed: Code: " + code + ", Response: " + response);
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
        finish();
    }
}