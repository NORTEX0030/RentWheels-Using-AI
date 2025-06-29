package com.example.enhanceddynamicpricing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingsFragment extends Fragment {

    private RecyclerView toPayRecyclerView;
    private RecyclerView placedOrdersRecyclerView;
    private BookingAdapter toPayAdapter;
    private BookingAdapter placedOrdersAdapter;
    private Button continueBookingButton;
    private List<Booking> toPayBookings = new ArrayList<>();
    private List<Booking> placedBookings = new ArrayList<>();

    private TextView toPayTitle;
    private TextView placedOrdersTitle;
    private LinearLayout toPayContent;
    private LinearLayout placedOrdersContent;
    private TextView toPayEmptyText;
    private TextView placedOrdersEmptyText;

    private TextInputEditText fromDateEditText;
    private TextInputEditText toDateEditText;
    private Calendar fromDateCalendar;
    private Calendar toDateCalendar;

    private ActivityResultLauncher<Intent> paymentLauncher;
    private boolean shouldShowPlacedOrders = false; // Flag to track if we should show Placed Orders

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BookingsFragment", "onCreate called");
        paymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("showPlacedOrders", false)) {
                            // Set the flag instead of calling loadBookings() directly
                            shouldShowPlacedOrders = true;
                            // If the view is already created, we can handle it immediately
                            if (getView() != null) {
                                loadBookings(getView());
                                showPlacedOrdersSection();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("BookingsFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        // Initialize UI elements with null checks
        Log.d("BookingsFragment", "Initializing views");
        toPayTitle = view.findViewById(R.id.toPayTitle);
        if (toPayTitle == null) Log.e("BookingsFragment", "toPayTitle is null");

        placedOrdersTitle = view.findViewById(R.id.placedOrdersTitle);
        if (placedOrdersTitle == null) Log.e("BookingsFragment", "placedOrdersTitle is null");

        toPayContent = view.findViewById(R.id.toPayContent);
        if (toPayContent == null) Log.e("BookingsFragment", "toPayContent is null");

        placedOrdersContent = view.findViewById(R.id.placedOrdersContent);
        if (placedOrdersContent == null) Log.e("BookingsFragment", "placedOrdersContent is null");

        toPayRecyclerView = view.findViewById(R.id.toPayRecyclerView);
        if (toPayRecyclerView == null) Log.e("BookingsFragment", "toPayRecyclerView is null");

        placedOrdersRecyclerView = view.findViewById(R.id.placedOrdersRecyclerView);
        if (placedOrdersRecyclerView == null) Log.e("BookingsFragment", "placedOrdersRecyclerView is null");

        continueBookingButton = view.findViewById(R.id.continueBookingButton);
        if (continueBookingButton == null) Log.e("BookingsFragment", "continueBookingButton is null");

        toPayEmptyText = view.findViewById(R.id.toPayEmptyText);
        if (toPayEmptyText == null) Log.e("BookingsFragment", "toPayEmptyText is null");

        placedOrdersEmptyText = view.findViewById(R.id.toPayEmptyText1);
        if (placedOrdersEmptyText == null) Log.e("BookingsFragment", "placedOrdersEmptyText is null");

        // Set up RecyclerViews
        Log.d("BookingsFragment", "Setting up RecyclerViews");
        if (toPayRecyclerView != null) {
            toPayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            toPayAdapter = new BookingAdapter();
            toPayRecyclerView.setAdapter(toPayAdapter);
        }

        if (placedOrdersRecyclerView != null) {
            placedOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            placedOrdersAdapter = new BookingAdapter();
            placedOrdersRecyclerView.setAdapter(placedOrdersAdapter);
        }

        // Add animations with null checks
        if (toPayTitle != null) {
            toPayTitle.setAlpha(0f);
            toPayTitle.setTranslationY(20f);
            toPayTitle.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start();
        }

        if (placedOrdersTitle != null) {
            placedOrdersTitle.setAlpha(0f);
            placedOrdersTitle.setTranslationY(20f);
            placedOrdersTitle.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(400).start();
        }

        if (continueBookingButton != null) {
            continueBookingButton.setScaleX(0f);
            continueBookingButton.setScaleY(0f);
            continueBookingButton.animate().scaleX(1f).scaleY(1f).setDuration(300).setStartDelay(600).start();
        }

        // Set click listeners for tabs
        Log.d("BookingsFragment", "Setting click listeners for tabs");
        if (toPayTitle != null) {
            toPayTitle.setOnClickListener(v -> showToPaySection());
        }
        if (placedOrdersTitle != null) {
            placedOrdersTitle.setOnClickListener(v -> showPlacedOrdersSection());
        }

        // Set up Continue Booking button with animation
        Log.d("BookingsFragment", "Setting up Continue Booking button");
        if (continueBookingButton != null) {
            continueBookingButton.setOnClickListener(v -> {
                if (continueBookingButton != null) {
                    continueBookingButton.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .withEndAction(() -> continueBookingButton.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start())
                            .start();
                }

                if (!toPayBookings.isEmpty()) {
                    showBookingDetailsDialog();
                } else {
                    Toast.makeText(getContext(), "No bookings to pay", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Log.d("BookingsFragment", "onCreateView completed");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("BookingsFragment", "onViewCreated called");

        // Load bookings and set initial state
        Log.d("BookingsFragment", "Loading bookings");
        loadBookings(view);
        if (shouldShowPlacedOrders) {
            showPlacedOrdersSection();
            shouldShowPlacedOrders = false; // Reset the flag
        } else {
            showToPaySection();
        }
    }

    private void showToPaySection() {
        Log.d("BookingsFragment", "Showing To Pay section");
        if (toPayTitle != null) toPayTitle.animate().alpha(1f).setDuration(200).start();
        if (placedOrdersTitle != null) placedOrdersTitle.animate().alpha(0.6f).setDuration(200).start();
        if (toPayContent != null) toPayContent.setVisibility(View.VISIBLE);
        if (placedOrdersContent != null) placedOrdersContent.setVisibility(View.GONE);
        if (toPayEmptyText != null) toPayEmptyText.setVisibility(toPayBookings.isEmpty() ? View.VISIBLE : View.GONE);
        if (toPayRecyclerView != null) toPayRecyclerView.setVisibility(toPayBookings.isEmpty() ? View.GONE : View.VISIBLE);
        if (continueBookingButton != null) continueBookingButton.setVisibility(toPayBookings.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showPlacedOrdersSection() {
        Log.d("BookingsFragment", "Showing Placed Orders section");
        if (toPayTitle != null) toPayTitle.animate().alpha(0.6f).setDuration(200).start();
        if (placedOrdersTitle != null) placedOrdersTitle.animate().alpha(1f).setDuration(200).start();
        if (toPayContent != null) toPayContent.setVisibility(View.GONE);
        if (placedOrdersContent != null) placedOrdersContent.setVisibility(View.VISIBLE);
        if (placedOrdersEmptyText != null) placedOrdersEmptyText.setVisibility(placedBookings.isEmpty() ? View.VISIBLE : View.GONE);
        if (placedOrdersRecyclerView != null) placedOrdersRecyclerView.setVisibility(placedBookings.isEmpty() ? View.GONE : View.VISIBLE);
        if (continueBookingButton != null) continueBookingButton.setVisibility(View.GONE);
    }

    private void loadBookings(View rootView) {
        Log.d("BookingsFragment", "Starting loadBookings");
        ProgressBar loadingProgressBar = rootView.findViewById(R.id.loadingProgressBar);
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        new Thread(() -> {
            try {
                Log.d("BookingsFragment", "Fetching bookings from database");
                AppDatabase db = AppDatabase.getDatabase(requireContext());
                List<Booking> toPay = db.bookingDao().getToPayBookings("user1");
                List<Booking> placed = db.bookingDao().getPlacedBookings("user1");

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("BookingsFragment", "Updating UI with bookings");
                        toPayBookings = toPay != null ? toPay : new ArrayList<>();
                        placedBookings = placed != null ? placed : new ArrayList<>();
                        if (toPayAdapter != null) toPayAdapter.setBookings(toPayBookings);
                        if (placedOrdersAdapter != null) placedOrdersAdapter.setBookings(placedBookings);

                        if (toPayEmptyText != null) {
                            toPayEmptyText.setVisibility(toPayBookings.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                        if (toPayRecyclerView != null) {
                            toPayRecyclerView.setVisibility(toPayBookings.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                        if (continueBookingButton != null) {
                            continueBookingButton.setVisibility(toPayBookings.isEmpty() ? View.GONE : View.VISIBLE);
                        }

                        if (placedOrdersEmptyText != null) {
                            placedOrdersEmptyText.setVisibility(placedBookings.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                        if (placedOrdersRecyclerView != null) {
                            placedOrdersRecyclerView.setVisibility(placedBookings.isEmpty() ? View.GONE : View.VISIBLE);
                        }

                        if (loadingProgressBar != null) {
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                        Log.d("BookingsFragment", "loadBookings completed");
                    });
                } else {
                    Log.w("BookingsFragment", "Fragment is detached, skipping UI update");
                }
            } catch (Exception e) {
                Log.e("BookingsFragment", "Error loading bookings", e);
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading bookings", Toast.LENGTH_SHORT).show();
                        if (loadingProgressBar != null) {
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    private void showBookingDetailsDialog() {
        Log.d("BookingsFragment", "Showing booking details dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_booking_details, null);
        builder.setView(dialogView);

        TextInputEditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        TextInputEditText addressEditText = dialogView.findViewById(R.id.addressEditText);
        TextInputEditText mobileEditText = dialogView.findViewById(R.id.mobileEditText);
        fromDateEditText = dialogView.findViewById(R.id.fromDateEditText);
        toDateEditText = dialogView.findViewById(R.id.toDateEditText);
        TextInputEditText timingsEditText = dialogView.findViewById(R.id.timingsEditText);
        Button payButton = dialogView.findViewById(R.id.payButton);
        TextView totalAmountTextView = dialogView.findViewById(R.id.totalAmountTextView);

        fromDateCalendar = Calendar.getInstance();
        toDateCalendar = Calendar.getInstance();
        toDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        fromDateEditText.setText(dateFormat.format(fromDateCalendar.getTime()));
        toDateEditText.setText(dateFormat.format(toDateCalendar.getTime()));

        double baseAmount = calculateBaseAmount();
        totalAmountTextView.setText("Base Amount: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(baseAmount));

        fromDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        fromDateCalendar.set(year, month, dayOfMonth);
                        fromDateEditText.setText(dateFormat.format(fromDateCalendar.getTime()));
                        toDateCalendar.setTime(fromDateCalendar.getTime());
                        toDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        toDateEditText.setText(dateFormat.format(toDateCalendar.getTime()));
                        updateTotalAmount(totalAmountTextView);
                    }, fromDateCalendar.get(Calendar.YEAR), fromDateCalendar.get(Calendar.MONTH),
                    fromDateCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        toDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        toDateCalendar.set(year, month, dayOfMonth);
                        toDateEditText.setText(dateFormat.format(toDateCalendar.getTime()));
                        updateTotalAmount(totalAmountTextView);
                    }, toDateCalendar.get(Calendar.YEAR), toDateCalendar.get(Calendar.MONTH),
                    toDateCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(fromDateCalendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1));
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        payButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String mobile = mobileEditText.getText().toString().trim();
            String timings = timingsEditText.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || mobile.isEmpty() || timings.isEmpty() ||
                    fromDateEditText.getText().toString().isEmpty() || toDateEditText.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Booking booking : toPayBookings) {
                booking.setName(name);
                booking.setAddress(address);
                booking.setMobileNumber(mobile);
                booking.setFromDate(fromDateCalendar.getTime());
                booking.setToDate(toDateCalendar.getTime());
                booking.setTimings(timings);
            }

            Intent intent = new Intent(requireContext(), PaymentActivity.class);
            intent.putExtra("totalAmount", baseAmount);
            intent.putExtra("toPayBookings", new ArrayList<>(toPayBookings));
            paymentLauncher.launch(intent);

            dialog.dismiss();
        });

        dialog.show();
    }

    private double calculateBaseAmount() {
        double total = 0;
        long days = 1;
        if (fromDateCalendar != null && toDateCalendar != null) {
            long diff = toDateCalendar.getTimeInMillis() - fromDateCalendar.getTimeInMillis();
            days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days < 1) days = 1;
            Log.d("BookingsFragment", "Date difference: " + diff + "ms, Days: " + days);
        } else {
            Log.d("BookingsFragment", "Date calendars are null");
        }
        for (Booking booking : toPayBookings) {
            total += booking.getPricePerDay() * days;
        }
        Log.d("BookingsFragment", "Total base amount: " + total + " for " + days + " days");
        return total;
    }

    private void updateTotalAmount(TextView totalAmountTextView) {
        double baseAmount = calculateBaseAmount();
        totalAmountTextView.setText("Base Amount: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(baseAmount));
    }
}