package com.hotel.hoteldesk.view;

import com.hotel.hoteldesk.manager.BookingManager;
import com.hotel.hoteldesk.manager.RoomManager;
import com.hotel.hoteldesk.model.Booking;
import com.hotel.hoteldesk.model.Room;
import com.hotel.hoteldesk.model.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardView {
    private final RoomManager rm;
    private final BookingManager bm;
    private Label totalVal, availVal, occVal, activeVal, revVal;
    private PieChart occChart, typeChart;

    public DashboardView(RoomManager rm, BookingManager bm) {
        this.rm = rm;
        this.bm = bm;
    }

    public Tab createTab() {
        Tab tab = new Tab("Dashboard");
        tab.setClosable(false);

        Label header = makeLabel("HotelDesk Dashboard", 26, true, "#1a5276");
        header.setMinHeight(40);

        Label sub = makeLabel("Real-time overview of hotel operations", 13, false, "#7f8c8d");
        sub.setMinHeight(25);

        totalVal  = new Label("0");
        availVal  = new Label("0");
        occVal    = new Label("0");
        activeVal = new Label("0");
        revVal    = new Label("Rs.0");

        HBox row1 = new HBox(20,
                metricCard("Total Rooms", totalVal, "#3498db"),
                metricCard("Available", availVal, "#27ae60"),
                metricCard("Occupied", occVal, "#e74c3c"));
        row1.setAlignment(Pos.CENTER);

        HBox row2 = new HBox(20,
                metricCard("Active Bookings", activeVal, "#8e44ad"),
                metricCard("Total Revenue", revVal, "#e67e22"));
        row2.setAlignment(Pos.CENTER);

        occChart  = makePie("Room Occupancy");
        typeChart = makePie("Room Types");

        VBox oc = chartCard(occChart, "#3498db");
        VBox tc = chartCard(typeChart, "#8e44ad");
        HBox.setHgrow(oc, Priority.ALWAYS);
        HBox.setHgrow(tc, Priority.ALWAYS);

        HBox charts = new HBox(20, oc, tc);
        charts.setAlignment(Pos.CENTER);

        refresh();
        rm.getRooms().addListener((ListChangeListener<Room>) c -> refresh());
        bm.getBookings().addListener((ListChangeListener<Booking>) c -> refresh());

        VBox layout = new VBox(20,
                header, sub,
                new Separator(),
                row1, row2,
                new Separator(),
                charts);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #f0f4f8;");

        tab.setContent(layout);
        return tab;
    }

    private VBox metricCard(String title, Label val, String color) {
        Label t = makeLabel(title, 13, true, "#7f8c8d");
        t.setMinHeight(25);
        t.setWrapText(false);

        val.setFont(Font.font("System", FontWeight.BOLD, 32));
        val.setStyle("-fx-text-fill: " + color + ";");
        val.setMinHeight(45);
        val.setWrapText(false);

        VBox c = new VBox(8, t, val);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(20));
        c.setPrefSize(200, 130);
        c.setMinSize(180, 120);
        c.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 0 0 4 0;" +
                "-fx-border-radius: 12;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private PieChart makePie(String title) {
        PieChart p = new PieChart();
        p.setTitle(title);
        p.setPrefHeight(280);
        p.setPrefWidth(400);
        p.setLabelsVisible(false);
        p.setLegendSide(Side.BOTTOM);
        p.setStartAngle(90);
        return p;
    }

    private VBox chartCard(PieChart chart, String borderColor) {
        VBox c = new VBox(chart);
        c.setPadding(new Insets(15));
        c.setAlignment(Pos.CENTER);
        c.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 0 0 4 0;" +
                "-fx-border-radius: 12;");
        return c;
    }

    private void refresh() {
        long t = rm.getTotalRooms();
        long a = rm.getAvailableRooms();
        long o = rm.getOccupiedRooms();
        long ab = bm.getActiveBookings();
        double r = bm.getTotalRevenue();

        totalVal.setText(String.valueOf(t));
        availVal.setText(String.valueOf(a));
        occVal.setText(String.valueOf(o));
        activeVal.setText(String.valueOf(ab));
        revVal.setText(String.format("Rs.%.0f", r));

        // Occupancy pie chart
        occChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Available (" + a + ")", Math.max(a, 0)),
                new PieChart.Data("Occupied (" + o + ")", Math.max(o, 0))));
        occChart.getData().forEach(d -> d.getNode().setStyle(
                d.getName().startsWith("Av")
                        ? "-fx-pie-color: #27ae60;"
                        : "-fx-pie-color: #e74c3c;"));

        // Room type pie chart
        long si = rm.countByType(RoomType.SINGLE);
        long db = rm.countByType(RoomType.DOUBLE);
        long dl = rm.countByType(RoomType.DELUXE);

        typeChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Single (" + si + ")", Math.max(si, 0)),
                new PieChart.Data("Double (" + db + ")", Math.max(db, 0)),
                new PieChart.Data("Deluxe (" + dl + ")", Math.max(dl, 0))));
        typeChart.getData().forEach(d -> d.getNode().setStyle(
                d.getName().startsWith("Si") ? "-fx-pie-color: #3498db;" :
                d.getName().startsWith("Do") ? "-fx-pie-color: #f39c12;" :
                                               "-fx-pie-color: #9b59b6;"));
    }

    private Label makeLabel(String text, int size, boolean bold, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("System",
                bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setStyle("-fx-text-fill: " + color + "; -fx-padding: 2 0 2 0;");
        l.setMinHeight(size + 10);
        l.setWrapText(false);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }
}
