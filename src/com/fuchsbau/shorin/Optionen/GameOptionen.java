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
import javafx.scene.paint.Paint;


public class GameOptionen {

    public static double width = 1280;
    public static double height = 720;
    public static double buttonwidth = 160;
    public static Insets padding = new Insets(5, 10, 5, 10);
    public static Background hintergrund = new Background(new BackgroundFill(Color.valueOf("13141c"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Paint timestamp = Paint.valueOf("734b4b");
    public static Paint highlightBlue = Paint.valueOf("4b6673");
    public static Paint missionDescription = Paint.valueOf("638387");
    public static Paint armies = Paint.valueOf("6b6f48");
    public static Paint ortcolor = Paint.valueOf("409970");

    private BorderPane pane;


    public GameOptionen() {


        ScrollPane optionsfenster = new ScrollPane();
        optionsfenster.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET, CornerRadii.EMPTY, Insets.EMPTY)));
        optionsfenster.setFitToWidth(true);
        optionsfenster.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        optionsfenster.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        VBox center = new VBox();
        center.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        center.setAlignment(Pos.CENTER);
        center.setPadding(padding);
        optionsfenster.setContent(center);

        HBox optionone = new HBox(10);
        optionone.setBackground(new Background(new BackgroundFill(Color.FUCHSIA, CornerRadii.EMPTY, Insets.EMPTY)));
        optionone.setAlignment(Pos.CENTER);
        optionone.setSpacing(100);
        center.getChildren().add(optionone);

        HBox optiontwo = new HBox(10);
        optiontwo.setPadding(padding);
        optiontwo.setAlignment(Pos.CENTER);
        optiontwo.setBackground(new Background(new BackgroundFill(Color.FUCHSIA, CornerRadii.EMPTY, Insets.EMPTY)));
        optiontwo.setSpacing(100);
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
            GameOptionen.buttonwidth = 160;
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
            GameOptionen.buttonwidth = 220;
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
