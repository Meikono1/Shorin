module com.fuchsbau.shorin {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;

    opens com.fuchsbau.shorin to javafx.fxml;
    exports com.fuchsbau.shorin;
}