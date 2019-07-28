package com.eppersindustries.shorin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private String patchnotes = "Shorin Patch:0.1";
    private Insets buttonpadding = new Insets(10, 10, 10, 10);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Shorin");

        BorderPane startbildschirm = new BorderPane();
        startbildschirm.setPrefHeight(700);
        startbildschirm.setPrefWidth(900);
        Label patch = new Label();
        startbildschirm.setTop(patch);

        HBox boxone = new HBox();
        boxone.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        boxone.setSpacing(10);
        boxone.setPrefHeight(100);
        boxone.alignmentProperty();

        Button start = new Button("Game Start");
        Button laden = new Button("Load Game");
        Button optionen = new Button("Optionen");

        boxone.getChildren().addAll(start, laden, optionen);

        startbildschirm.setBottom(boxone);
        patch.setText(patchnotes);

        stage.setScene(new Scene(startbildschirm));
        stage.show();
    }
}
