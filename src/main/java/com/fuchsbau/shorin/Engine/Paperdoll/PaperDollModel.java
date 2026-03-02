package com.fuchsbau.shorin.Engine.Paperdoll;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PaperDollModel {

    private static Stage window;

    /** Öffnet das Fenster wenn geschlossen, schließt wenn offen. */
    public static void toggle() {
        if (window != null && window.isShowing()) {
            window.hide();
        } else {
            open();
        }
    }

    public static void open() {
        if (window == null) {
            window = new Stage();
            window.setTitle("Paperdoll");
            window.initStyle(StageStyle.DECORATED);
            window.setResizable(true);
            window.setAlwaysOnTop(false);
        }

        window.setScene(buildScene());
        window.setWidth(460);
        window.setHeight(680);
        window.show();
    }

    public static void close() {
        if (window != null) window.hide();
    }

    // -------------------------------------------------------------------------

    private static Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setBackground(GameOptions.hintergrund);
        root.setPadding(new Insets(12));

        // Haupt-Paperdoll-Bereich (Platzhalter)
        Region doll = new Region();
        doll.setBackground(new Background(new BackgroundFill(
                Color.rgb(25, 25, 40), new CornerRadii(8), Insets.EMPTY)));

        Label hint = new Label("[ Paperdoll-Modell ]\n\nHier erscheint später\ndas animierte Charakter-Modell\nmit Ausrüstungs-Slots.");
        hint.setTextFill(Color.LIGHTGRAY);
        hint.setAlignment(Pos.CENTER);
        hint.setStyle("-fx-font-size: 13px;");

        StackPane dollBox = new StackPane(doll, hint);
        dollBox.setPrefSize(300, 500);

        // Gear-Slot-Leiste rechts (Platzhalter)
        VBox slots = new VBox(8);
        slots.setPadding(new Insets(0, 0, 0, 12));
        slots.setPrefWidth(120);
        for (String slot : new String[]{"Head", "Body", "Arms", "Legs", "Boots", "Weapon", "Off-Hand"}) {
            Label l = new Label(slot + ":\n[ leer ]");
            l.setTextFill(Color.GRAY);
            l.setStyle("-fx-font-size: 11px; -fx-border-color: #333350; -fx-border-width: 1; -fx-padding: 4;");
            l.setMaxWidth(Double.MAX_VALUE);
            slots.getChildren().add(l);
        }

        HBox content = new HBox(dollBox, slots);
        content.setAlignment(Pos.CENTER);

        root.setCenter(content);

        Scene scene = new Scene(root);
        String css = CSSLoader.resolveUserOrBackupCSS();
        if (css != null) scene.getStylesheets().add(css);
        return scene;
    }
}