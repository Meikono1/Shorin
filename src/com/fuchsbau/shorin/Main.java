package com.fuchsbau.shorin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private String patchnotes = "Shorin Patch : 0.1";
    private Insets buttonpadding = new Insets(10, 10, 10, 10);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Shorin");


        BorderPane startbildschirm = new BorderPane();
        startbildschirm.setPrefHeight(700);
        startbildschirm.setPrefWidth(900);

        VBox top = new VBox();
        top.setPadding(new Insets(10, 0, 0, 0));
        top.setAlignment(Pos.CENTER);
        Label patch = new Label();
        top.getChildren().add(patch);
        startbildschirm.setTop(top);

        HBox boxone = new HBox();
        boxone.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        boxone.setSpacing(10);
        boxone.setPrefHeight(100);
        boxone.setAlignment(Pos.CENTER);

        Button start = new Button("Game Start");
        start.setOnMouseClicked(event -> {
            BorderPane charaktererstellung = new BorderPane();
            charaktererstellung.setPrefWidth(900);
            charaktererstellung.setPrefHeight(700);
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

            ImageView inventory = new ImageView("/images/plastic_bag.png");
            inventory.setFitHeight(100);
            inventory.setFitWidth(100);
            charakter.getChildren().add(inventory);

            charaktererstellung.setLeft(charakter);
            charaktererstellung.setBottom(controls);
            stage.setTitle("Characktererstellung");
            stage.setScene(new Scene(charaktererstellung));

        });
        Button laden = new Button("Load Game");
        Button optionen = new Button("Optionen");
        optionen.setOnMouseClicked(event -> {
            Parent options = Options.createOptionsWindow();
            stage.setTitle("Shorin - Optionen");
            stage.setScene(new Scene(options));
            stage.setResizable(false);
        });

        boxone.getChildren().addAll(start, laden, optionen);

        startbildschirm.setBottom(boxone);
        patch.setText(patchnotes);

        stage.setScene(new Scene(startbildschirm));
        stage.show();
    }


}
