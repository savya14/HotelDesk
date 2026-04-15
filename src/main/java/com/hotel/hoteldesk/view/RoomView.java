package com.hotel.hoteldesk.view;

import com.hotel.hoteldesk.manager.RoomManager;
import com.hotel.hoteldesk.model.Room;
import com.hotel.hoteldesk.model.RoomType;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RoomView {
    private final RoomManager rm;

    public RoomView(RoomManager rm) {
        this.rm = rm;
    }

    public Tab createTab() {
        Tab tab = new Tab("Rooms");
        tab.setClosable(false);
        VBox form = buildForm();
        VBox table = buildTable();
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox c = new HBox(20, form, table);
        c.setPadding(new Insets(20));
        c.setStyle("-fx-background-color:#f0f4f8;");
        tab.setContent(c);
        return tab;
    }

    private VBox buildForm() {
        // ═══ ADD ROOM ═══
        Label title = lbl("Add New Room", 18, "#1a5276");

        Label l1 = lbl("Room Number:", 13, "#2c3e50");
        TextField f1 = tf("e.g. 101", 230);
        f1.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*")) f1.setText(n.replaceAll("[^\\d]", ""));
        });

        Label l2 = lbl("Room Type:", 13, "#2c3e50");
        ComboBox<RoomType> cb = new ComboBox<>();
        cb.getItems().addAll(RoomType.values());
        cb.setValue(RoomType.SINGLE);
        cb.setPrefWidth(230);
        cb.setPrefHeight(36);

        Label l3 = lbl("Price Per Day (Rs.):", 13, "#2c3e50");
        TextField f2 = tf("e.g. 2500", 230);
        f2.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*\\.?\\d*")) f2.setText(a);
        });

        Label fb = new Label(" ");
        fb.setFont(Font.font("System", 12));
        fb.setWrapText(true);

        Button btn = btn("Save Room", "#2980b9", 230);
        btn.setOnAction(e -> {
            if (f1.getText().trim().isEmpty() || f2.getText().trim().isEmpty()) {
                fb.setText("Fill all fields.");
                fb.setStyle("-fx-text-fill:#e74c3c;");
                return;
            }
            try {
                int num = Integer.parseInt(f1.getText().trim());
                double pr = Double.parseDouble(f2.getText().trim());
                if (num <= 0 || pr <= 0) {
                    fb.setText("Values > 0.");
                    fb.setStyle("-fx-text-fill:#e74c3c;");
                    return;
                }
                if (rm.addRoom(num, cb.getValue(), pr)) {
                    fb.setText("Room " + num + " added!");
                    fb.setStyle("-fx-text-fill:#27ae60;");
                    f1.clear();
                    f2.clear();
                    cb.setValue(RoomType.SINGLE);
                } else {
                    fb.setText("Room exists.");
                    fb.setStyle("-fx-text-fill:#e74c3c;");
                }
            } catch (Exception ex) {
                fb.setText("Invalid input.");
                fb.setStyle("-fx-text-fill:#e74c3c;");
            }
        });

        // ═══ EDIT PRICE ═══
        Label editTitle = lbl("Edit Room Price", 16, "#8e44ad");

        Label el1 = lbl("Room Number:", 13, "#2c3e50");
        TextField ef1 = tf("Room # to edit", 230);
        ef1.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*")) ef1.setText(n.replaceAll("[^\\d]", ""));
        });

        Label el2 = lbl("New Price (Rs.):", 13, "#2c3e50");
        TextField ef2 = tf("New price", 230);
        ef2.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*\\.?\\d*")) ef2.setText(a);
        });

        Label efb = new Label(" ");
        efb.setFont(Font.font("System", 12));
        efb.setWrapText(true);

        Button editBtn = btn("Update Price", "#8e44ad", 230);
        editBtn.setOnAction(e -> {
            if (ef1.getText().trim().isEmpty() || ef2.getText().trim().isEmpty()) {
                efb.setText("Fill both fields.");
                efb.setStyle("-fx-text-fill:#e74c3c;");
                return;
            }
            try {
                int num = Integer.parseInt(ef1.getText().trim());
                double newPrice = Double.parseDouble(ef2.getText().trim());
                if (newPrice <= 0) {
                    efb.setText("Price must be > 0.");
                    efb.setStyle("-fx-text-fill:#e74c3c;");
                    return;
                }
                if (rm.findRoom(num) == null) {
                    efb.setText("Room " + num + " not found.");
                    efb.setStyle("-fx-text-fill:#e74c3c;");
                    return;
                }
                if (rm.updateRoomPrice(num, newPrice)) {
                    efb.setText("Room " + num + " → Rs." + String.format("%.0f", newPrice));
                    efb.setStyle("-fx-text-fill:#27ae60;");
                    ef1.clear();
                    ef2.clear();
                } else {
                    efb.setText("Update failed.");
                    efb.setStyle("-fx-text-fill:#e74c3c;");
                }
            } catch (Exception ex) {
                efb.setText("Invalid input.");
                efb.setStyle("-fx-text-fill:#e74c3c;");
            }
        });

        // ═══ MAINTENANCE TOGGLE ═══
        Label maintTitle = lbl("Room Maintenance", 16, "#e67e22");

        Label ml1 = lbl("Room Number:", 13, "#2c3e50");
        TextField mf1 = tf("Room # for maintenance", 230);
        mf1.textProperty().addListener((o, a, n) -> {
            if (!n.matches("\\d*")) mf1.setText(n.replaceAll("[^\\d]", ""));
        });

        Label mfb = new Label(" ");
        mfb.setFont(Font.font("System", 12));
        mfb.setWrapText(true);

        Button maintBtn = btn("Toggle Maintenance", "#e67e22", 230);
        maintBtn.setOnAction(e -> {
            if (mf1.getText().trim().isEmpty()) {
                mfb.setText("Enter room number.");
                mfb.setStyle("-fx-text-fill:#e74c3c;");
                return;
            }
            try {
                int num = Integer.parseInt(mf1.getText().trim());
                Room room = rm.findRoom(num);

                if (room == null) {
                    mfb.setText("Room " + num + " not found.");
                    mfb.setStyle("-fx-text-fill:#e74c3c;");
                    return;
                }

                if (!room.isAvailable() && !room.isUnderMaintenance()) {
                    mfb.setText("Room " + num + " is occupied. Checkout first.");
                    mfb.setStyle("-fx-text-fill:#e74c3c;");
                    return;
                }

                if (rm.toggleMaintenance(num)) {
                    Room updated = rm.findRoom(num);
                    if (updated.isUnderMaintenance()) {
                        mfb.setText("Room " + num + " → Under Maintenance");
                        mfb.setStyle("-fx-text-fill:#e67e22;");
                    } else {
                        mfb.setText("Room " + num + " → Available");
                        mfb.setStyle("-fx-text-fill:#27ae60;");
                    }
                    mf1.clear();
                } else {
                    mfb.setText("Toggle failed.");
                    mfb.setStyle("-fx-text-fill:#e74c3c;");
                }
            } catch (Exception ex) {
                mfb.setText("Invalid input.");
                mfb.setStyle("-fx-text-fill:#e74c3c;");
            }
        });

        // ═══ COMBINE ALL SECTIONS ═══
        VBox formContent = new VBox(10,
                title, new Separator(),
                l1, f1, l2, cb, l3, f2, btn, fb,
                new Separator(),
                editTitle, el1, ef1, el2, ef2, editBtn, efb,
                new Separator(),
                maintTitle, ml1, mf1, maintBtn, mfb
        );
        formContent.setPadding(new Insets(20));
        formContent.setPrefWidth(290);
        formContent.setAlignment(Pos.TOP_LEFT);

        ScrollPane sp = new ScrollPane(formContent);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");

        VBox wrapper = new VBox(sp);
        wrapper.setPrefWidth(310);
        wrapper.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        VBox.setVgrow(sp, Priority.ALWAYS);

        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        Label title = lbl("Room Inventory", 18, "#1a5276");

        Button fb = btn("Available Only", "#8e44ad", -1);
        Button vb = btn("View All", "#2c3e50", -1);
        fb.setOnAction(e -> rm.setFilterAvailableOnly(true));
        vb.setOnAction(e -> rm.setFilterAvailableOnly(false));

        TextField sf = tf("Search room #...", 180);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox bar = new HBox(10, fb, vb, sp, sf);
        bar.setAlignment(Pos.CENTER_LEFT);

        TableView<Room> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Room, Integer> c1 = new TableColumn<>("Room #");
        c1.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, RoomType> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, Double> c3 = new TableColumn<>("Price/Day");
        c3.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        c3.setCellFactory(c -> new TableCell<Room, Double>() {
            @Override
            protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                setText(e || v == null ? "" : String.format("Rs.%.0f", v));
            }
        });

        // ✅ Status column with 3 states
        TableColumn<Room, Boolean> c4 = new TableColumn<>("Status");
        c4.setCellValueFactory(new PropertyValueFactory<>("available"));
        c4.setCellFactory(c -> new TableCell<Room, Boolean>() {
            @Override
            protected void updateItem(Boolean v, boolean e) {
                super.updateItem(v, e);
                if (e) {
                    setGraphic(null);
                    setText("");
                    return;
                }

                Room room = getTableView().getItems().get(getIndex());
                String status = room.getStatusText();
                Label badge = new Label(status);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                badge.setTextFill(Color.WHITE);

                switch (status) {
                    case "Available":
                        badge.setStyle("-fx-background-color:#27ae60; -fx-background-radius:10;");
                        break;
                    case "Occupied":
                        badge.setStyle("-fx-background-color:#e74c3c; -fx-background-radius:10;");
                        break;
                    case "Maintenance":
                        badge.setStyle("-fx-background-color:#e67e22; -fx-background-radius:10;");
                        break;
                }

                setGraphic(badge);
                setText("");
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Room, Void> c5 = new TableColumn<>("Action");
        c5.setSortable(false);
        c5.setCellFactory(c -> new TableCell<Room, Void>() {
            final Button d = new Button("Delete");
            {
                d.setStyle(
                        "-fx-background-color:#e74c3c;-fx-text-fill:white;" +
                                "-fx-font-size:11px;-fx-font-weight:bold;" +
                                "-fx-background-radius:5;-fx-cursor:hand;-fx-padding:4 10;");
                d.setOnAction(e -> {
                    Room r = getTableView().getItems().get(getIndex());
                    if (!r.isAvailable() && !r.isUnderMaintenance()) {
                        alert("Occupied", "Checkout guest first.");
                        return;
                    }
                    new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete Room " + r.getRoomNumber() + "?",
                            ButtonType.OK, ButtonType.CANCEL)
                            .showAndWait()
                            .filter(x -> x == ButtonType.OK)
                            .ifPresent(x -> rm.deleteRoom(r.getRoomNumber()));
                });
            }

            @Override
            protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                setGraphic(e ? null : d);
                if (!e) setAlignment(Pos.CENTER);
            }
        });

        t.getColumns().addAll(c1, c2, c3, c4, c5);

        FilteredList<Room> fl = new FilteredList<>(rm.getFilteredRooms(), p -> true);
        sf.textProperty().addListener((o, a, n) ->
                fl.setPredicate(r ->
                        n == null || n.isEmpty()
                                || String.valueOf(r.getRoomNumber()).contains(n)));
        t.setItems(fl);

        VBox v = new VBox(12, title, bar, t);
        v.setPadding(new Insets(20));
        v.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        VBox.setVgrow(t, Priority.ALWAYS);
        return v;
    }

    private Label lbl(String t, int s, String c) {
        Label l = new Label(t);
        l.setFont(Font.font("System", FontWeight.BOLD, s));
        l.setStyle("-fx-text-fill:" + c + ";");
        return l;
    }

    private TextField tf(String p, int w) {
        TextField f = new TextField();
        f.setPromptText(p);
        f.setPrefWidth(w);
        f.setPrefHeight(42);
        f.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-font-size: 13px;");
        return f;
    }

    private Button btn(String t, String c, int w) {
        Button b = new Button(t);
        b.setStyle(
                "-fx-background-color:" + c + ";-fx-text-fill:white;" +
                        "-fx-font-weight:bold;-fx-background-radius:6;" +
                        "-fx-cursor:hand;-fx-padding:6 14;");
        if (w > 0) {
            b.setPrefWidth(w);
            b.setPrefHeight(40);
        }
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1));
        return b;
    }

    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}