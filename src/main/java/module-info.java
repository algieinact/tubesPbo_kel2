module com.pboass3 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.pboass3 to javafx.fxml;
    exports com.pboass3;
}
