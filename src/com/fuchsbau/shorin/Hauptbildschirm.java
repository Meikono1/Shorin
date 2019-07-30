package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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
            VBox charakter = new VBox();
            VBox controls = new VBox();
            HBox rowone = new HBox();
            rowone.setSpacing(10);
            rowone.setAlignment(Pos.CENTER);

            controls.getChildren().add(rowone);
            Button one = new Button();
            one.setText("noch offen");
            rowone.getChildren().add(one);

            Label name = new Label();
            name.setText("Dein Name");
            name.prefHeight(80);


            ImageView inventory = new ImageView("/images/plastic_bag.png");
            inventory.setFitHeight(100);
            inventory.setFitWidth(100);
            charakter.getChildren().add(inventory);

            BorderPane charaktererstellung = SceneBuilder.buildBorderPane(null, null, charakter, controls);
            Main.getStage().setTitle("Characktererstellung");
            Main.getStage().setScene(new Scene(charaktererstellung));

        });
        Button laden = new Button("Load Game");
        Button optionen = new Button("Optionen");
        optionen.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin - Optionen");
            Main.getStage().setScene(new Scene(new GameOptionen().getPane()));
            Main.getStage().setResizable(false);
        });

        boxone.getChildren().addAll(start, laden, optionen);

        patch.setText(patchnotes);


        pane = SceneBuilder.buildBorderPane(top, null, null, boxone);

    }


    public BorderPane getPane() {
        return pane = new Hauptbildschirm().pane;
    }

    public Scene getOptions() {
        return new Scene(new GameOptionen().getPane());
    }

}
