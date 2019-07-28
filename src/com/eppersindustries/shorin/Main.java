package com.eppersindustries.shorin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        Button laden = new Button("Load Game");
        Button optionen = new Button("Optionen");
        optionen.setOnMouseClicked(event -> {
            Parent options = createOptionsWindow();
            stage.setTitle("Shorin - Optionen");
            stage.setScene(new Scene(options));
        });

        boxone.getChildren().addAll(start, laden, optionen);

        startbildschirm.setBottom(boxone);
        patch.setText(patchnotes);

        stage.setScene(new Scene(startbildschirm));
        stage.show();
    }

    private Parent createOptionsWindow() {
        ScrollPane optionsfenster = new ScrollPane();
        optionsfenster.setPrefHeight(700);
        optionsfenster.setPrefWidth(900);
        return optionsfenster;
    }
}
