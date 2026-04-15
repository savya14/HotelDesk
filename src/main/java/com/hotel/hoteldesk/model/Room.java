package com.hotel.hoteldesk.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Room {
    private final int roomNumber;
    private final RoomType roomType;
    private final double pricePerDay;
    private final BooleanProperty available;
    private final BooleanProperty underMaintenance;

    public Room(int roomNumber, RoomType roomType, double pricePerDay) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.available = new SimpleBooleanProperty(true);
        this.underMaintenance = new SimpleBooleanProperty(false);
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public double getPricePerDay() { return pricePerDay; }

    public boolean isAvailable() { return available.get(); }
    public void setAvailable(boolean value) { available.set(value); }
    public BooleanProperty availableProperty() { return available; }

    public boolean isUnderMaintenance() { return underMaintenance.get(); }
    public void setUnderMaintenance(boolean value) { underMaintenance.set(value); }
    public BooleanProperty underMaintenanceProperty() { return underMaintenance; }

    // A room is bookable only if available AND not under maintenance
    public boolean isBookable() {
        return isAvailable() && !isUnderMaintenance();
    }

    public String getStatusText() {
        if (isUnderMaintenance()) return "Maintenance";
        if (!isAvailable()) return "Occupied";
        return "Available";
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + roomType + ") - Rs." + pricePerDay + "/night";
    }
}