module ma.examen {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ma.examen to javafx.fxml;
    exports ma.examen;
    exports ma.examen.controller;
    opens ma.examen.controller to javafx.fxml;
}