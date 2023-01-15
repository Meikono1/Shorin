package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.Spiel.Intro.Creation;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class Mainscreen implements Saveble {
    private final Creation creation = new Creation(true);
    private Scene scene;
    private final String patchnotes = "Shorin Patch : 0.1";

    private void makeScene() {
        VBox top = new VBox();
        top.setPadding(new Insets(10, 0, 0, 0));
        top.setAlignment(Pos.CENTER);
        Label patch = new Label();
        top.getChildren().add(patch);

        HBox boxone = new HBox();
        boxone.setBackground(GameOption.rowHintergrund);
        boxone.setSpacing(10);
        boxone.setPrefHeight(100);
        boxone.setAlignment(Pos.CENTER);

        Button start = new Button("New Game");
        start.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Charakter creation");
            Main.getStage().setScene(creation.getScene(1));
        });
        start.prefWidthProperty().bind(Bindings.divide(boxone.widthProperty(), 5));

        Button laden = new Button("Load Game");
        laden.prefWidthProperty().bind(Bindings.divide(boxone.widthProperty(), 5));


        Button optionen = new Button("Options");
        optionen.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin - Options");
            Main.getStage().setScene(Game.getInstance().optionen.getScene(0));
        });
        optionen.prefWidthProperty().bind(Bindings.divide(boxone.widthProperty(), 5));

        boxone.getChildren().addAll(start, laden, optionen);

        patch.setText(patchnotes);
        patch.setTextFill(Paint.valueOf("868686"));


        BorderPane pane = SceneBuilder.buildBorderPane(top, null, null, boxone);
        pane.setBackground(GameOption.hintergrund);
        scene = new Scene(pane);
    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }

}
