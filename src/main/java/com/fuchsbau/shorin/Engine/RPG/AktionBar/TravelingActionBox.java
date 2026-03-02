package com.fuchsbau.shorin.Engine.RPG.AktionBar;

import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class TravelingActionBox implements ActionRow {
    private SceneBuilder sb = SceneBuilder.getSceneBuilder();

    @Override
    public void build(VBox container, Scene scene) {

        // REIHE 1  –  1-6 Schnellaktionen
        HBox row1 = buildRow(
                sb.makeActionButton("1", "Erkunden"),
                sb.makeActionButton("2", "Sprechen"),
                sb.makeActionButton("3", "Rasten"),
                sb.makeActionButton("4", "Lager"),
                sb.makeActionButton("5", "Karte"),
                sb.makeActionButton("6", "Log")
        );

        // REIHE 2  –  Q, E + weitere
        HBox row2 = buildRow(
                sb.makeActionButton("Q", "Interagieren"),
                sb.makeActionButton("W", "↑"),
                sb.makeActionButton("E", "Untersuchen"),
                sb.makeActionButton("R", "—"),
                sb.makeActionButton("T", "—"),
                sb.makeActionButton("Z", "—")
        );

        // REIHE 3  –  Bewegung WASD + Kontext
        HBox row3 = buildRow(
                sb.makeActionButton("A", "←"),
                sb.makeActionButton("S", "↓"),
                sb.makeActionButton("D", "→"),
                sb.makeActionButton("F", "—"),
                sb.makeActionButton("G", "—"),
                sb.makeActionButton("H", "—")
        );

        container.getChildren().addAll(row1, row2, row3);

        // Keyboard
        if (scene != null) {
            scene.setOnKeyPressed(e -> handleKey(e.getCode()));
        }
    }

    private HBox buildRow(Button... buttons) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        for (Button btn : buttons) {
            btn.setPrefWidth(0); // lässt HGrow arbeiten
            HBox.setHgrow(btn, Priority.ALWAYS);
            row.getChildren().add(btn);
        }
        return row;
    }

    private void handleKey(KeyCode code) {
        switch (code) {
            case DIGIT1 -> System.out.println("Erkunden");
            case DIGIT2 -> System.out.println("Sprechen");
            case DIGIT3 -> System.out.println("Rasten");
            case W -> System.out.println("Move N");
            case A -> System.out.println("Move W");
            case S -> System.out.println("Move S");
            case D -> System.out.println("Move E");
            case Q -> System.out.println("Interagieren");
            case E -> System.out.println("Untersuchen");
            // TODO: echte Handler
        }
    }
}
