package com.hotel.hoteldesk.view;

import com.hotel.hoteldesk.manager.BookingManager;
import com.hotel.hoteldesk.manager.CustomerManager;
import com.hotel.hoteldesk.manager.RoomManager;
import com.hotel.hoteldesk.model.Booking;
import com.hotel.hoteldesk.model.Customer;
import com.hotel.hoteldesk.model.Room;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingView {
    private final BookingManager bm;
    private final CustomerManager cm;
    private final RoomManager rm;
    private ComboBox<Booking> bkCombo;
    private TableView<Booking> table;

    public BookingView(BookingManager bm, CustomerManager cm, RoomManager rm) {
        this.bm = bm;
        this.cm = cm;
        this.rm = rm;
    }

    public Tab createTab() {
        Tab tab = new Tab("Bookings");
        tab.setClosable(false);
        VBox left = buildLeft();
        VBox right = buildTable();
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox c = new HBox(20, left, right);
        c.setPadding(new Insets(20));
        c.setStyle("-fx-background-color:#f0f4f8;");
        tab.setContent(c);
        return tab;
    }

    private VBox buildLeft() {

        // ════════════════════════════════
        //         BOOK A ROOM
        // ════════════════════════════════

        Label bt = lbl("Book a Room", 18, "#1a5276");

        Label cl = lbl("Select Customer:", 13, "#2c3e50");
        ComboBox<Customer> cc = new ComboBox<>();
        cc.setItems(cm.getCustomers());
        cc.setPromptText("-- Choose Customer --");
        cc.setPrefWidth(280);
        cc.setCellFactory(lv -> custCell());
        cc.setButtonCell(custCell());

        Label rl = lbl("Select Available Room:", 13, "#2c3e50");
        ComboBox<Room> rc = new ComboBox<>();
        rc.setPromptText("-- Choose Room --");
        rc.setPrefWidth(280);
        rc.setCellFactory(lv -> roomCell());
        rc.setButtonCell(roomCell());
        rc.setOnShowing(e -> {
            Room cur = rc.getValue();
            rc.getItems().setAll(
                    rm.getRooms().filtered(Room::isAvailable));
            if (cur != null && cur.isAvailable()) {
                rc.setValue(cur);
            }
        });

        Label cil = lbl("Check-in Date:", 13, "#2c3e50");
        DatePicker ci = new DatePicker(LocalDate.now());
        ci.setPrefWidth(280);
        ci.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean e) {
                super.updateItem(d, e);
                if (d.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color:#ffc0cb;");
                }
            }
        });

        Label col = lbl("Check-out Date:", 13, "#2c3e50");
        DatePicker co = new DatePicker(LocalDate.now().plusDays(1));
        co.setPrefWidth(280);
        co.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean e) {
                super.updateItem(d, e);
                LocalDate i = ci.getValue();
                if (i != null && d.isBefore(i.plusDays(1))) {
                    setDisable(true);
                    setStyle("-fx-background-color:#ffc0cb;");
                }
            }
        });

        ci.valueProperty().addListener((o, a, n) -> {
            if (n != null) {
                LocalDate cur = co.getValue();
                if (cur == null || !cur.isAfter(n)) {
                    co.setValue(n.plusDays(1));
                }
            }
        });

        Label nl = new Label("Stay Duration: 1 night");
        nl.setFont(Font.font("System", FontWeight.BOLD, 13));
        nl.setStyle("-fx-text-fill:#2980b9;");

        Runnable updateNights = () -> {
            LocalDate i = ci.getValue();
            LocalDate o2 = co.getValue();
            if (i != null && o2 != null && o2.isAfter(i)) {
                long n = ChronoUnit.DAYS.between(i, o2);
                nl.setText("Stay Duration: " + n + " night" + (n > 1 ? "s" : ""));
            } else {
                nl.setText("Stay Duration: --");
            }
        };
        ci.valueProperty().addListener((o, a, n) -> updateNights.run());
        co.valueProperty().addListener((o, a, n) -> updateNights.run());

        Label fb = new Label();
        fb.setFont(Font.font("System", 12));
        fb.setWrapText(true);

        Button bb = btn("Book Now", "#2980b9", 280);

        bb.setOnAction(e -> {
            Customer cu = cc.getValue();
            Room ro = rc.getValue();
            LocalDate i = ci.getValue();
            LocalDate o2 = co.getValue();

            if (cu == null || ro == null) {
                alert("Select customer and room.");
                return;
            }
            if (i == null || o2 == null || !o2.isAfter(i)) {
                alert("Invalid dates.");
                return;
            }
            if (!ro.isAvailable()) {
                alert("Room occupied.");
                return;
            }

            long nights = Math.max(ChronoUnit.DAYS.between(i, o2), 1);

            Booking b = bm.book(cu, ro, i, o2);
            if (b != null) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Booking #" + b.getBookingId() + " created!\n\n" +
                                "Guest: " + cu.getName() +
                                "\nRoom: " + ro.getRoomNumber() +
                                " (" + ro.getRoomType() + ")" +
                                "\nCheck-in: " + i +
                                "\nCheck-out: " + o2 +
                                "\nNights: " + nights +
                                "\nRate/Night: Rs." +
                                String.format("%.2f", ro.getPricePerDay()) +
                                "\nEst. Total: Rs." +
                                String.format("%.2f", nights * ro.getPricePerDay()))
                        .showAndWait();

                fb.setText("Booking #" + b.getBookingId() + " confirmed");
                fb.setStyle("-fx-text-fill:#27ae60;");

                // ✅ Reset without clearing items
                cc.setValue(null);
                rc.setValue(null);
                ci.setValue(LocalDate.now());
                co.setValue(LocalDate.now().plusDays(1));

                refreshActive();
                refreshTable();
            }
        });

        VBox bs = new VBox(8,
                bt, new Separator(),
                cl, cc,
                rl, rc,
                cil, ci,
                col, co,
                nl,
                bb, fb);
        bs.setPadding(new Insets(20));
        bs.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");

        // ════════════════════════════════
        //          CHECKOUT
        // ════════════════════════════════

        Label ct = lbl("Checkout", 18, "#1a5276");
        Label sl = lbl("Select Active Booking:", 13, "#2c3e50");

        bkCombo = new ComboBox<>();
        bkCombo.setPrefWidth(280);
        bkCombo.setPromptText("-- Choose Booking --");
        bkCombo.setCellFactory(lv -> bkCell());
        bkCombo.setButtonCell(bkCell());
        bkCombo.setOnShowing(ev -> refreshActive());

        Button cb2 = btn("Checkout", "#e67e22", 280);

        cb2.setOnAction(e -> {
            Booking sel = bkCombo.getValue();

            if (sel == null) {
                alert("Select a booking.");
                return;
            }

            String bill = bm.checkout(sel);

            if (bill != null) {
                Alert ba = new Alert(Alert.AlertType.INFORMATION);
                ba.setTitle("Bill");
                ba.setHeaderText("Checkout Complete");

                TextArea ta = new TextArea(bill);
                ta.setEditable(false);
                ta.setFont(Font.font("Monospaced", 13));
                ta.setPrefRowCount(14);

                ba.getDialogPane().setContent(ta);
                ba.showAndWait();

                // ✅ Just reset value — don't clear items
                bkCombo.setValue(null);
                refreshActive();
                refreshTable();
            }
        });

        VBox cs = new VBox(10,
                ct, new Separator(),
                sl, bkCombo,
                cb2);
        cs.setPadding(new Insets(20));
        cs.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");

        // ════════════════════════════════
        //       SCROLLABLE WRAPPER
        // ════════════════════════════════

        VBox lc = new VBox(20, bs, cs);
        ScrollPane scrollPane = new ScrollPane(lc);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background:transparent; " +
                        "-fx-background-color:transparent;");

        VBox w = new VBox(scrollPane);
        w.setPrefWidth(340);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return w;
    }

    // ════════════════════════════════
    //         BOOKINGS TABLE
    // ════════════════════════════════

    private VBox buildTable() {
        Label title = lbl("All Bookings", 18, "#1a5276");

        TextField sf = new TextField();
        sf.setPromptText("Search guest or room...");
        sf.setPrefWidth(250);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox hdr = new HBox(10, title, sp, sf);
        hdr.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        addCol("ID", d -> str(
                String.valueOf(d.getValue().getBookingId())));

        addCol("Guest", d -> str(
                d.getValue().getCustomerName()));

        addCol("Room", d -> str(
                String.valueOf(d.getValue().getRoomNumber())));

        addCol("Check-in", d -> str(
                d.getValue().getCheckInDate().toString()));

        addCol("Check-out", d -> str(
                d.getValue().getCheckOutDate() == null
                        ? "-"
                        : d.getValue().getCheckOutDate().toString()));

        addCol("Total", d -> {
            double t = d.getValue().getTotalAmount();
            return str(t == 0 ? "-" : String.format("Rs.%.2f", t));
        });

        // Status column with colored badge
        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Booking, String>,
                        ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<Booking, String> d) {
                        return new SimpleStringProperty(
                                d.getValue().getStatus());
                    }
                });

        statusCol.setCellFactory(
                new Callback<TableColumn<Booking, String>,
                        TableCell<Booking, String>>() {
                    @Override
                    public TableCell<Booking, String> call(
                            TableColumn<Booking, String> col2) {
                        return new TableCell<Booking, String>() {
                            @Override
                            protected void updateItem(String v, boolean e) {
                                super.updateItem(v, e);
                                if (e || v == null) {
                                    setGraphic(null);
                                    setText("");
                                    return;
                                }
                                Label b = new Label(v);
                                b.setPadding(new Insets(3, 10, 3, 10));
                                b.setFont(Font.font("System",
                                        FontWeight.BOLD, 11));
                                b.setTextFill(Color.WHITE);
                                b.setStyle(v.equals("Active")
                                        ? "-fx-background-color:#3498db;" +
                                        "-fx-background-radius:10;"
                                        : "-fx-background-color:#95a5a6;" +
                                        "-fx-background-radius:10;");
                                setGraphic(b);
                                setText("");
                                setAlignment(Pos.CENTER);
                            }
                        };
                    }
                });

        table.getColumns().add(statusCol);

        FilteredList<Booking> fl = new FilteredList<>(
                bm.getBookings(), p -> true);

        sf.textProperty().addListener((o, a, n) ->
                fl.setPredicate(b -> {
                    if (n == null || n.isEmpty()) return true;
                    String q = n.toLowerCase();
                    return b.getCustomerName().toLowerCase().contains(q)
                            || String.valueOf(b.getBookingId()).contains(q)
                            || String.valueOf(b.getRoomNumber()).contains(q);
                }));

        table.setItems(fl);

        VBox v = new VBox(12, hdr, table);
        v.setPadding(new Insets(20));
        v.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        VBox.setVgrow(table, Priority.ALWAYS);

        return v;
    }

    // ════════════════════════════════
    //       HELPER METHODS
    // ════════════════════════════════

    private void addCol(String name,
                        Callback<TableColumn.CellDataFeatures<Booking, String>,
                                ObservableValue<String>> factory) {
        TableColumn<Booking, String> col2 = new TableColumn<>(name);
        col2.setCellValueFactory(factory);
        table.getColumns().add(col2);
    }

    private ObservableValue<String> str(String val) {
        return new SimpleStringProperty(val);
    }

    private void refreshTable() {
        if (table != null) {
            table.refresh();
        }
    }

    private void refreshActive() {
        bkCombo.getItems().setAll(
                bm.getBookings().filtered(Booking::isActive));
    }

    private ListCell<Customer> custCell() {
        return new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer i, boolean e) {
                super.updateItem(i, e);
                if (e || i == null) {
                    setText(null);
                } else {
                    setText(i.getCustomerId() + " — " +
                            i.getName() + " (" + i.getContact() + ")");
                }
            }
        };
    }

    private ListCell<Room> roomCell() {
        return new ListCell<Room>() {
            @Override
            protected void updateItem(Room i, boolean e) {
                super.updateItem(i, e);
                if (e || i == null) {
                    setText(null);
                } else {
                    setText("Room " + i.getRoomNumber() +
                            " (" + i.getRoomType() + ") — Rs." +
                            (int) i.getPricePerDay() + "/night");
                }
            }
        };
    }

    private ListCell<Booking> bkCell() {
        return new ListCell<Booking>() {
            @Override
            protected void updateItem(Booking i, boolean e) {
                super.updateItem(i, e);
                if (e || i == null) {
                    setText(null);
                } else {
                    setText("#" + i.getBookingId() + " " +
                            i.getCustomerName() +
                            " (Room " + i.getRoomNumber() + ")");
                }
            }
        };
    }

    private Label lbl(String t, int s, String c) {
        Label l = new Label(t);
        l.setFont(Font.font("System", FontWeight.BOLD, s));
        l.setStyle("-fx-text-fill:" + c + ";");
        return l;
    }

    private Button btn(String t, String c, int w) {
        Button b = new Button(t);
        b.setPrefWidth(w);
        b.setPrefHeight(38);
        b.setStyle(
                "-fx-background-color:" + c + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1));
        return b;
    }

    private void alert(String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}