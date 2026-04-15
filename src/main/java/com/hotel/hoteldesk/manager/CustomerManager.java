package com.hotel.hoteldesk.manager;

import com.hotel.hoteldesk.model.Customer;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CustomerManager {
    private final ObservableList<Customer> customers;
    private int nextId = 1001;

    public CustomerManager() {
        customers = FXCollections.observableArrayList(
                customer -> new Observable[]{customer.assignedRoomProperty()}
        );
    }

    public Customer addCustomer(String name, String contact) {
        Customer c = new Customer(nextId++, name, contact);
        customers.add(c);
        return c;
    }

    public Customer findById(int id) {
        for (Customer c : customers) {
            if (c.getCustomerId() == id) {
                return c;
            }
        }
        return null;
    }

    // ===== NEW: DELETE CUSTOMER =====
    public boolean deleteCustomer(int customerId) {
        Customer customer = findById(customerId);
        if (customer == null) {
            return false;
        }
        // Cannot delete if room is assigned (active booking)
        if (customer.getAssignedRoom() != -1) {
            return false;
        }
        customers.remove(customer);
        return true;
    }

    public boolean hasActiveBooking(int customerId) {
        Customer customer = findById(customerId);
        return customer != null && customer.getAssignedRoom() != -1;
    }
    // ================================

    public ObservableList<Customer> getCustomers() {
        return customers;
    }
}