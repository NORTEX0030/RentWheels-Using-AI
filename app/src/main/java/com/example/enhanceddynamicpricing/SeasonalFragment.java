package com.example.enhanceddynamicpricing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SeasonalFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private RecyclerView seasonalVehicleRecyclerView;
    private VehicleModelAdapter vehicleModelAdapter;
    private List<VehicleModel> allVehicles;
    private List<VehicleModel> filteredVehicles;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private double latitude, longitude;
    private String location = "Unknown";
    private String weather = "Unknown";
    private TextView locationInfo;
    private TextView weatherInfo;
    private MaterialCardView locationInfoCard;
    private MaterialCardView weatherInfoCard;
    private MaterialCardView vehicleListCard;
    private MaterialCardView filterButtonCard;
    private List<String> highDemandCities = Arrays.asList("Mumbai", "Mira Bhayandar", "Thane", "Navi Mumbai");
    private TextView text1;
    private TextView text2;
    private FloatingActionButton filterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seasonal, container, false);

        // Initialize views
        text1 = view.findViewById(R.id.text1);
        text2 = view.findViewById(R.id.text2);
        filterButton = view.findViewById(R.id.filterButton);
        locationInfo = view.findViewById(R.id.locationInfo);
        weatherInfo = view.findViewById(R.id.weatherInfo);
        locationInfoCard = view.findViewById(R.id.locationInfoCard);
        weatherInfoCard = view.findViewById(R.id.weatherInfoCard);
        vehicleListCard = view.findViewById(R.id.vehicleListCard);
        filterButtonCard = view.findViewById(R.id.filterButtonCard);
        seasonalVehicleRecyclerView = view.findViewById(R.id.seasonalVehicleRecyclerView);
        Button mostDemandingButton = view.findViewById(R.id.mostDemandingButton);

        // Animate header text
        text1.setAlpha(0f);
        text1.setTranslationY(20f);
        text1.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start();

        text2.setAlpha(0f);
        text2.setTranslationY(20f);
        text2.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(400).start();

        // Animate FAB
        filterButton.setScaleX(0f);
        filterButton.setScaleY(0f);
        filterButton.animate().scaleX(1f).scaleY(1f).setDuration(300).setStartDelay(600).start();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Initialize vehicle list
        allVehicles = createDefaultVehicleList();
        filteredVehicles = new ArrayList<>(allVehicles);

        // Set up RecyclerView
        seasonalVehicleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vehicleModelAdapter = new VehicleModelAdapter(filteredVehicles);
        vehicleModelAdapter.setOnItemClickListener(vehicleModel -> {
            // Show bottom sheet with vehicle details
            showVehicleDetailsBottomSheet(vehicleModel);
        });
        seasonalVehicleRecyclerView.setAdapter(vehicleModelAdapter);

        // Set up button click listeners
        mostDemandingButton.setOnClickListener(v -> fetchLocationAndUpdateList());
        filterButton.setOnClickListener(v -> showFilterDialog());

        return view;
    }
    public void filterVehicles(String query) {
        if (query == null || query.trim().isEmpty()) {
            updateVehicleList(); // Reset to the location/weather-based list
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredVehicles = allVehicles.stream()
                    .filter(vehicle -> vehicle.getName().toLowerCase().contains(lowerCaseQuery) ||
                            vehicle.getVehicleType().toLowerCase().contains(lowerCaseQuery) ||
                            vehicle.getFuelType().toLowerCase().contains(lowerCaseQuery) ||
                            vehicle.getDescription().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
            vehicleModelAdapter.updateModels(filteredVehicles);
        }
    }

    private void showVehicleDetailsBottomSheet(VehicleModel vehicleModel) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_vehicle_details, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Initialize UI elements
        ImageView vehicleImage = bottomSheetView.findViewById(R.id.vehicleImage);
        TextView vehicleName = bottomSheetView.findViewById(R.id.vehicleName);
        TextView fuelType = bottomSheetView.findViewById(R.id.fuelType);
        TextView availability = bottomSheetView.findViewById(R.id.availability);
        TextView seatingCapacity = bottomSheetView.findViewById(R.id.seatingCapacity);
        TextView description = bottomSheetView.findViewById(R.id.description);
        FloatingActionButton bookButton = bottomSheetView.findViewById(R.id.bookButton);

        // Set vehicle details
        vehicleImage.setImageResource(vehicleModel.getImageResId());
        vehicleName.setText(vehicleModel.getName());
        fuelType.setText("Fuel Type: " + vehicleModel.getFuelType());
        availability.setText("Availability: " + (vehicleModel.isAvailable() ? "Available" : "Not Available"));
        seatingCapacity.setText("Seating Capacity: " + vehicleModel.getMaxPersons());
        description.setText("Description: " + vehicleModel.getDescription());

        // Set book button click listener
        bookButton.setOnClickListener(v -> {
            // Add to "To Pay" list
            Booking booking = new Booking("user1", vehicleModel.getName(), vehicleModel.getVehicleType(),
                    vehicleModel.getFinalPrice(), vehicleModel.getImageResId());
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(requireContext());
                db.bookingDao().insert(booking);
            }).start();

            Toast.makeText(requireContext(), vehicleModel.getName() + " added to To Pay", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();

            // Navigate to BookingsFragment
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
        });

        bottomSheetDialog.show();
    }

    private List<VehicleModel> createDefaultVehicleList() {
        List<VehicleModel> vehicles = new ArrayList<>();
        // Cars (4 persons, higher range)
        VehicleModel toyotaCamry = new VehicleModel("Toyota Camry", "Petrol", true, 5000.0, "Car", R.drawable.ic_car, 4, 200);
        toyotaCamry.setDescription("A luxurious sedan with excellent fuel efficiency and comfort for long drives.");
        vehicles.add(toyotaCamry);

        VehicleModel hondaCivic = new VehicleModel("Honda Civic", "Petrol", false, 4800.0, "Car", R.drawable.ic_car, 4, 180);
        hondaCivic.setDescription("A sporty sedan with a sleek design and advanced safety features.");
        vehicles.add(hondaCivic);

        VehicleModel hyundaiCreta = new VehicleModel("Hyundai Creta", "Diesel", true, 5200.0, "Car", R.drawable.ic_car, 4, 200);
        hyundaiCreta.setDescription("A compact SUV with a powerful engine and modern amenities.");
        vehicles.add(hyundaiCreta);

        VehicleModel marutiSwift = new VehicleModel("Maruti Swift", "Petrol", true, 4000.0, "Car", R.drawable.ic_car, 4, 150);
        marutiSwift.setDescription("A popular hatchback known for its agility and fuel efficiency.");
        vehicles.add(marutiSwift);

        VehicleModel fordEcoSport = new VehicleModel("Ford EcoSport", "Petrol", true, 5100.0, "Car", R.drawable.ic_car, 4, 190);
        fordEcoSport.setDescription("A compact SUV with a sporty look and great handling.");
        vehicles.add(fordEcoSport);

        VehicleModel tataNexon = new VehicleModel("Tata Nexon", "Diesel", false, 4900.0, "Car", R.drawable.ic_car, 4, 180);
        tataNexon.setDescription("A safe and stylish SUV with a 5-star safety rating.");
        vehicles.add(tataNexon);

        VehicleModel volkswagenPolo = new VehicleModel("Volkswagen Polo", "Petrol", true, 4700.0, "Car", R.drawable.ic_car, 4, 160);
        volkswagenPolo.setDescription("A premium hatchback with a solid build and smooth ride.");
        vehicles.add(volkswagenPolo);

        VehicleModel renaultDuster = new VehicleModel("Renault Duster", "Diesel", true, 5000.0, "Car", R.drawable.ic_car, 4, 200);
        renaultDuster.setDescription("A rugged SUV designed for adventure and comfort.");
        vehicles.add(renaultDuster);

        // Bikes (2 persons, lower range)
        VehicleModel hondaActiva = new VehicleModel("Honda Activa", "Petrol", true, 1500.0, "Bike", R.drawable.ic_car, 2, 50);
        hondaActiva.setDescription("A reliable scooter for city commuting with excellent mileage.");
        vehicles.add(hondaActiva);

        VehicleModel yamahaR15 = new VehicleModel("Yamaha R15", "Petrol", false, 1300.0, "Bike", R.drawable.ic_car, 2, 80);
        yamahaR15.setDescription("A sporty bike with a powerful engine and aerodynamic design.");
        vehicles.add(yamahaR15);

        VehicleModel bajajPulsar = new VehicleModel("Bajaj Pulsar", "Petrol", true, 1250.0, "Bike", R.drawable.ic_bike, 2, 70);
        bajajPulsar.setDescription("A versatile bike known for its performance and durability.");
        vehicles.add(bajajPulsar);

        VehicleModel tvsApache = new VehicleModel("TVS Apache", "Petrol", true, 1280.0, "Bike", R.drawable.ic_bike, 2, 60);
        tvsApache.setDescription("A racing-inspired bike with great handling and speed.");
        vehicles.add(tvsApache);

        VehicleModel royalEnfieldClassic = new VehicleModel("Royal Enfield Classic 350", "Petrol", true, 3500.0, "Bike", R.drawable.ic_bike, 2, 90);
        royalEnfieldClassic.setDescription("A classic bike with a vintage look and powerful performance.");
        vehicles.add(royalEnfieldClassic);

        VehicleModel ktmDuke = new VehicleModel("KTM Duke 200", "Petrol", false, 3200.0, "Bike", R.drawable.ic_bike, 2, 85);
        ktmDuke.setDescription("A streetfighter bike with aggressive styling and agility.");
        vehicles.add(ktmDuke);

        VehicleModel suzukiGixxer = new VehicleModel("Suzuki Gixxer", "Petrol", true, 2900.0, "Bike", R.drawable.ic_bike, 2, 75);
        suzukiGixxer.setDescription("A sporty bike with a refined engine and great handling.");
        vehicles.add(suzukiGixxer);

        VehicleModel heroSplendor = new VehicleModel("Hero Splendor", "Petrol", true, 1800.0, "Bike", R.drawable.ic_bike, 2, 45);
        heroSplendor.setDescription("A fuel-efficient bike ideal for daily commuting.");
        vehicles.add(heroSplendor);

        // XUVs (5 persons, higher range)
        VehicleModel mahindraScorpio = new VehicleModel("Mahindra Scorpio", "Diesel", true, 6000.0, "XUV", R.drawable.ic_xuv, 5, 250);
        mahindraScorpio.setDescription("A rugged SUV perfect for off-road adventures and family trips.");
        vehicles.add(mahindraScorpio);

        VehicleModel tataHarrier = new VehicleModel("Tata Harrier", "Diesel", false, 6200.0, "XUV", R.drawable.ic_xuv, 5, 230);
        tataHarrier.setDescription("A stylish SUV with a spacious interior and advanced features.");
        vehicles.add(tataHarrier);

        VehicleModel mgHector = new VehicleModel("MG Hector", "Petrol", true, 5800.0, "XUV", R.drawable.ic_xuv, 5, 220);
        mgHector.setDescription("A premium SUV with a panoramic sunroof and smart connectivity.");
        vehicles.add(mgHector);

        VehicleModel kiaSeltos = new VehicleModel("Kia Seltos", "Petrol", true, 5900.0, "XUV", R.drawable.ic_xuv, 5, 210);
        kiaSeltos.setDescription("A modern SUV with a bold design and excellent performance.");
        vehicles.add(kiaSeltos);

        VehicleModel jeepCompass = new VehicleModel("Jeep Compass", "Diesel", true, 6100.0, "XUV", R.drawable.ic_xuv, 5, 240);
        jeepCompass.setDescription("A premium SUV with off-road capability and luxury features.");
        vehicles.add(jeepCompass);

        VehicleModel hyundaiTucson = new VehicleModel("Hyundai Tucson", "Petrol", false, 6300.0, "XUV", R.drawable.ic_xuv, 5, 230);
        hyundaiTucson.setDescription("A sophisticated SUV with a refined design and comfort.");
        vehicles.add(hyundaiTucson);

        VehicleModel toyotaFortuner = new VehicleModel("Toyota Fortuner", "Diesel", true, 6500.0, "XUV", R.drawable.ic_xuv, 5, 260);
        toyotaFortuner.setDescription("A powerful SUV with a commanding presence and durability.");
        vehicles.add(toyotaFortuner);

        VehicleModel fordEndeavour = new VehicleModel("Ford Endeavour", "Diesel", true, 6400.0, "XUV", R.drawable.ic_xuv, 5, 250);
        fordEndeavour.setDescription("A robust SUV with excellent off-road performance and luxury.");
        vehicles.add(fordEndeavour);

        return vehicles;
    }

    private void fetchLocationAndUpdateList() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                this.location = addresses.get(0).getLocality() != null ? addresses.get(0).getLocality() : "Unknown";
                            }
                        } catch (IOException e) {
                            this.location = "Unknown";
                        }
                        fetchWeatherAndUpdateList();
                    } else {
                        this.location = "Unknown";
                        latitude = 0.0;
                        longitude = 0.0;
                        Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
                        updateVehicleList();
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    this.location = "Unknown";
                    latitude = 0.0;
                    longitude = 0.0;
                    Toast.makeText(requireContext(), "Error getting location", Toast.LENGTH_SHORT).show();
                    updateVehicleList();
                });
    }

    private void fetchWeatherAndUpdateList() {
        if (location.equals("Unknown")) {
            Toast.makeText(requireContext(), "Weather data unavailable due to unknown location", Toast.LENGTH_SHORT).show();
            updateVehicleList();
            return;
        }

        String apiKey = "df1f92ce6e859f66ba5144360cd657d3";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject weatherObject = response.getJSONArray("weather").getJSONObject(0);
                        weather = weatherObject.getString("main");
                        double temp = response.getJSONObject("main").getDouble("temp");
                        weather = weather + ", " + temp + "°C";
                    } catch (Exception e) {
                        weather = "Unknown";
                    }
                    updateVehicleList();
                },
                error -> {
                    weather = "Unknown";
                    Toast.makeText(requireContext(), "Error fetching weather data", Toast.LENGTH_SHORT).show();
                    updateVehicleList();
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void updateVehicleList() {
        // Show location and weather
        locationInfo.setText("Your Location: " + location);
        weatherInfo.setText("Current Weather: " + weather);
        locationInfoCard.setVisibility(View.VISIBLE);
        weatherInfoCard.setVisibility(View.VISIBLE);

        // Show the vehicle list card and filter button
        vehicleListCard.setVisibility(View.VISIBLE);
        filterButtonCard.setVisibility(View.VISIBLE);

        // Filter and sort vehicles based on location and weather
        filteredVehicles = new ArrayList<>(allVehicles);
        Log.d("SeasonalFragment", "Initial vehicle count: " + filteredVehicles.size());

        // Apply location-based filtering
        boolean isHighDemandCity = highDemandCities.contains(location);
        if (isHighDemandCity) {
            // Prioritize XUVs and Cars in high-demand cities
            filteredVehicles.sort((v1, v2) -> {
                if (v1.getVehicleType().equals("XUV") && !v2.getVehicleType().equals("XUV")) return -1;
                if (!v1.getVehicleType().equals("XUV") && v2.getVehicleType().equals("XUV")) return 1;
                if (v1.getVehicleType().equals("Car") && !v2.getVehicleType().equals("Car")) return -1;
                if (!v1.getVehicleType().equals("Car") && v2.getVehicleType().equals("Car")) return 1;
                return Double.compare(v2.getBasePrice(), v1.getBasePrice()); // Higher price first
            });
        } else {
            // Prioritize Bikes in non-high-demand cities
            filteredVehicles.sort((v1, v2) -> {
                if (v1.getVehicleType().equals("Bike") && !v2.getVehicleType().equals("Bike")) return -1;
                if (!v1.getVehicleType().equals("Bike") && v2.getVehicleType().equals("Bike")) return 1;
                return Double.compare(v2.getBasePrice(), v1.getBasePrice()); // Higher price first
            });
        }

        // Apply weather-based filtering
        if (!weather.equals("Unknown")) {
            String weatherCondition = weather.split(",")[0].trim();
            switch (weatherCondition) {
                case "Rain":
                case "Snow":
                    // Prioritize XUVs and Cars for bad weather
                    filteredVehicles = filteredVehicles.stream()
                            .filter(v -> v.getVehicleType().equals("XUV") || v.getVehicleType().equals("Car"))
                            .collect(Collectors.toList());
                    break;
                case "Clear":
                case "Clouds":
                    // Prioritize Bikes for good weather
                    filteredVehicles.sort((v1, v2) -> {
                        if (v1.getVehicleType().equals("Bike") && !v2.getVehicleType().equals("Bike")) return -1;
                        if (!v1.getVehicleType().equals("Bike") && v2.getVehicleType().equals("Bike")) return 1;
                        return 0;
                    });
                    break;
            }
        }

        Log.d("SeasonalFragment", "Filtered vehicle count: " + filteredVehicles.size());
        // Update the RecyclerView
        vehicleModelAdapter.updateModels(filteredVehicles);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        // Initialize dialog components
        Spinner filterModelSpinner = dialogView.findViewById(R.id.filterModelSpinner);
        Spinner filterFuelTypeSpinner = dialogView.findViewById(R.id.filterFuelTypeSpinner);
        Spinner filterAvailabilitySpinner = dialogView.findViewById(R.id.filterAvailabilitySpinner);
        Spinner filterPersonsSpinner = dialogView.findViewById(R.id.filterPersonsSpinner);
        TextView personsNoteTextView = dialogView.findViewById(R.id.personsNoteTextView);
        Spinner filterRangeSpinner = dialogView.findViewById(R.id.filterRangeSpinner);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button applyButton = dialogView.findViewById(R.id.applyButton);

        // Populate the vehicle type spinner
        List<String> vehicleTypesList = new ArrayList<>();
        vehicleTypesList.add("All");
        vehicleTypesList.add("Car");
        vehicleTypesList.add("Bike");
        vehicleTypesList.add("XUV");
        vehicleTypesList.add("Pickup Truck");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, vehicleTypesList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterModelSpinner.setAdapter(typeAdapter);

        // Set up other spinners
        ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"All", "Petrol", "Diesel"});
        fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterFuelTypeSpinner.setAdapter(fuelTypeAdapter);

        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"All", "Available", "Not Available"});
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterAvailabilitySpinner.setAdapter(availabilityAdapter);

        ArrayAdapter<String> personsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"All", "2", "2+"});
        personsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterPersonsSpinner.setAdapter(personsAdapter);

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

        ArrayAdapter<String> rangeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"All", "50", "100", "150", "200+"});
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterRangeSpinner.setAdapter(rangeAdapter);

        AlertDialog dialog = builder.create();

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Apply button
        applyButton.setOnClickListener(v -> {
            String vehicleType = filterModelSpinner.getSelectedItem().toString();
            String fuelType = filterFuelTypeSpinner.getSelectedItem().toString();
            String availability = filterAvailabilitySpinner.getSelectedItem().toString();
            String persons = filterPersonsSpinner.getSelectedItem().toString();
            String range = filterRangeSpinner.getSelectedItem().toString();

            // Apply filters
            filteredVehicles = allVehicles.stream()
                    .filter(model -> {
                        // Filter by vehicle type
                        if (!vehicleType.equals("All") && !model.getVehicleType().equals(vehicleType)) {
                            return false;
                        }

                        // Filter by fuel type
                        if (!fuelType.equals("All") && !model.getFuelType().equals(fuelType)) {
                            return false;
                        }

                        // Filter by availability
                        if (!availability.equals("All")) {
                            boolean isAvailable = availability.equals("Available");
                            if (model.isAvailable() != isAvailable) {
                                return false;
                            }
                        }

                        // Filter by persons
                        if (!persons.equals("All")) {
                            if (persons.equals("2") && model.getMaxPersons() < 2) {
                                return false;
                            } else if (persons.equals("2+")) {
                                // Exclude bikes for 2+
                                if (model.getVehicleType().equals("Bike")) {
                                    return false;
                                }
                                if (model.getMaxPersons() < 3 || model.getMaxPersons() > 5) {
                                    return false;
                                }
                            }
                        }

                        // Filter by range
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

            vehicleModelAdapter.updateModels(filteredVehicles);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndUpdateList();
            } else {
                location = "Unknown";
                latitude = 0.0;
                longitude = 0.0;
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                updateVehicleList();
            }
        }
    }
}