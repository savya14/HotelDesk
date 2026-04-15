package com.hotel.hoteldesk;

import com.hotel.hoteldesk.manager.BookingManager;
import com.hotel.hoteldesk.manager.CustomerManager;
import com.hotel.hoteldesk.manager.RoomManager;
import com.hotel.hoteldesk.view.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        // Suppress macOS native warnings
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) { }
        }) {
            @Override
            public void println(String x) {
                // Only suppress macOS NSSavePanel warning
                if (x != null && x.contains("NSSavePanel")) return;
                super.println(x);
            }
        });

        launch(args);
    }
    @Override
    public void start(Stage stage) {

        RoomManager rm = new RoomManager();
        CustomerManager cm = new CustomerManager();
        BookingManager bm = new BookingManager(rm, cm);

        TabPane tabs = new TabPane();
        tabs.setTabMinWidth(100);
        tabs.setTabMinHeight(36);

        tabs.getTabs().addAll(
                new DashboardView(rm, bm).createTab(),
                new RoomView(rm).createTab(),
                new CustomerView(cm, rm).createTab(),
                new BookingView(bm, cm, rm).createTab(),
                new HistoryView(bm).createTab()
        );

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1280, 720);

        stage.setTitle("HotelDesk - Hotel Management System");
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.show();
    }

    private HBox buildHeader() {
        Label logo = new Label("HotelDesk");
        logo.setFont(Font.font("System", FontWeight.BOLD, 22));
        logo.setTextFill(Color.WHITE);

        Label tag = new Label("Hotel Management System");
        tag.setFont(Font.font("System", 13));
        tag.setTextFill(Color.web("#aed6f1"));

        Label ver = new Label("v1.0.0");
        ver.setFont(Font.font("System", 12));
        ver.setTextFill(Color.web("#aed6f1"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(15, new HBox(12, logo, tag), spacer, ver);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(12, 25, 12, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1a5276, #2980b9);");


        return header;
    }


}