package com.example.enhanceddynamicpricing;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "bookings")
public class Booking implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String userId;
    private String vehicleName;
    private String vehicleType;
    private double pricePerDay;
    private int imageResId;

    private String name;
    private String address;
    private String mobileNumber;
    @TypeConverters(DateConverter.class)
    private Date fromDate;
    @TypeConverters(DateConverter.class)
    private Date toDate;
    private String timings;

    private String status;

    public Booking(String userId, String vehicleName, String vehicleType, double pricePerDay, int imageResId) {
        this.userId = userId;
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.pricePerDay = pricePerDay;
        this.imageResId = imageResId;
        this.status = "To Pay";
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public Date getFromDate() { return fromDate; }
    public void setFromDate(Date fromDate) { this.fromDate = fromDate; }
    public Date getToDate() { return toDate; }
    public void setToDate(Date toDate) { this.toDate = toDate; }
    public String getTimings() { return timings; }
    public void setTimings(String timings) { this.timings = timings; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}