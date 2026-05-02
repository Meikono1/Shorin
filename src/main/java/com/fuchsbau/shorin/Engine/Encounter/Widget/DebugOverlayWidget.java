package com.fuchsbau.shorin.Engine.Encounter.Widget;

import com.fuchsbau.shorin.Engine.Encounter.EncounterState;
import com.fuchsbau.shorin.Engine.Map.Token;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DebugOverlayWidget implements EncounterWidget {

    @Override
    public String getId() {
        return "debug-overlay";
    }

    @Override
    public Node build(EncounterState state) {
        // --- Labels ---
        Label roundLabel = new Label("Runde: –");
        Label tokenLabel = new Label("Aktiv: –");
        Label actionsLabel = new Label("Aktionen: –");
        Label hpLabel = new Label("HP: –");
        Label initLabel = new Label("Initiative-Einträge: 0");

        for (Label l : new Label[]{roundLabel, tokenLabel, actionsLabel, hpLabel, initLabel}) {
            l.setStyle("-fx-text-fill: #b0b0cc; -fx-font-size: 11px;");
        }

        Runnable refresh = () -> Platform.runLater(() -> {
            roundLabel.setText("Runde: " + state.round.get());
            initLabel.setText("Tokens: " + state.initiative.size());

            Token active = state.activeToken.get();
            if (active != null) {
                tokenLabel.setText("Aktiv: " + active.name);
                int used = state.actionsUsed.get();
                int max = active.maxActions;
                actionsLabel.setText("Aktionen: " + used + " / " + max);
                hpLabel.setText("HP: " + (active.npcBuild != null ? active.npcBuild.hp : "?"));
            } else {
                tokenLabel.setText("Aktiv: –");
                actionsLabel.setText("Aktionen: –");
                hpLabel.setText("HP: –");
            }
        });

        state.round.addListener((obs, o, n) -> refresh.run());
        state.activeToken.addListener((obs, o, n) -> refresh.run());
        state.actionsUsed.addListener((obs, o, n) -> refresh.run());
        state.initiative.addListener((javafx.collections.ListChangeListener<Token>) c -> refresh.run());
        refresh.run();

        // --- Schnellzugriff-Buttons ---
        Button startBtn = debugBtn("▶ Start", () -> {
            if (state.initiative.isEmpty()) return;
            state.round.set(1);
            state.actionsUsed.set(0);
            state.activeToken.set(state.initiative.getFirst());
        });

        Button nextBtn = debugBtn("⏭ Nächster", state::nextTurn);

        Button resetBtn = debugBtn("↺ Reset", () -> {
            state.round.set(0);
            state.actionsUsed.set(0);
            state.activeToken.set(null);
        });

        Button endBtn = debugBtn("■ Ende", () -> {
            state.round.set(0);
            state.actionsUsed.set(null == null ? 0 : 0);
            state.activeToken.set(null);
            state.initiative.clear();
        });

        HBox btnRow1 = new HBox(4, startBtn, nextBtn);
        HBox btnRow2 = new HBox(4, resetBtn, endBtn);

        // --- Titel ---
        Label title = new Label("DEBUG");
        title.setStyle("-fx-text-fill: #ff9933; -fx-font-weight: bold; -fx-font-size: 11px;");

        VBox panel = new VBox(4,
                title,
                new Separator(),
                roundLabel, initLabel, tokenLabel, actionsLabel, hpLabel,
                new Separator(),
                btnRow1, btnRow2
        );
        panel.setPadding(new Insets(7));
        panel.setMaxWidth(180);
        panel.setStyle("""
                -fx-background-color: rgba(10,5,5,0.80);
                -fx-background-radius: 6;
                -fx-border-color: #554422;
                -fx-border-radius: 6;
                -fx-border-width: 1;
                """);

        return panel;
    }

    private Button debugBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("""
                -fx-background-color: #2a2a3a;
                -fx-text-fill: #cccccc;
                -fx-font-size: 11px;
                -fx-padding: 3 6 3 6;
                -fx-background-radius: 4;
                """);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}