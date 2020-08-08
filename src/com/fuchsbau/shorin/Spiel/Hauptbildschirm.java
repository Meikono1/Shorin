package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.Spiel.Intro.Charaktererstellung;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Hauptbildschirm implements Saveble {
    private Charaktererstellung charaktererstellung = new Charaktererstellung();
    private Scene scene;
    private String patchnotes = "Shorin Patch : 0.1";
    int i = 0;
    int j = 0;

    private void makeScene() {
        VBox top = new VBox();
        top.setPadding(new Insets(10, 0, 0, 0));
        top.setAlignment(Pos.CENTER);
        Label patch = new Label();
        top.getChildren().add(patch);

        HBox boxone = new HBox();
        boxone.setBackground(GameOptionen.rowHintergrund);
        boxone.setSpacing(10);
        boxone.setPrefHeight(100);
        boxone.setAlignment(Pos.CENTER);

        Button start = new Button("Game Start");
        start.setOnMouseClicked(event -> {


            Main.getStage().setTitle("Charaktererstellung");
            Main.getStage().setScene(charaktererstellung.getScene(1));

        });
        start.setPrefWidth(GameOptionen.buttonwidth);

        Button laden = new Button("Load Game");
        laden.setPrefWidth(GameOptionen.buttonwidth);


        Button optionen = new Button("Optionen");
        optionen.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin - Optionen");
            Main.getStage().setScene(Game.getInstance().optionen.getScene(0));
        });
        optionen.setPrefWidth(GameOptionen.buttonwidth);

        boxone.getChildren().addAll(start, laden, optionen);

        patch.setText(patchnotes);
        patch.setTextFill(Paint.valueOf("868686"));


        BorderPane pane = SceneBuilder.buildBorderPane(top, null, null, boxone);
        pane.setBackground(GameOptionen.hintergrund);
        scene = new Scene(pane);

    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

}
