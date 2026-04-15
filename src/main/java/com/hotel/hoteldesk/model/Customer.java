package com.hotel.hoteldesk.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Customer {
    private final int customerId;
    private final String name;
    private final String contact;
    private final IntegerProperty assignedRoom;

    public Customer(int customerId, String name, String contact) {
        this.customerId = customerId;
        this.name = name;
        this.contact = contact;
        this.assignedRoom = new SimpleIntegerProperty(-1);
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public int getAssignedRoom() {
        return assignedRoom.get();
    }

    public void setAssignedRoom(int roomNumber) {
        assignedRoom.set(roomNumber);
    }

    public IntegerProperty assignedRoomProperty() {
        return assignedRoom;
    }

    @Override
    public String toString() {
        return customerId + " - " + name;
    }
}