module com.fuchsbau.shorin {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.logging;

    opens com.fuchsbau.shorin to javafx.fxml;
    exports com.fuchsbau.shorin;
    exports com.fuchsbau.shorin.Engine;
    opens com.fuchsbau.shorin.Engine to javafx.fxml;
    exports com.fuchsbau.shorin.Engine.Styler;
    opens com.fuchsbau.shorin.Engine.Styler to javafx.fxml;
}