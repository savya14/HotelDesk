module com.hotel.hoteldesk {
    requires javafx.controls;

    opens com.hotel.hoteldesk to javafx.graphics;
    opens com.hotel.hoteldesk.model to javafx.base;

    exports com.hotel.hoteldesk;
}