package com.fuchsbau.shorin.Engine.RPG.AktionBar;

import com.fuchsbau.shorin.Engine.RPG.Controls.Actionable;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * 18 Action-Slots in 3 Reihen à 6.
 * Slot-Belegung kommt als Map<Integer, Actionable> — leere Slots disabled.
 */
public class TravelingActionBox implements ActionRow {

    private static final int COLS = 6;
    private static final SceneBuilder sb = SceneBuilder.getSceneBuilder();

    private static final String[][] SLOTS = {
            {"1","Erkunden"}, {"2","Sprechen"},    {"3","Rasten"},
            {"4","Lager"},    {"5","Karte"},        {"6","Log"},
            {"Q","Interagieren"}, {"W","↑"},        {"E","Untersuchen"},
            {"R","—"},        {"T","—"},             {"Z","—"},
            {"A","←"},        {"S","↓"},             {"D","→"},
            {"F","—"},        {"G","—"},             {"H","—"}
    };

    @Override
    public void build(VBox container, Scene scene, Map<Integer, Actionable> actions) {
        Button[] buttons = new Button[SLOTS.length];

        for (int i = 0; i < SLOTS.length; i++) {
            int slot = i + 1;
            Button btn = sb.makeActionButton(SLOTS[i][0], SLOTS[i][1]);
            btn.setPrefWidth(0);
            HBox.setHgrow(btn, Priority.ALWAYS);

            Actionable action = actions.get(slot);
            if (action != null && action.canExecute()) {
                btn.setOnAction(e -> action.execute());
            } else if (action != null) {
                btn.setDisable(true);
                Tooltip.install(btn, new Tooltip(action.getDisabledReason()));
            } else {
                btn.setDisable(true);
            }

            buttons[i] = btn;
        }

        for (int row = 0; row < buttons.length / COLS; row++) {
            HBox hbox = new HBox(4);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setMaxWidth(Double.MAX_VALUE);
            for (int col = 0; col < COLS; col++) {
                hbox.getChildren().add(buttons[row * COLS + col]);
            }
            container.getChildren().add(hbox);
        }

        // @TODO Keyboard-Handler wenn WASD aktiv
    }
}