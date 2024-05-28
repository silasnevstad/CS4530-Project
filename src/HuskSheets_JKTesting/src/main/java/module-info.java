module com.example.husksheets_jktesting {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.husksheets_jktesting to javafx.fxml;
    exports com.example.husksheets_jktesting;
}