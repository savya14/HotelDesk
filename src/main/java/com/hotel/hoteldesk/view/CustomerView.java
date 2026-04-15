package com.hotel.hoteldesk.view;

import com.hotel.hoteldesk.manager.CustomerManager;
import com.hotel.hoteldesk.manager.RoomManager;
import com.hotel.hoteldesk.model.Customer;
import com.hotel.hoteldesk.model.Room;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CustomerView {
    private final CustomerManager cm;
    private final RoomManager rm;

    public CustomerView(CustomerManager cm, RoomManager rm) {
        this.cm = cm;
        this.rm = rm;
    }

    public Tab createTab() {
        Tab tab = new Tab("Customers");
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
        Label title = lbl("Register New Customer", 18, "#1a5276");

        Label l1 = lbl("Full Name:", 13, "#2c3e50");
        TextField f1 = tf("Enter full name", 260);

        Label l2 = lbl("Contact (10 digits):", 13, "#2c3e50");
        TextField f2 = tf("e.g. 9876543210", 260);
        f2.textProperty().addListener((o, a, n) -> {
            String d = n.replaceAll("[^\\d]", "");
            if (d.length() > 10) d = d.substring(0, 10);
            if (!d.equals(n)) f2.setText(d);
        });

        Label fb = new Label(" ");
        fb.setFont(Font.font("System", 12));
        fb.setWrapText(true);

        Button btn = btn("Register Customer", "#27ae60", 260);

        Label ot = lbl("Occupancy", 14, "#1a5276");
        Label ol = new Label();
        ol.setFont(Font.font("System", 13));
        ol.setStyle("-fx-text-fill:#2c3e50;");
        updateOcc(ol);
        rm.getRooms().addListener((ListChangeListener<Room>) c -> updateOcc(ol));

        VBox ob = new VBox(5, ot, ol);
        ob.setPadding(new Insets(10));
        ob.setStyle("-fx-background-color:#eaf2f8; -fx-background-radius:8;");

        btn.setOnAction(e -> {
            String name = f1.getText().trim();
            String contact = f2.getText().trim();
            if (name.isEmpty()) {
                fb.setText("Name required.");
                fb.setStyle("-fx-text-fill:#e74c3c;");
                return;
            }
            if (contact.length() != 10) {
                fb.setText("Contact must be 10 digits.");
                fb.setStyle("-fx-text-fill:#e74c3c;");
                return;
            }
            Customer c2 = cm.addCustomer(name, contact);
            fb.setText("Customer #" + c2.getCustomerId() + " registered!");
            fb.setStyle("-fx-text-fill:#27ae60;");
            f1.clear();
            f2.clear();
        });

        VBox v = new VBox(10,
                title, new Separator(),
                l1, f1,
                l2, f2,
                btn, fb,
                new Separator(), ob);
        v.setPadding(new Insets(20));
        v.setPrefWidth(310);
        v.setAlignment(Pos.TOP_LEFT);
        v.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        return v;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        Label title = lbl("Registered Customers", 18, "#1a5276");

        TextField sf = tf("Search name, ID, or phone...", 260);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox hdr = new HBox(10, title, sp, sf);
        hdr.setAlignment(Pos.CENTER_LEFT);

        TableView<Customer> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Customer, Integer> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Customer, String> c2 = new TableColumn<>("Name");
        c2.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Customer, String> c3 = new TableColumn<>("Contact");
        c3.setCellValueFactory(new PropertyValueFactory<>("contact"));

        TableColumn<Customer, Number> c4 = new TableColumn<>("Assigned Room");
        c4.setCellValueFactory(new PropertyValueFactory<>("assignedRoom"));
        c4.setCellFactory(c -> new TableCell<Customer, Number>() {
            @Override
            protected void updateItem(Number v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) {
                    setText("");
                    return;
                }
                int r = v.intValue();
                setText(r == -1 ? "—" : "" + r);
                setStyle(r == -1
                        ? "-fx-text-fill:#95a5a6;"
                        : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
            }
        });

        TableColumn<Customer, Void> c5 = new TableColumn<>("Action");
        c5.setSortable(false);
        c5.setPrefWidth(90);
        c5.setCellFactory(c -> new TableCell<Customer, Void>() {
            final Button d = new Button("Delete");
            {
                d.setStyle(
                        "-fx-background-color:#e74c3c;-fx-text-fill:white;" +
                                "-fx-font-size:11px;-fx-font-weight:bold;" +
                                "-fx-background-radius:5;-fx-cursor:hand;");
                d.setOnAction(e -> {
                    Customer cu = getTableView().getItems().get(getIndex());
                    if (cm.hasActiveBooking(cu.getCustomerId())) {
                        alert("Active booking. Checkout first.");
                        return;
                    }
                    new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete " + cu.getName() + "?",
                            ButtonType.OK, ButtonType.CANCEL)
                            .showAndWait()
                            .filter(x -> x == ButtonType.OK)
                            .ifPresent(x -> cm.deleteCustomer(cu.getCustomerId()));
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

        FilteredList<Customer> fl = new FilteredList<>(cm.getCustomers(), p -> true);
        sf.textProperty().addListener((o, a, n) -> fl.setPredicate(cu -> {
            if (n == null || n.isEmpty()) return true;
            String q = n.toLowerCase();
            return cu.getName().toLowerCase().contains(q)
                    || cu.getContact().contains(q)
                    || ("" + cu.getCustomerId()).contains(q);
        }));
        t.setItems(fl);

        VBox v = new VBox(12, hdr, t);
        v.setPadding(new Insets(20));
        v.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        VBox.setVgrow(t, Priority.ALWAYS);
        return v;
    }

    private void updateOcc(Label l) {
        long t = rm.getTotalRooms();
        long o = rm.getOccupiedRooms();
        l.setText(String.format("Occupied: %d / %d  (%.1f%%)",
                o, t, t == 0 ? 0 : (o * 100.0 / t)));
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
        b.setPrefWidth(w);
        b.setPrefHeight(40);
        b.setStyle(
                "-fx-background-color:" + c + ";-fx-text-fill:white;" +
                        "-fx-font-weight:bold;-fx-background-radius:6;-fx-cursor:hand;");
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