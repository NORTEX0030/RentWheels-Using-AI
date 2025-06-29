package com.example.enhanceddynamicpricing;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookingDao {
    @Insert
    void insert(Booking booking);

    @Update
    void update(Booking booking);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status = 'To Pay'")
    List<Booking> getToPayBookings(String userId);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status = 'Placed'")
    List<Booking> getPlacedBookings(String userId);
}