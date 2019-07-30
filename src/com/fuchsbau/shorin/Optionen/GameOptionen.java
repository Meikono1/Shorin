package com.fuchsbau.shorin.Optionen;

import com.fuchsbau.shorin.Hauptbildschirm;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.SceneBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;


public class GameOptionen {

    public static double width = 1280;
    public static double height = 720;
    private static Insets padding = new Insets(10, 10, 10, 10);
    private BorderPane pane;


    public GameOptionen() {


        ScrollPane optionsfenster = new ScrollPane();
        optionsfenster.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        optionsfenster.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        optionsfenster.setMinHeight(500);

        VBox center = new VBox();
        center.setBackground(new Background(new BackgroundFill(Color.BLANCHEDALMOND, CornerRadii.EMPTY, Insets.EMPTY)));
        center.setPadding(padding);
        optionsfenster.setContent(center);

        HBox optionone = new HBox(10);
        optionone.setBackground(new Background(new BackgroundFill(Color.FUCHSIA, CornerRadii.EMPTY, Insets.EMPTY)));
        optionone.setAlignment(Pos.CENTER);
        center.getChildren().add(optionone);

        HBox optiontwo = new HBox(10);
        optiontwo.setPadding(padding);
        optiontwo.setBackground(new Background(new BackgroundFill(Color.FUCHSIA, CornerRadii.EMPTY, Insets.EMPTY)));
        center.getChildren().add(optiontwo);

        Label resonel = new Label();
        resonel.setPadding(padding);
        resonel.setText("Resolution");

        Button resoneb = new Button();
        resoneb.setText("720p");
        resoneb.setPadding(padding);
        resoneb.setOnMouseClicked(event -> {
            GameOptionen.width = 1280;
            GameOptionen.height = 720;
            Main.getStage().setScene(new Scene(new GameOptionen().getPane()));
        });
        Label restwol = new Label();
        restwol.setPadding(padding);
        restwol.setText("Resolution");

        Button restwob = new Button();
        restwob.setText("1080p");
        restwob.setPadding(padding);
        restwob.setOnMouseClicked(event -> {
            GameOptionen.width = 1900;
            GameOptionen.height = 1080;
            Main.getStage().setScene(new Scene(new GameOptionen().getPane()));
        });


        optiontwo.getChildren().addAll(restwol, restwob);
        optionone.getChildren().addAll(resonel, resoneb);

        HBox buttongroup = new HBox();
        buttongroup.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        buttongroup.setSpacing(10);
        buttongroup.setPrefHeight(100);
        buttongroup.setAlignment(Pos.CENTER);

        Button back = new Button("Zurück");
        back.setOnMouseClicked(event -> {

            Main.getStage().setScene(new Scene(new Hauptbildschirm().getPane()));
            Main.getStage().setTitle("Shorin");

        });

        buttongroup.getChildren().setAll(back);


        pane = SceneBuilder.buildBorderPane(null, null, null, buttongroup);
        pane.setCenter(optionsfenster);
    }


    public Pane getPane() {
        return pane;
    }
}
