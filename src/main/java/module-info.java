module org.example.notes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.notes to javafx.fxml;
    exports org.example.notes;
}