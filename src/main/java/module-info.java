module com.fuchsbau.shorin {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.logging;
    requires com.fasterxml.jackson.databind;


    exports com.fuchsbau.shorin.Races;
    exports com.fuchsbau.shorin.Races.Base;
    exports com.fuchsbau.shorin;
    opens com.fuchsbau.shorin to javafx.fxml;
    exports com.fuchsbau.shorin.Engine;
    opens com.fuchsbau.shorin.Engine to javafx.fxml;
    exports com.fuchsbau.shorin.Engine.Styler;
    opens com.fuchsbau.shorin.Engine.Styler to javafx.fxml;
    exports com.fuchsbau.shorin.Engine.RPG;
    opens com.fuchsbau.shorin.Engine.RPG to com.fasterxml.jackson.databind;

    exports com.fuchsbau.shorin.Engine.Editor.Module.Actions;

    exports com.fuchsbau.shorin.test;
    exports com.fuchsbau.shorin.Engine.Race;
    exports com.fuchsbau.shorin.Engine.Map.Core;
    exports com.fuchsbau.shorin.Engine.Paperdoll;
    opens com.fuchsbau.shorin.Engine.Paperdoll to com.fasterxml.jackson.databind;

    exports com.fuchsbau.shorin.Engine.System to com.fasterxml.jackson.databind;
    exports com.fuchsbau.shorin.Engine.Map;
    exports com.fuchsbau.shorin.Engine.Map.Core.Lighting;
    exports com.fuchsbau.shorin.Engine.Map.Core.Tiles;
    exports com.fuchsbau.shorin.Engine.Map.Core.Sound;
    exports com.fuchsbau.shorin.Engine.Map.Core.Walls;
}