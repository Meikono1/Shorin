package com.fuchsbau.shorin.Engine.Encounter.Widget;

import com.fuchsbau.shorin.Engine.Encounter.EncounterState;
import com.fuchsbau.shorin.Engine.Map.Token;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class InitiativeTrackerWidget implements EncounterWidget {

    @Override
    public String getId() {
        return "initiative-tracker";
    }

    @Override
    public Node build(EncounterState state) {
        VBox listBox = new VBox(3);
        listBox.setPadding(new Insets(4, 8, 4, 8));

        Label title = new Label("Initiative");
        title.setStyle("-fx-text-fill: #a0a0ff; -fx-font-weight: bold; -fx-font-size: 13px;");

        VBox container = new VBox(6, title, listBox);
        container.setPadding(new Insets(8));
        container.setStyle("""
                -fx-background-color: rgba(10,10,30,0.72);
                -fx-background-radius: 6;
                -fx-border-color: #333355;
                -fx-border-radius: 6;
                -fx-border-width: 1;
                """);
        container.setMinWidth(170);
        container.setMaxWidth(200);

        Runnable rebuild = () -> Platform.runLater(() -> {
            listBox.getChildren().clear();
            Token active = state.activeToken.get();

            for (Token t : state.initiative) {
                boolean isActive = t == active;
                String prefix = isActive ? "▶ " : "  ";
                String hpText = (t.npcBuild != null) ? "  HP " + t.npcBuild.hp : "";

                Label nameLabel = new Label(prefix + t.initiative + "  " + t.name);
                nameLabel.setStyle("-fx-text-fill: " + (isActive ? "#ffdd44" : "#cccccc") + "; -fx-font-size: 12px;");

                Label hpLabel = new Label(hpText);
                hpLabel.setStyle("-fx-text-fill: #88cc88; -fx-font-size: 11px; -fx-padding: 0 0 0 12;");

                VBox entry = new VBox(0, nameLabel, hpLabel);
                if (isActive) {
                    entry.setStyle("-fx-background-color: rgba(255,220,50,0.08); -fx-background-radius: 3;");
                }
                listBox.getChildren().add(entry);
            }
        });

        state.initiative.addListener((ListChangeListener<Token>) c -> rebuild.run());
        state.activeToken.addListener((obs, o, n) -> rebuild.run());
        rebuild.run();

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(340);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        return scroll;
    }
}