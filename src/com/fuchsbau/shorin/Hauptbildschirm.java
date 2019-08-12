package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Charakters.Charaktererstellung;
import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class Hauptbildschirm {
    private BorderPane pane;
    private String patchnotes = "Shorin Patch : 0.1";

    public Hauptbildschirm() {
        VBox top = new VBox();
        top.setPadding(new Insets(10, 0, 0, 0));
        top.setAlignment(Pos.CENTER);
        Label patch = new Label();
        top.getChildren().add(patch);

        HBox boxone = new HBox();
        boxone.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        boxone.setSpacing(10);
        boxone.setPrefHeight(100);
        boxone.setAlignment(Pos.CENTER);

        Button start = new Button("Game Start");
        start.setOnMouseClicked(event -> {



            Main.getStage().setTitle("Characktererstellung");
            Main.getStage().setScene(new Scene(new Charaktererstellung().getPane()));

        });
        start.setPrefWidth(GameOptionen.buttonwidth);
        Button laden = new Button("Load Game");
        laden.setPrefWidth(GameOptionen.buttonwidth);
        Button optionen = new Button("Optionen");
        optionen.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin - Optionen");
            Main.getStage().setScene(new Scene(new GameOptionen().getPane()));
            Main.getStage().setResizable(false);
        });
        optionen.setPrefWidth(GameOptionen.buttonwidth);

        boxone.getChildren().addAll(start, laden, optionen);

        patch.setText(patchnotes);


        pane = SceneBuilder.buildBorderPane(top, null, null, boxone);
        pane.setBackground(GameOptionen.hintergrund);

    }


    public BorderPane getPane() {
        return pane = new Hauptbildschirm().pane;
    }

    public Scene getOptions() {
        return new Scene(new GameOptionen().getPane());
    }

}
