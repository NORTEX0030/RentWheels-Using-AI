package com.example.enhanceddynamicpricing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import androidx.appcompat.widget.SearchView;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private MaterialCardView searchCard; // Added to control the entire search bar card
    private SearchView searchView;
    private RecyclerView vehicleTypeRecyclerView, vehicleModelRecyclerView;
    private TextView vehicleModelLabel;
    private VehicleTypeAdapter vehicleTypeAdapter;
    private VehicleModelAdapter vehicleModelAdapter;
    private List<VehicleType> vehicleTypes;
    private List<VehicleModel> allVehicleModels;
    private List<VehicleModel> filteredVehicleModels;
    private FusedLocationProviderClient fusedLocationClient;
    private double demandFactor = 1.0;
    private String location = "Unknown";
    private String season = "Regular";
    private String timeOfDay = "Regular";
    private String weather = "Clear";
    private double latitude, longitude;
    private RequestQueue requestQueue;
    private List<String> highDemandCities = Arrays.asList("Mumbai", "Mira Bhayandar", "Thane", "Navi Mumbai");
    private boolean isLocationFetched = false;

    // Linear regression coefficients (in INR) - Simulated as if trained
    private static final double INTERCEPT = 2000.0;
    private static final double DEMAND_COEFF = 800.0;
    private static final double LOCATION_COEFF = 400.0;
    private static final double SEASON_COEFF = 600.0;
    private static final double TIME_OF_DAY_COEFF = 200.0;
    private static final double WEATHER_COEFF = 300.0;

    // Vehicle type coefficients (simulated)
    private static final double BIKE_COEFF = 0.0; // Reference category
    private static final double CAR_COEFF = 500.0;
    private static final double XUV_COEFF = 1000.0;
    private static final double PICKUP_TRUCK_COEFF = 800.0;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "Layout inflated successfully");
        } catch (Exception e) {
            Log.e("MainActivity", "Error inflating layout", e);
            Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize UI elements
        searchCard = findViewById(R.id.searchCard); // Initialize the searchCard
        searchView = findViewById(R.id.searchView);
        vehicleTypeRecyclerView = findViewById(R.id.vehicleTypeRecyclerView);
        vehicleModelRecyclerView = findViewById(R.id.vehicleModelRecyclerView);
        vehicleModelLabel = findViewById(R.id.vehicleModelLabel);
        Button fetchLocationButton = findViewById(R.id.fetchLocationButton);
        FloatingActionButton filterButton = findViewById(R.id.filterButton);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Log visibility of key elements
        Log.d("MainActivity", "searchCard visibility: " + (searchCard != null ? searchCard.getVisibility() : "null"));
        Log.d("MainActivity", "searchView visibility: " + (searchView != null ? searchView.getVisibility() : "null"));
        Log.d("MainActivity", "vehicleTypeRecyclerView visibility: " + (vehicleTypeRecyclerView != null ? vehicleTypeRecyclerView.getVisibility() : "null"));
        Log.d("MainActivity", "filterButton visibility: " + (filterButton != null ? filterButton.getVisibility() : "null"));
        Log.d("MainActivity", "bottomNavigation visibility: " + (bottomNavigationView != null ? bottomNavigationView.getVisibility() : "null"));

        // Ensure initial visibility of home screen elements
        if (searchCard != null) searchCard.setVisibility(View.VISIBLE);
        if (findViewById(R.id.vehicleTypeHeader) != null) findViewById(R.id.vehicleTypeHeader).setVisibility(View.VISIBLE);
        if (vehicleTypeRecyclerView != null) vehicleTypeRecyclerView.setVisibility(View.VISIBLE);
        if (filterButton != null) filterButton.setVisibility(View.VISIBLE);
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);

        // Add animations for UI elements
        if (searchCard != null) {
            searchCard.setAlpha(0f);
            searchCard.setScaleX(0.8f);
            searchCard.setScaleY(0.8f);
            searchCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).setStartDelay(200).start();
        }

        if (filterButton != null) {
            filterButton.setAlpha(0f);
            filterButton.setScaleX(0f);
            filterButton.setScaleY(0f);
            filterButton.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).setStartDelay(800).start();
        }

        if (bottomNavigationView != null) {
            bottomNavigationView.setTranslationY(100f);
            bottomNavigationView.setAlpha(0f);
            bottomNavigationView.animate().translationY(0f).alpha(1f).setDuration(600).setStartDelay(400).start();
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Initialize vehicle types
        vehicleTypes = new ArrayList<>();
        vehicleTypes.add(new VehicleType("Car", true, R.drawable.ic_car));
        vehicleTypes.add(new VehicleType("Bike", false, R.drawable.ic_bike));
        vehicleTypes.add(new VehicleType("XUV", false, R.drawable.ic_xuv));
        vehicleTypes.add(new VehicleType("Pickup Truck", false, R.drawable.ic_pickup_truck));

        // Initialize vehicle models
        allVehicleModels = createVehicleModels();
        filteredVehicleModels = new ArrayList<>(allVehicleModels);

        // Set up RecyclerViews
        setupVehicleTypeRecyclerView();
        setupVehicleModelRecyclerView();

        // Set up search functionality
        setupSearchView();

        // Set up filter button
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                filterButton.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction(() -> filterButton.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start())
                        .start();
                showFilterDialog();
            });
        }

        // Set up fetch location button
        if (fetchLocationButton != null) {
            fetchLocationButton.setOnClickListener(v -> {
                fetchLocationButton.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction(() -> fetchLocationButton.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start())
                        .start();
                fetchLocation();
            });
        }

        // Set up bottom navigation
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int position = bottomNavigationView.getMenu().findItem(item.getItemId()).getOrder();
                switch (position) {
                    case 0: // Home
                        // Show the Home screen
                        if (searchCard != null) searchCard.setVisibility(View.VISIBLE);
                        if (findViewById(R.id.vehicleTypeHeader) != null) findViewById(R.id.vehicleTypeHeader).setVisibility(View.VISIBLE);
                        if (vehicleTypeRecyclerView != null) vehicleTypeRecyclerView.setVisibility(View.VISIBLE);
                        if (vehicleModelLabel != null) vehicleModelLabel.setVisibility(isLocationFetched ? View.VISIBLE : View.GONE);
                        if (vehicleModelRecyclerView != null) vehicleModelRecyclerView.setVisibility(isLocationFetched ? View.VISIBLE : View.GONE);
                        if (findViewById(R.id.vehicleModelCard) != null) findViewById(R.id.vehicleModelCard).setVisibility(isLocationFetched ? View.VISIBLE : View.GONE);
                        if (findViewById(R.id.fragmentContainer) != null) findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
                        if (filterButton != null) filterButton.setVisibility(View.VISIBLE);
                        return true;
                    case 1: // Seasonal
                        selectedFragment = new SeasonalFragment();
                        if (searchCard != null) searchCard.setVisibility(View.VISIBLE); // Show search bar for SeasonalFragment
                        if (filterButton != null) filterButton.setVisibility(View.VISIBLE); // Show filter button for SeasonalFragment
                        break;
                    case 2: // Bookings
                        selectedFragment = new BookingsFragment();
                        if (searchCard != null) searchCard.setVisibility(View.GONE); // Hide search bar for BookingsFragment
                        if (filterButton != null) filterButton.setVisibility(View.GONE); // Hide filter button for BookingsFragment
                        break;
                    case 3: // Profile
                        selectedFragment = new ProfileFragment();
                        if (searchCard != null) searchCard.setVisibility(View.GONE); // Hide search bar for ProfileFragment
                        if (filterButton != null) filterButton.setVisibility(View.GONE); // Hide filter button for ProfileFragment
                        break;
                    default:
                        return false;
                }
                // Hide the Home screen views when navigating away
                if (findViewById(R.id.vehicleTypeHeader) != null) findViewById(R.id.vehicleTypeHeader).setVisibility(View.GONE);
                if (vehicleTypeRecyclerView != null) vehicleTypeRecyclerView.setVisibility(View.GONE);
                if (vehicleModelLabel != null) vehicleModelLabel.setVisibility(View.GONE);
                if (vehicleModelRecyclerView != null) vehicleModelRecyclerView.setVisibility(View.GONE);
                if (findViewById(R.id.vehicleModelCard) != null) findViewById(R.id.vehicleModelCard).setVisibility(View.GONE);
                // Show the fragment container
                if (findViewById(R.id.fragmentContainer) != null) findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
                // Replace the fragment in the container with a fade transition
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.fade_in,  // Enter animation
                                android.R.anim.fade_out, // Exit animation
                                android.R.anim.fade_in,  // Pop enter animation
                                android.R.anim.fade_out  // Pop exit animation
                        )
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            });

            // Set default selection to Home (position 0)
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        }

        // Initially filter models by "Car" to populate the RecyclerView
        filterModelsByType("Car");
    }

    private List<VehicleModel> createVehicleModels() {
        List<VehicleModel> models = new ArrayList<>();
        // Cars (4 persons, higher range)
        VehicleModel toyotaCamry = new VehicleModel("Toyota Camry", "Petrol", true, 5000.0, "Car", R.drawable.ic_car, 4, 200);
        toyotaCamry.setDescription("A luxurious sedan with excellent fuel efficiency and comfort for long drives.");
        models.add(toyotaCamry);

        VehicleModel hondaCivic = new VehicleModel("Honda Civic", "Petrol", false, 4800.0, "Car", R.drawable.ic_car, 4, 180);
        hondaCivic.setDescription("A sporty sedan with a sleek design and advanced safety features.");
        models.add(hondaCivic);

        VehicleModel hyundaiCreta = new VehicleModel("Hyundai Creta", "Diesel", true, 5200.0, "Car", R.drawable.ic_car, 4, 200);
        hyundaiCreta.setDescription("A compact SUV with a powerful engine and modern amenities.");
        models.add(hyundaiCreta);

        VehicleModel marutiSwift = new VehicleModel("Maruti Swift", "Petrol", true, 4000.0, "Car", R.drawable.ic_car, 4, 150);
        marutiSwift.setDescription("A popular hatchback known for its agility and fuel efficiency.");
        models.add(marutiSwift);

        // Bikes (2 persons, lower range)
        VehicleModel hondaActiva = new VehicleModel("Honda Activa", "Petrol", true, 200.0, "Bike", R.drawable.ic_bike, 2, 50);
        hondaActiva.setDescription("A reliable scooter for city commuting with excellent mileage.");
        models.add(hondaActiva);

        VehicleModel yamahaR15 = new VehicleModel("Yamaha R15", "Petrol", false, 300.0, "Bike", R.drawable.ic_bike, 2, 80);
        yamahaR15.setDescription("A sporty bike with a powerful engine and aerodynamic design.");
        models.add(yamahaR15);

        VehicleModel bajajPulsar = new VehicleModel("Bajaj Pulsar", "Petrol", true, 250.0, "Bike", R.drawable.ic_bike, 2, 70);
        bajajPulsar.setDescription("A versatile bike known for its performance and durability.");
        models.add(bajajPulsar);

        VehicleModel tvsApache = new VehicleModel("TVS Apache", "Petrol", true, 280.0, "Bike", R.drawable.ic_bike, 2, 60);
        tvsApache.setDescription("A racing-inspired bike with great handling and speed.");
        models.add(tvsApache);

        // XUVs (5 persons, higher range)
        VehicleModel mahindraScorpio = new VehicleModel("Mahindra Scorpio", "Diesel", true, 6000.0, "XUV", R.drawable.ic_xuv, 5, 250);
        mahindraScorpio.setDescription("A rugged SUV perfect for off-road adventures and family trips.");
        models.add(mahindraScorpio);

        VehicleModel tataHarrier = new VehicleModel("Tata Harrier", "Diesel", false, 6200.0, "XUV", R.drawable.ic_xuv, 5, 230);
        tataHarrier.setDescription("A stylish SUV with a spacious interior and advanced features.");
        models.add(tataHarrier);

        VehicleModel mgHector = new VehicleModel("MG Hector", "Petrol", true, 5800.0, "XUV", R.drawable.ic_xuv, 5, 220);
        mgHector.setDescription("A premium SUV with a panoramic sunroof and smart connectivity.");
        models.add(mgHector);

        VehicleModel kiaSeltos = new VehicleModel("Kia Seltos", "Petrol", true, 5900.0, "XUV", R.drawable.ic_xuv, 5, 210);
        kiaSeltos.setDescription("A modern SUV with a bold design and excellent performance.");
        models.add(kiaSeltos);

        // Pickup Trucks (3 persons, medium range)
        VehicleModel tataYodha = new VehicleModel("Tata Yodha", "Diesel", true, 5500.0, "Pickup Truck", R.drawable.ic_pickup_truck, 3, 150);
        tataYodha.setDescription("A sturdy pickup truck for heavy-duty tasks and long hauls.");
        models.add(tataYodha);

        VehicleModel mahindraBoleroPickup = new VehicleModel("Mahindra Bolero Pickup", "Diesel", false, 5300.0, "Pickup Truck", R.drawable.ic_pickup_truck, 3, 140);
        mahindraBoleroPickup.setDescription("A reliable pickup truck with a strong build and good load capacity.");
        models.add(mahindraBoleroPickup);

        VehicleModel isuzuDMax = new VehicleModel("Isuzu D-Max", "Diesel", true, 5700.0, "Pickup Truck", R.drawable.ic_pickup_truck, 3, 160);
        isuzuDMax.setDescription("A versatile pickup truck with excellent off-road capabilities.");
        models.add(isuzuDMax);

        return models;
    }

    private void setupVehicleTypeRecyclerView() {
        if (vehicleTypeRecyclerView != null) {
            vehicleTypeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            vehicleTypeAdapter = new VehicleTypeAdapter(vehicleTypes, this::filterModelsByType);
            vehicleTypeRecyclerView.setAdapter(vehicleTypeAdapter);
            Log.d("MainActivity", "vehicleTypeRecyclerView adapter set with " + vehicleTypes.size() + " items");
        }
    }

    private void setupVehicleModelRecyclerView() {
        if (vehicleModelRecyclerView != null) {
            vehicleModelRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            vehicleModelAdapter = new VehicleModelAdapter(filteredVehicleModels);
            vehicleModelAdapter.setOnItemClickListener(vehicleModel -> {
                showVehicleDetailsBottomSheet(vehicleModel);
            });
            vehicleModelRecyclerView.setAdapter(vehicleModelAdapter);
            Log.d("MainActivity", "vehicleModelRecyclerView adapter set with " + filteredVehicleModels.size() + " items");
        }
    }

    private void showVehicleDetailsBottomSheet(VehicleModel vehicleModel) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_vehicle_details, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        ImageView vehicleImage = bottomSheetView.findViewById(R.id.vehicleImage);
        TextView vehicleName = bottomSheetView.findViewById(R.id.vehicleName);
        TextView fuelType = bottomSheetView.findViewById(R.id.fuelType);
        TextView availability = bottomSheetView.findViewById(R.id.availability);
        TextView seatingCapacity = bottomSheetView.findViewById(R.id.seatingCapacity);
        TextView description = bottomSheetView.findViewById(R.id.description);
        FloatingActionButton bookButton = bottomSheetView.findViewById(R.id.bookButton);

        if (vehicleImage != null) vehicleImage.setImageResource(vehicleModel.getImageResId());
        if (vehicleName != null) vehicleName.setText(vehicleModel.getName());
        if (fuelType != null) fuelType.setText("Fuel Type: " + vehicleModel.getFuelType());
        if (availability != null) availability.setText("Availability: " + (vehicleModel.isAvailable() ? "Available" : "Not Available"));
        if (seatingCapacity != null) seatingCapacity.setText("Seating Capacity: " + vehicleModel.getMaxPersons());
        if (description != null) description.setText("Description: " + vehicleModel.getDescription());

        if (bookButton != null) {
            bookButton.setOnClickListener(v -> {
                Booking booking = new Booking("user1", vehicleModel.getName(), vehicleModel.getVehicleType(),
                        vehicleModel.getFinalPrice(), vehicleModel.getImageResId());
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    db.bookingDao().insert(booking);
                }).start();

                Toast.makeText(this, vehicleModel.getName() + " added to To Pay", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();

                BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void setupSearchView() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Check the currently displayed screen
                    if (findViewById(R.id.fragmentContainer).getVisibility() == View.VISIBLE) {
                        // A fragment is visible, check if it's SeasonalFragment
                        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                        if (currentFragment instanceof SeasonalFragment) {
                            ((SeasonalFragment) currentFragment).filterVehicles(newText);
                        }
                    } else {
                        // Home screen is visible
                        filterModelsBySearch(newText);
                    }
                    return true;
                }
            });

            // Clear search query when the close button is clicked
            searchView.setOnCloseListener(() -> {
                searchView.setQuery("", false);
                if (findViewById(R.id.fragmentContainer).getVisibility() == View.VISIBLE) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (currentFragment instanceof SeasonalFragment) {
                        ((SeasonalFragment) currentFragment).filterVehicles("");
                    }
                } else {
                    filterModelsBySearch("");
                }
                return false;
            });
        }
    }

    private void filterModelsByType(String vehicleType) {
        filteredVehicleModels = allVehicleModels.stream()
                .filter(model -> model.getVehicleType().equals(vehicleType))
                .collect(Collectors.toList());
        if (vehicleModelAdapter != null) {
            vehicleModelAdapter.updateModels(filteredVehicleModels);
            Log.d("MainActivity", "Filtered models by type " + vehicleType + ": " + filteredVehicleModels.size() + " items");
        }
    }

    private void filterModelsBySearch(String query) {
        String selectedType = vehicleTypes.stream()
                .filter(VehicleType::isSelected)
                .findFirst()
                .map(VehicleType::getName)
                .orElse("Car");

        filteredVehicleModels = allVehicleModels.stream()
                .filter(model -> model.getVehicleType().equals(selectedType) &&
                        model.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        if (vehicleModelAdapter != null) {
            vehicleModelAdapter.updateModels(filteredVehicleModels);
            Log.d("MainActivity", "Filtered models by search '" + query + "': " + filteredVehicleModels.size() + " items");
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Spinner filterModelSpinner = dialogView.findViewById(R.id.filterModelSpinner);
        Spinner filterFuelTypeSpinner = dialogView.findViewById(R.id.filterFuelTypeSpinner);
        Spinner filterAvailabilitySpinner = dialogView.findViewById(R.id.filterAvailabilitySpinner);
        Spinner filterPersonsSpinner = dialogView.findViewById(R.id.filterPersonsSpinner);
        TextView personsNoteTextView = dialogView.findViewById(R.id.personsNoteTextView);
        Spinner filterRangeSpinner = dialogView.findViewById(R.id.filterRangeSpinner);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button applyButton = dialogView.findViewById(R.id.applyButton);

        List<String> vehicleTypesList = new ArrayList<>();
        vehicleTypesList.add("All");
        vehicleTypesList.add("Car");
        vehicleTypesList.add("Bike");
        vehicleTypesList.add("XUV");
        vehicleTypesList.add("Pickup Truck");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleTypesList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (filterModelSpinner != null) filterModelSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "Petrol", "Diesel"});
        fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (filterFuelTypeSpinner != null) filterFuelTypeSpinner.setAdapter(fuelTypeAdapter);

        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "Available", "Not Available"});
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (filterAvailabilitySpinner != null) filterAvailabilitySpinner.setAdapter(availabilityAdapter);

        ArrayAdapter<String> personsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "2", "2+"});
        personsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (filterPersonsSpinner != null) filterPersonsSpinner.setAdapter(personsAdapter);

        if (filterPersonsSpinner != null && personsNoteTextView != null) {
            filterPersonsSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    personsNoteTextView.setVisibility(filterPersonsSpinner.getSelectedItem().toString().equals("2+") ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    personsNoteTextView.setVisibility(View.GONE);
                }
            });
        }

        ArrayAdapter<String> rangeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "50", "100", "150", "200+"});
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (filterRangeSpinner != null) filterRangeSpinner.setAdapter(rangeAdapter);

        AlertDialog dialog = builder.create();

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                String vehicleType = filterModelSpinner != null ? filterModelSpinner.getSelectedItem().toString() : "All";
                String fuelType = filterFuelTypeSpinner != null ? filterFuelTypeSpinner.getSelectedItem().toString() : "All";
                String availability = filterAvailabilitySpinner != null ? filterAvailabilitySpinner.getSelectedItem().toString() : "All";
                String persons = filterPersonsSpinner != null ? filterPersonsSpinner.getSelectedItem().toString() : "All";
                String range = filterRangeSpinner != null ? filterRangeSpinner.getSelectedItem().toString() : "All";

                filteredVehicleModels = allVehicleModels.stream()
                        .filter(model -> {
                            if (!vehicleType.equals("All") && !model.getVehicleType().equals(vehicleType)) {
                                return false;
                            }
                            if (!fuelType.equals("All") && !model.getFuelType().equals(fuelType)) {
                                return false;
                            }
                            if (!availability.equals("All")) {
                                boolean isAvailable = availability.equals("Available");
                                if (model.isAvailable() != isAvailable) {
                                    return false;
                                }
                            }
                            if (!persons.equals("All")) {
                                if (persons.equals("2") && model.getMaxPersons() < 2) {
                                    return false;
                                } else if (persons.equals("2+")) {
                                    if (model.getVehicleType().equals("Bike")) {
                                        return false;
                                    }
                                    if (model.getMaxPersons() < 3 || model.getMaxPersons() > 5) {
                                        return false;
                                    }
                                }
                            }
                            if (!range.equals("All")) {
                                if (range.equals("200+")) {
                                    if (model.getRangeKm() < 200) {
                                        return false;
                                    }
                                } else {
                                    int rangeValue = Integer.parseInt(range);
                                    if (model.getRangeKm() < rangeValue || model.getRangeKm() >= (rangeValue + 50)) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        })
                        .collect(Collectors.toList());

                if (vehicleModelAdapter != null) {
                    vehicleModelAdapter.updateModels(filteredVehicleModels);
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                this.location = addresses.get(0).getLocality() != null ? addresses.get(0).getLocality() : "Unknown";
                            }
                        } catch (IOException e) {
                            this.location = "Unknown";
                        }
                    } else {
                        this.location = "Unknown";
                    }
                    fetchDemandFactor();
                    fetchWeather();
                    determineSeason();
                    determineTimeOfDay();
                    isLocationFetched = true;
                    if (vehicleModelLabel != null) vehicleModelLabel.setVisibility(View.VISIBLE);
                    if (vehicleModelRecyclerView != null) vehicleModelRecyclerView.setVisibility(View.VISIBLE);
                    // Set the parent card to VISIBLE
                    View vehicleModelCard = findViewById(R.id.vehicleModelCard);
                    if (vehicleModelCard != null) vehicleModelCard.setVisibility(View.VISIBLE);
                    updatePrices();
                    filterModelsByType("Car");
                })
                .addOnFailureListener(this, e -> {
                    this.location = "Unknown";
                    latitude = 0.0;
                    longitude = 0.0;
                    fetchDemandFactor();
                    fetchWeather();
                    determineSeason();
                    determineTimeOfDay();
                    isLocationFetched = true;
                    if (vehicleModelLabel != null) vehicleModelLabel.setVisibility(View.VISIBLE);
                    if (vehicleModelRecyclerView != null) vehicleModelRecyclerView.setVisibility(View.VISIBLE);
                    // Set the parent card to VISIBLE
                    View vehicleModelCard = findViewById(R.id.vehicleModelCard);
                    if (vehicleModelCard != null) vehicleModelCard.setVisibility(View.VISIBLE);
                    updatePrices();
                    filterModelsByType("Car");
                });
    }

    private void fetchDemandFactor() {
        String url = "https://mockapi.io/demand";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    demandFactor = 1.5; // Hardcoded for testing
                    updatePrices();
                },
                error -> {
                    demandFactor = 1.0;
                    updatePrices();
                });
        requestQueue.add(stringRequest);
    }

    private void fetchWeather() {
        String apiKey = "df1f92ce6e859f66ba5144360cd657d3";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject weatherObject = response.getJSONArray("weather").getJSONObject(0);
                        weather = weatherObject.getString("main");
                        updatePrices();
                    } catch (Exception e) {
                        weather = "Clear";
                        updatePrices();
                    }
                },
                error -> {
                    weather = "Clear";
                    updatePrices();
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void determineSeason() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        if (month >= 5 && month <= 7 || month == 11 || month == 0) {
            season = "Peak";
        } else {
            season = "Regular";
        }
        updatePrices();
    }

    private void determineTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 12) {
            timeOfDay = "Morning";
        } else if (hour >= 12 && hour < 17) {
            timeOfDay = "Afternoon";
        } else if (hour >= 17 && hour < 21) {
            timeOfDay = "Evening";
        } else {
            timeOfDay = "Night";
        }
        updatePrices();
    }

    private void updatePrices() {
        double locationFactor = highDemandCities.contains(location) ? 1.0 : 0.0;
        double seasonFactor = season.equals("Peak") ? 1.0 : 0.0;
        double timeOfDayFactor = (timeOfDay.equals("Morning") || timeOfDay.equals("Evening")) ? 1.0 : 0.5;
        double weatherFactor;
        switch (weather) {
            case "Rain":
            case "Snow":
                weatherFactor = 1.0;
                break;
            case "Clouds":
                weatherFactor = 0.5;
                break;
            default:
                weatherFactor = 0.0;
        }

        for (VehicleModel model : allVehicleModels) {
            double vehicleTypeFactor;
            switch (model.getVehicleType()) {
                case "Car":
                    vehicleTypeFactor = CAR_COEFF;
                    break;
                case "XUV":
                    vehicleTypeFactor = XUV_COEFF;
                    break;
                case "Pickup Truck":
                    vehicleTypeFactor = PICKUP_TRUCK_COEFF;
                    break;
                case "Bike":
                default:
                    vehicleTypeFactor = BIKE_COEFF;
                    break;
            }

            double finalPrice = INTERCEPT +
                    (demandFactor * DEMAND_COEFF) +
                    (locationFactor * LOCATION_COEFF) +
                    (seasonFactor * SEASON_COEFF) +
                    (timeOfDayFactor * TIME_OF_DAY_COEFF) +
                    (weatherFactor * WEATHER_COEFF) +
                    vehicleTypeFactor;

            finalPrice = Math.max(finalPrice, model.getBasePrice());
            model.setFinalPrice(finalPrice);
        }

        String selectedType = vehicleTypes.stream()
                .filter(VehicleType::isSelected)
                .findFirst()
                .map(VehicleType::getName)
                .orElse("Car");
        filterModelsByType(selectedType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                location = "Unknown";
                latitude = 0.0;
                longitude = 0.0;
                fetchDemandFactor();
                fetchWeather();
                determineSeason();
                determineTimeOfDay();
                isLocationFetched = true;
                if (vehicleModelLabel != null) vehicleModelLabel.setVisibility(View.VISIBLE);
                if (vehicleModelRecyclerView != null) vehicleModelRecyclerView.setVisibility(View.VISIBLE);
                updatePrices();
                filterModelsByType("Car");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("searchCardVisible", searchCard.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isSearchCardVisible = savedInstanceState.getBoolean("searchCardVisible", true);
        searchCard.setVisibility(isSearchCardVisible ? View.VISIBLE : View.GONE);
    }
}