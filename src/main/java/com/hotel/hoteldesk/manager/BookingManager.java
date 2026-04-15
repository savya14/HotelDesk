package com.hotel.hoteldesk.manager;

import com.hotel.hoteldesk.model.Booking;
import com.hotel.hoteldesk.model.Customer;
import com.hotel.hoteldesk.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingManager {
    private final ObservableList<Booking> bookings;
    private final RoomManager roomManager;
    private final CustomerManager customerManager;
    private int nextBookingId = 1;
    private double totalRevenue = 0;

    public BookingManager(RoomManager roomManager, CustomerManager customerManager) {
        this.roomManager = roomManager;
        this.customerManager = customerManager;
        this.bookings = FXCollections.observableArrayList();
    }

    public Booking book(Customer customer, Room room,
                        LocalDate checkIn, LocalDate checkOut) {
        if (!room.isAvailable()) {
            return null;
        }

        Booking booking = new Booking(
                nextBookingId++,
                customer.getCustomerId(),
                customer.getName(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerDay(),
                checkIn,
                checkOut
        );

        room.setAvailable(false);
        customer.setAssignedRoom(room.getRoomNumber());
        bookings.add(booking);
        return booking;
    }

    public String checkout(Booking booking) {
        if (!booking.isActive()) {
            return null;
        }

        booking.checkout();

        Room room = roomManager.findRoom(booking.getRoomNumber());
        if (room != null) {
            room.setAvailable(true);
        }

        Customer customer = customerManager.findById(booking.getCustomerId());
        if (customer != null) {
            customer.setAssignedRoom(-1);
        }

        totalRevenue += booking.getTotalAmount();

        int idx = bookings.indexOf(booking);
        if (idx >= 0) {
            bookings.set(idx, booking);
        }

        long nights = ChronoUnit.DAYS.between(
                booking.getCheckInDate(), booking.getCheckOutDate());
        if (nights < 1) {
            nights = 1;
        }

        return String.format(
                "========================================\n" +
                        "           HOTEL BILL SUMMARY           \n" +
                        "========================================\n\n" +
                        "  Booking ID:     #%d\n" +
                        "  Guest Name:     %s\n" +
                        "  Room:           %d (%s)\n" +
                        "  Check-in:       %s\n" +
                        "  Check-out:      %s\n" +
                        "  Nights:         %d\n" +
                        "  Rate/Night:     Rs.%.2f\n\n" +
                        "----------------------------------------\n" +
                        "  TOTAL:          Rs.%.2f\n" +
                        "========================================",
                booking.getBookingId(),
                booking.getCustomerName(),
                booking.getRoomNumber(),
                booking.getRoomType(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                nights,
                booking.getPricePerDay(),
                booking.getTotalAmount()
        );
    }

    public ObservableList<Booking> getBookings() {
        return bookings;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public long getActiveBookings() {
        return bookings.stream().filter(Booking::isActive).count();
    }
}