package com.hotel.hoteldesk.view;

import com.hotel.hoteldesk.manager.BookingManager;
import com.hotel.hoteldesk.model.Booking;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HistoryView {
    private final BookingManager bm;
    private TableView<Booking> table;
    private FilteredList<Booking> data;
    private Label revL, avgL, gstL, stayL;

    public HistoryView(BookingManager bm) {
        this.bm = bm;
    }

    public Tab createTab() {
        Tab tab = new Tab("History");
        tab.setClosable(false);
        VBox root = build();
        root.setStyle("-fx-background-color:#f0f4f8;");
        tab.setContent(root);
        return tab;
    }

    private VBox build() {
        revL = new Label("Rs.0");
        avgL = new Label("Rs.0");
        gstL = new Label("0");
        stayL = new Label("0 nights");

        HBox cards = new HBox(20,
                aCard("Total Revenue", revL, "#27ae60"),
                aCard("Avg Bill", avgL, "#3498db"),
                aCard("Guests Served", gstL, "#8e44ad"),
                aCard("Avg Stay", stayL, "#e67e22"));
        cards.setAlignment(Pos.CENTER);

        Label title = lbl("Booking History", 20, "#1a5276");
        Label sub = new Label("Complete record of all checked-out guests");
        sub.setFont(Font.font("System", 13));
        sub.setStyle("-fx-text-fill:#7f8c8d;");

        TextField sf = new TextField();
        sf.setPromptText("Search guest, room, or ID...");
        sf.setPrefWidth(280);
        sf.setPrefHeight(36);
        sf.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 12;" +
                        "-fx-font-size: 13px;");

        // Generate Receipt (PDF via Print)
        Button receiptBtn = new Button("Generate Receipt (PDF)");
        receiptBtn.setStyle(
                "-fx-background-color:#2980b9; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6; " +
                        "-fx-padding:7 16; -fx-cursor:hand;");
        receiptBtn.setOnMouseEntered(e -> receiptBtn.setOpacity(0.85));
        receiptBtn.setOnMouseExited(e -> receiptBtn.setOpacity(1));

        // Export CSV
        Button exp = new Button("Export CSV");
        exp.setStyle(
                "-fx-background-color:#27ae60; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6; " +
                        "-fx-padding:7 16; -fx-cursor:hand;");
        exp.setOnMouseEntered(e -> exp.setOpacity(0.85));
        exp.setOnMouseExited(e -> exp.setOpacity(1));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox hdr = new HBox(12, title, sp, sf, receiptBtn, exp);
        hdr.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No checkout history yet."));

        addCol("Booking ID", d -> str(String.valueOf(d.getValue().getBookingId())));
        addCol("Guest", d -> str(d.getValue().getCustomerName()));
        addCol("Room", d -> str(String.valueOf(d.getValue().getRoomNumber())));
        addCol("Type", d -> str(d.getValue().getRoomType().toString()));
        addCol("Check-in", d -> str(d.getValue().getCheckInDate().toString()));
        addCol("Check-out", d -> str(
                d.getValue().getCheckOutDate() == null
                        ? "—" : d.getValue().getCheckOutDate().toString()));
        addCol("Nights", d -> {
            Booking b = d.getValue();
            if (b.getCheckOutDate() == null) return str("—");
            return str(String.valueOf(Math.max(
                    ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()), 1)));
        });
        addCol("Rate/Night", d -> str(
                String.format("Rs.%.0f", d.getValue().getPricePerDay())));
        addCol("Total Bill", d -> str(
                String.format("Rs.%.2f", d.getValue().getTotalAmount())));

        data = new FilteredList<>(bm.getBookings(), b -> !b.isActive());

        sf.textProperty().addListener((o, a, n) -> {
            data.setPredicate(b -> {
                if (b.isActive()) return false;
                if (n == null || n.isEmpty()) return true;
                String q = n.toLowerCase();
                return b.getCustomerName().toLowerCase().contains(q)
                        || String.valueOf(b.getBookingId()).contains(q)
                        || String.valueOf(b.getRoomNumber()).contains(q);
            });
            updateStats();
        });

        table.setItems(data);
        bm.getBookings().addListener((ListChangeListener<Booking>) c -> {
            data.setPredicate(b -> !b.isActive());
            table.refresh();
            updateStats();
        });

        exp.setOnAction(e -> exportCSV());

        receiptBtn.setOnAction(e -> {
            Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("No Selection");
                warn.setHeaderText(null);
                warn.setContentText("Please select a booking from the table first.");
                warn.showAndWait();
                return;
            }
            generateReceipt(selected);
        });

        updateStats();

        VBox tc = new VBox(12, hdr, sub, table);
        tc.setPadding(new Insets(20));
        tc.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(20, cards, tc);
        root.setPadding(new Insets(20));
        VBox.setVgrow(tc, Priority.ALWAYS);
        return root;
    }

    // ═══════════════════════════════════════════
    //    RECEIPT — Preview + Print as PDF + Save TXT
    // ═══════════════════════════════════════════

    private void generateReceipt(Booking b) {
        long nights = b.getCheckOutDate() == null ? 1
                : Math.max(java.time.temporal.ChronoUnit.DAYS.between(
                b.getCheckInDate(), b.getCheckOutDate()), 1);
        double roomTotal = nights * b.getPricePerDay();

        String line = "==============================================";
        String thin = "----------------------------------------------";

        StringBuilder r = new StringBuilder();
        r.append(line).append("\n");
        r.append("          HOTELDESK — HOTEL INVOICE           \n");
        r.append(line).append("\n\n");
        r.append(String.format("  %-20s %s%n", "Invoice #:", b.getBookingId()));
        r.append(String.format("  %-20s %s%n", "Date:", java.time.LocalDate.now()));
        r.append("\n").append(thin).append("\n");
        r.append("  GUEST DETAILS\n");
        r.append(thin).append("\n\n");
        r.append(String.format("  %-20s %s%n", "Name:", b.getCustomerName()));
        r.append(String.format("  %-20s %d (%s)%n", "Room:",
                b.getRoomNumber(), b.getRoomType()));
        r.append("\n").append(thin).append("\n");
        r.append("  STAY DETAILS\n");
        r.append(thin).append("\n\n");
        r.append(String.format("  %-20s %s%n", "Check-in:", b.getCheckInDate()));
        r.append(String.format("  %-20s %s%n", "Check-out:", b.getCheckOutDate()));
        r.append(String.format("  %-20s %d%n", "Nights:", nights));
        r.append(String.format("  %-20s Rs.%.2f%n", "Rate/Night:", b.getPricePerDay()));
        r.append(String.format("  %-20s Rs.%.2f%n", "Room Total:", roomTotal));
        r.append("\n").append(line).append("\n");
        r.append(String.format("  %-20s Rs.%.2f%n", "TOTAL AMOUNT:", b.getTotalAmount()));
        r.append(line).append("\n\n");
        r.append("  Thank you for staying with us!\n");
        r.append("  — HotelDesk Management System\n");

        // Show receipt in dialog
        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Invoice / Receipt");
        receiptAlert.setHeaderText("Receipt for Booking #" + b.getBookingId());

        TextArea ta = new TextArea(r.toString());
        ta.setEditable(false);
        ta.setFont(Font.font("Courier New", 13));
        ta.setPrefRowCount(28);
        ta.setPrefColumnCount(52);

        // ✅ Save as PDF button (uses system Print → Save as PDF)
        Button pdfBtn = new Button("Save as PDF");
        pdfBtn.setStyle(
                "-fx-background-color:#e74c3c; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6; " +
                        "-fx-cursor:hand; -fx-padding:8 20;");
        pdfBtn.setOnMouseEntered(ev -> pdfBtn.setOpacity(0.85));
        pdfBtn.setOnMouseExited(ev -> pdfBtn.setOpacity(1));

        pdfBtn.setOnAction(ev -> {
            // Create a printable node
            javafx.scene.text.Text printText = new javafx.scene.text.Text(r.toString());
            printText.setFont(Font.font("Courier New", 12));
            printText.setWrappingWidth(500);

            javafx.print.PrinterJob job =
                    javafx.print.PrinterJob.createPrinterJob();

            if (job != null) {
                // Show print dialog — user can choose "Save as PDF"
                boolean proceed = job.showPrintDialog(
                        table.getScene().getWindow());

                if (proceed) {
                    boolean printed = job.printPage(printText);
                    if (printed) {
                        job.endJob();
                        Alert ok = new Alert(Alert.AlertType.INFORMATION);
                        ok.setTitle("PDF Saved");
                        ok.setHeaderText(null);
                        ok.setContentText(
                                "Receipt saved/printed successfully!");
                        ok.showAndWait();
                    }
                }
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setHeaderText(null);
                err.setContentText("No printer available.");
                err.showAndWait();
            }
        });

        // ✅ Save as Text button
        Button txtBtn = new Button("Save as Text");
        txtBtn.setStyle(
                "-fx-background-color:#2980b9; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6; " +
                        "-fx-cursor:hand; -fx-padding:8 20;");
        txtBtn.setOnMouseEntered(ev -> txtBtn.setOpacity(0.85));
        txtBtn.setOnMouseExited(ev -> txtBtn.setOpacity(1));

        txtBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Receipt");
            fc.setInitialFileName(
                    "receipt_booking_" + b.getBookingId() + ".txt");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fc.showSaveDialog(table.getScene().getWindow());
            if (file != null) {
                try (PrintWriter w = new PrintWriter(file)) {
                    w.print(r.toString());
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Saved");
                    ok.setHeaderText(null);
                    ok.setContentText(
                            "Receipt saved to:\n" + file.getAbsolutePath());
                    ok.showAndWait();
                } catch (Exception ex) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Error");
                    err.setHeaderText(null);
                    err.setContentText(
                            "Could not save: " + ex.getMessage());
                    err.showAndWait();
                }
            }
        });

        HBox buttons = new HBox(12, pdfBtn, txtBtn);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox content = new VBox(12, ta, buttons);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        receiptAlert.getDialogPane().setContent(content);
        receiptAlert.getDialogPane().setPrefWidth(600);
        receiptAlert.showAndWait();
    }

    // ═══════════════════════════════════════════
    //    PRINT AS PDF (Uses JavaFX Print API)
    //    On Mac: Choose "Save as PDF" in print dialog
    // ═══════════════════════════════════════════

    private void printReceiptAsPDF(Booking b, long nights, double roomTotal) {
        // Build a styled TextFlow for printing
        TextFlow receiptFlow = new TextFlow();
        receiptFlow.setPadding(new Insets(30));
        receiptFlow.setPrefWidth(500);
        receiptFlow.setStyle("-fx-background-color: white;");

        // Hotel name
        Text hotelName = new Text("HOTELDESK\n");
        hotelName.setFont(Font.font("System", FontWeight.BOLD, 24));

        Text hotelSub = new Text("Hotel Management System\n\n");
        hotelSub.setFont(Font.font("System", 12));

        // Line
        Text line1 = new Text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        line1.setFont(Font.font("Monospaced", 10));

        Text invoiceTitle = new Text("INVOICE\n\n");
        invoiceTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Invoice details
        Text details = new Text(
                "Invoice #:       " + b.getBookingId() + "\n" +
                        "Date:            " + LocalDate.now() + "\n\n"
        );
        details.setFont(Font.font("System", 13));

        // Line
        Text line2 = new Text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        line2.setFont(Font.font("Monospaced", 10));

        Text guestTitle = new Text("GUEST DETAILS\n\n");
        guestTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        Text guestDetails = new Text(
                "Name:            " + b.getCustomerName() + "\n" +
                        "Room:            " + b.getRoomNumber() + " (" + b.getRoomType() + ")\n\n"
        );
        guestDetails.setFont(Font.font("System", 13));

        // Line
        Text line3 = new Text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        line3.setFont(Font.font("Monospaced", 10));

        Text stayTitle = new Text("STAY DETAILS\n\n");
        stayTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        Text stayDetails = new Text(
                "Check-in:        " + b.getCheckInDate() + "\n" +
                        "Check-out:       " + b.getCheckOutDate() + "\n" +
                        "Nights:          " + nights + "\n" +
                        "Rate/Night:      Rs." + String.format("%.2f", b.getPricePerDay()) + "\n" +
                        "Room Total:      Rs." + String.format("%.2f", roomTotal) + "\n\n"
        );
        stayDetails.setFont(Font.font("System", 13));

        // Line
        Text line4 = new Text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        line4.setFont(Font.font("Monospaced", 10));

        // Total
        Text totalLabel = new Text("TOTAL AMOUNT:    ");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Text totalValue = new Text("Rs." + String.format("%.2f", b.getTotalAmount()) + "\n\n");
        totalValue.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Line
        Text line5 = new Text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        line5.setFont(Font.font("Monospaced", 10));

        // Thank you
        Text thanks = new Text("Thank you for staying with us!\n");
        thanks.setFont(Font.font("System", FontWeight.BOLD, 12));

        Text footer = new Text("— HotelDesk Management System\n");
        footer.setFont(Font.font("System", 11));

        receiptFlow.getChildren().addAll(
                hotelName, hotelSub,
                line1, invoiceTitle, details,
                line2, guestTitle, guestDetails,
                line3, stayTitle, stayDetails,
                line4, totalLabel, totalValue,
                line5, thanks, footer
        );

        // Use JavaFX PrinterJob
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Show print dialog — user can choose "Save as PDF" on Mac
            boolean proceed = job.showPrintDialog(table.getScene().getWindow());
            if (proceed) {
                boolean printed = job.printPage(receiptFlow);
                if (printed) {
                    job.endJob();
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Printed");
                    ok.setHeaderText(null);
                    ok.setContentText("Receipt sent to printer / saved as PDF successfully!");
                    ok.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Print Failed");
                    err.setHeaderText(null);
                    err.setContentText("Could not print the receipt.");
                    err.showAndWait();
                }
            }
        } else {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("No Printer");
            err.setHeaderText(null);
            err.setContentText("No printer available. Please check your printer settings.");
            err.showAndWait();
        }
    }

    // ═══════════════════════════════════════════
    //       HELPER METHODS
    // ═══════════════════════════════════════════

    private void addCol(String name,
                        Callback<TableColumn.CellDataFeatures<Booking, String>,
                                ObservableValue<String>> factory) {
        TableColumn<Booking, String> col = new TableColumn<>(name);
        col.setCellValueFactory(factory);
        table.getColumns().add(col);
    }

    private ObservableValue<String> str(String val) {
        return new SimpleStringProperty(val);
    }

    private VBox aCard(String title, Label val, String color) {
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        t.setStyle("-fx-text-fill:#7f8c8d;");
        val.setFont(Font.font("System", FontWeight.BOLD, 24));
        val.setStyle("-fx-text-fill:" + color + ";");
        VBox c = new VBox(6, t, val);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(15));
        c.setPrefSize(190, 90);
        c.setStyle(
                "-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);" +
                        "-fx-border-color:" + color +
                        "; -fx-border-width:0 0 3 0; -fx-border-radius:10;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private void updateStats() {
        double rev = 0;
        long nights = 0;
        int cnt = 0;
        for (Booking b : data) {
            if (b.isActive()) continue;
            rev += b.getTotalAmount();
            if (b.getCheckInDate() != null && b.getCheckOutDate() != null)
                nights += Math.max(ChronoUnit.DAYS.between(
                        b.getCheckInDate(), b.getCheckOutDate()), 1);
            cnt++;
        }
        revL.setText(String.format("Rs.%.0f", rev));
        gstL.setText("" + cnt);
        avgL.setText(cnt > 0 ? String.format("Rs.%.0f", rev / cnt) : "Rs.0");
        stayL.setText(cnt > 0
                ? String.format("%.1f nights", (double) nights / cnt)
                : "0 nights");
    }

    private void exportCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export History");
        fc.setInitialFileName("hotel_history.csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showSaveDialog(table.getScene().getWindow());
        if (f == null) return;
        try (PrintWriter w = new PrintWriter(f)) {
            w.println("ID,Guest,Room,Type,CheckIn,CheckOut,Nights,Rate,Total");
            for (Booking b : data) {
                if (b.isActive()) continue;
                long n = b.getCheckOutDate() == null ? 1
                        : Math.max(ChronoUnit.DAYS.between(
                        b.getCheckInDate(), b.getCheckOutDate()), 1);
                w.printf("%d,%s,%d,%s,%s,%s,%d,%.2f,%.2f%n",
                        b.getBookingId(), b.getCustomerName(),
                        b.getRoomNumber(), b.getRoomType(),
                        b.getCheckInDate(), b.getCheckOutDate(),
                        n, b.getPricePerDay(), b.getTotalAmount());
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Export Successful");
            ok.setHeaderText(null);
            ok.setContentText("Saved to: " + f.getAbsolutePath());
            ok.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private Label lbl(String t, int s, String c) {
        Label l = new Label(t);
        l.setFont(Font.font("System", FontWeight.BOLD, s));
        l.setStyle("-fx-text-fill:" + c + ";");
        return l;
    }
}