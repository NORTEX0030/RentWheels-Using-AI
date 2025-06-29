package com.example.enhanceddynamicpricing;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView invoicesRecyclerView;
    private InvoiceAdapter invoiceAdapter;
    private TextView emptyInvoicesText;
    private LinearLayout invoicesContainer;
    private TextView downloadInvoicesText;
    private TextView profileVerificationText;
    private TextView aboutUsText;
    private TextView termsConditionsText;
    private List<Booking> placedBookings = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserDetailsPrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address"; // New key for address

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize storage permission launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                loadInvoices();
            } else {
                Toast.makeText(getContext(), "Storage permission denied. Cannot download invoices.", Toast.LENGTH_LONG).show();
            }
        });

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI elements
        invoicesRecyclerView = view.findViewById(R.id.invoicesRecyclerView);
        emptyInvoicesText = view.findViewById(R.id.emptyInvoicesText);
        invoicesContainer = view.findViewById(R.id.invoicesContainer);
        downloadInvoicesText = view.findViewById(R.id.downloadInvoicesText);
        profileVerificationText = view.findViewById(R.id.profileVerificationText);
        aboutUsText = view.findViewById(R.id.aboutUsText);
        termsConditionsText = view.findViewById(R.id.termsConditionsText);

        // Set up RecyclerView
        invoiceAdapter = new InvoiceAdapter();
        invoicesRecyclerView.setAdapter(invoiceAdapter);

        // Set up click listeners
        downloadInvoicesText.setOnClickListener(v -> {
            if (invoicesContainer.getVisibility() == View.GONE) {
                checkStoragePermission();
                invoicesContainer.setVisibility(View.VISIBLE);
                downloadInvoicesText.setText("Hide Invoices");
            } else {
                invoicesContainer.setVisibility(View.GONE);
                downloadInvoicesText.setText("Download Invoices");
            }
        });

        profileVerificationText.setOnClickListener(v -> {
            showProfileDetailsDialog();
        });

        aboutUsText.setOnClickListener(v -> {
            showAboutUsDialog();
        });

        termsConditionsText.setOnClickListener(v -> {
            showTermsConditionsDialog();
        });

        return view;
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses MediaStore, no permission needed
            loadInvoices();
        } else {
            // Android 9 and below need WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                loadInvoices();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void loadInvoices() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<Booking> placed = db.bookingDao().getPlacedBookings("user1");

            requireActivity().runOnUiThread(() -> {
                placedBookings = placed;
                invoiceAdapter.setBookings(placedBookings, requireContext());
                if (placedBookings.isEmpty()) {
                    emptyInvoicesText.setVisibility(View.VISIBLE);
                    invoicesRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyInvoicesText.setVisibility(View.GONE);
                    invoicesRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void showProfileDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_face_verification, null);
        builder.setView(dialogView);

        TextView titleText = dialogView.findViewById(R.id.titleText);
        LinearLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        LinearLayout displayLayout = dialogView.findViewById(R.id.displayLayout);
        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        EditText addressInput = dialogView.findViewById(R.id.addressInput); // New address input
        TextView nameText = dialogView.findViewById(R.id.nameText);
        TextView emailText = dialogView.findViewById(R.id.emailText);
        TextView phoneText = dialogView.findViewById(R.id.phoneText);
        TextView addressText = dialogView.findViewById(R.id.addressText); // New address text
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button closeButton = dialogView.findViewById(R.id.closeButton);

        if (titleText != null) {
            titleText.setText("Profile Details");
        }

        AlertDialog dialog = builder.create();

        // Check if user details are already saved
        String savedName = sharedPreferences.getString(KEY_NAME, null);
        String savedEmail = sharedPreferences.getString(KEY_EMAIL, null);
        String savedPhone = sharedPreferences.getString(KEY_PHONE, null);
        String savedAddress = sharedPreferences.getString(KEY_ADDRESS, null); // Retrieve address

        if (savedName != null && savedEmail != null && savedPhone != null && savedAddress != null) {
            // Display saved details
            inputLayout.setVisibility(View.GONE);
            displayLayout.setVisibility(View.VISIBLE);
            nameText.setText("Name: " + savedName);
            emailText.setText("Email: " + savedEmail);
            phoneText.setText("Phone: " + savedPhone);
            addressText.setText("Address: " + savedAddress); // Display address
        } else {
            // Show input fields to enter details
            inputLayout.setVisibility(View.VISIBLE);
            displayLayout.setVisibility(View.GONE);
        }

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                String address = addressInput.getText().toString().trim(); // Get address

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                    // Show error if any field is empty
                    if (name.isEmpty()) nameInput.setError("Name is required");
                    if (email.isEmpty()) emailInput.setError("Email is required");
                    if (phone.isEmpty()) phoneInput.setError("Phone number is required");
                    if (address.isEmpty()) addressInput.setError("Address is required"); // Validate address
                    return;
                }

                // Save details to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_NAME, name);
                editor.putString(KEY_EMAIL, email);
                editor.putString(KEY_PHONE, phone);
                editor.putString(KEY_ADDRESS, address); // Save address
                editor.apply();

                // Update UI to show saved details
                inputLayout.setVisibility(View.GONE);
                displayLayout.setVisibility(View.VISIBLE);
                nameText.setText("Name: " + name);
                emailText.setText("Email: " + email);
                phoneText.setText("Phone: " + phone);
                addressText.setText("Address: " + address); // Show address
            });
        }

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showAboutUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about_us, null);
        builder.setView(dialogView);

        Button closeButton = dialogView.findViewById(R.id.closeButton);
        AlertDialog dialog = builder.create();
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    private void showTermsConditionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_terms_conditions, null);
        builder.setView(dialogView);

        Button closeButton = dialogView.findViewById(R.id.closeButton);
        AlertDialog dialog = builder.create();
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }
}