package com.hotel.hoteldesk.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {
    private final int bookingId;
    private final int customerId;
    private final String customerName;
    private final int roomNumber;
    private final RoomType roomType;
    private final double pricePerDay;

    private final LocalDate checkInDate;
    private final LocalDate plannedCheckOutDate;

    private LocalDate actualCheckOutDate;
    private double totalAmount;
    private boolean active;

    public Booking(int bookingId,
                   int customerId,
                   String customerName,
                   int roomNumber,
                   RoomType roomType,
                   double pricePerDay,
                   LocalDate checkInDate,
                   LocalDate plannedCheckOutDate) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.checkInDate = checkInDate;
        this.plannedCheckOutDate = plannedCheckOutDate;
        this.actualCheckOutDate = null;
        this.totalAmount = 0;
        this.active = true;
    }

    public void checkout() {
        this.actualCheckOutDate = plannedCheckOutDate;

        long nights = ChronoUnit.DAYS.between(checkInDate, plannedCheckOutDate);
        if (nights < 1) {
            nights = 1;
        }

        this.totalAmount = nights * pricePerDay;
        this.active = false;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getPlannedCheckOutDate() {
        return plannedCheckOutDate;
    }

    public LocalDate getCheckOutDate() {
        return actualCheckOutDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public boolean isActive() {
        return active;
    }

    public String getStatus() {
        return active ? "Active" : "Checked Out";
    }

    public long getNumberOfNights() {
        long nights = ChronoUnit.DAYS.between(checkInDate, plannedCheckOutDate);
        return Math.max(nights, 1);
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " - " + customerName + " (Room " + roomNumber + ")";
    }
}