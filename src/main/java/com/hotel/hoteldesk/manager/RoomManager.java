package com.hotel.hoteldesk.manager;

import com.hotel.hoteldesk.model.Room;
import com.hotel.hoteldesk.model.RoomType;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class RoomManager {
    private final ObservableList<Room> rooms;
    private final FilteredList<Room> filteredRooms;

    public RoomManager() {
        rooms = FXCollections.observableArrayList(
                room -> new Observable[]{
                        room.availableProperty(),
                        room.underMaintenanceProperty()
                }
        );
        filteredRooms = new FilteredList<>(rooms, p -> true);
        loadSampleRooms();
    }

    private void loadSampleRooms() {
        rooms.add(new Room(101, RoomType.SINGLE, 1500));
        rooms.add(new Room(102, RoomType.SINGLE, 1500));
        rooms.add(new Room(201, RoomType.DOUBLE, 2500));
        rooms.add(new Room(202, RoomType.DOUBLE, 2500));
        rooms.add(new Room(301, RoomType.DELUXE, 4500));
        rooms.add(new Room(302, RoomType.DELUXE, 4500));
    }

    public boolean addRoom(int roomNumber, RoomType type, double price) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) return false;
        }
        rooms.add(new Room(roomNumber, type, price));
        return true;
    }

    public boolean deleteRoom(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null || !room.isAvailable()) return false;
        rooms.remove(room);
        return true;
    }

    public boolean updateRoomPrice(int roomNumber, double newPrice) {
        Room room = findRoom(roomNumber);
        if (room == null || newPrice <= 0) return false;
        int index = rooms.indexOf(room);
        boolean wasAvailable = room.isAvailable();
        boolean wasMaintenance = room.isUnderMaintenance();
        rooms.remove(index);
        Room updated = new Room(roomNumber, room.getRoomType(), newPrice);
        updated.setAvailable(wasAvailable);
        updated.setUnderMaintenance(wasMaintenance);
        rooms.add(index, updated);
        return true;
    }

    // Toggle maintenance mode
    public boolean toggleMaintenance(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null) return false;

        // Cannot put occupied room under maintenance
        if (!room.isAvailable() && !room.isUnderMaintenance()) return false;

        if (room.isUnderMaintenance()) {
            // Remove from maintenance — make available again
            room.setUnderMaintenance(false);
            room.setAvailable(true);
        } else {
            // Put under maintenance — make unavailable
            room.setUnderMaintenance(true);
            room.setAvailable(false);
        }
        return true;
    }

    public Room findRoom(int roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) return r;
        }
        return null;
    }

    public ObservableList<Room> getRooms() { return rooms; }
    public FilteredList<Room> getFilteredRooms() { return filteredRooms; }

    public void setFilterAvailableOnly(boolean val) {
        filteredRooms.setPredicate(val ? Room::isBookable : p -> true);
    }

    public long getTotalRooms() { return rooms.size(); }

    public long getAvailableRooms() {
        return rooms.stream().filter(Room::isBookable).count();
    }

    public long getOccupiedRooms() {
        return rooms.stream()
                .filter(r -> !r.isAvailable() && !r.isUnderMaintenance())
                .count();
    }

    public long getMaintenanceRooms() {
        return rooms.stream().filter(Room::isUnderMaintenance).count();
    }

    public long countByType(RoomType type) {
        return rooms.stream().filter(r -> r.getRoomType() == type).count();
    }
}