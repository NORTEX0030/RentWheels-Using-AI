package com.example.enhanceddynamicpricing;

public class VehicleModel {
    private String name;
    private String fuelType;
    private boolean isAvailable;
    private double basePrice;
    private double finalPrice;
    private String vehicleType;
    private int imageResId;
    private int maxPersons;
    private int rangeKm;
    private String description; // New field for description

    public VehicleModel(String name, String fuelType, boolean isAvailable, double basePrice, String vehicleType, int imageResId, int maxPersons, int rangeKm) {
        this.name = name;
        this.fuelType = fuelType;
        this.isAvailable = isAvailable;
        this.basePrice = basePrice;
        this.finalPrice = basePrice;
        this.vehicleType = vehicleType;
        this.imageResId = imageResId;
        this.maxPersons = maxPersons;
        this.rangeKm = rangeKm;
        this.description = "A reliable " + vehicleType.toLowerCase() + " for your travel needs."; // Default description
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getFuelType() {
        return fuelType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getMaxPersons() {
        return maxPersons;
    }

    public int getRangeKm() {
        return rangeKm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}