package com.fuchsbau.shorin.Optionen;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Hauptbildschirm;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;


public class GameOptionen {

    public static double width = 1280;
    public static double height = 720;
    public static double buttonwidth = 160;
    public static double itembuttonwidth = 100;
    public static double imagewidth = 170;
    public static double imageheight = 170;
    public static int textsize = 21;
    public static Insets padding = new Insets(5, 10, 5, 10);
    public static Background hintergrund = new Background(new BackgroundFill(Color.valueOf("13141c"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Background testbackground = new Background(new BackgroundFill(Color.valueOf("23423c"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Background rowHintergrund = new Background(new BackgroundFill(Color.valueOf("0c0c12"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Paint player = Paint.valueOf("2c5f78");
    public static Paint timestamp = Paint.valueOf("734b4b");
    public static Paint highlightBlue = Paint.valueOf("4b6673");
    public static Paint missionDescription = Paint.valueOf("638387");
    public static Paint armies = Paint.valueOf("6b6f48");
    public static Paint ortcolor = Paint.valueOf("409970");
    public static Paint goodPaint = Paint.valueOf("589214");
    public static int inventarbuttonwidth = 450;


    private Scene scene;


    private void makePane(int stage) {

        ScrollPane optionsfenster = new ScrollPane();
        optionsfenster.setBackground(hintergrund);
        optionsfenster.setFitToWidth(true);
        optionsfenster.setFitToHeight(true);
        optionsfenster.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        optionsfenster.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        //VBox inside Scrollpane
        VBox center = new VBox();
        center.setBackground(hintergrund);
        center.setAlignment(Pos.CENTER);
        center.setPadding(padding);
        optionsfenster.setContent(center);

        //Option 1
        Button res1280 = makeButton("1280");
        res1280.setOnMouseClicked(event -> {
            width = 1280;
            height = 720;
            imagewidth = 170;
            imageheight = 170;
            buttonwidth = 160;
            textsize = 21;
            inventarbuttonwidth = 450;
            Main.getStage().setScene(Game.getInstance().optionen.getScene(stage));
        });

        HBox optionone = makeOption("Resolution", res1280);
        center.getChildren().add(optionone);

        //Option 3
        Button res1600 = makeButton("1600");
        res1600.setOnMouseClicked(event -> {
            width = 1600;
            height = 1280;
            imagewidth = 230;
            imageheight = 230;
            buttonwidth = 190;
            textsize = 23;
            inventarbuttonwidth = 700;
            Main.getStage().setScene(Game.getInstance().optionen.getScene(stage));
        });
        HBox optionnthree = makeOption("Resolution", res1600);
        center.getChildren().add(optionnthree);

        //Option 2
        Button res1900 = makeButton("1900");
        res1900.setOnMouseClicked(event -> {
            width = 1900;
            height = 1080;
            imagewidth = 270;
            imageheight = 270;
            buttonwidth = 220;
            textsize = 26;
            inventarbuttonwidth = 1050;
            Main.getStage().setScene(Game.getInstance().optionen.getScene(stage));
        });

        HBox optiontwo = makeOption("Resolution", res1900);
        center.getChildren().add(optiontwo);


        HBox buttongroup = SceneBuilder.makeButtonrow();
        buttongroup.setBackground(GameOptionen.rowHintergrund);
        buttongroup.setPrefHeight(100);

        Button back = SceneBuilder.makeButton();
        back.setText("Back");
        back.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin");
            if (stage == 0) {
                Main.getStage().setScene(new Hauptbildschirm().getScene(0));
            } else {
                Main.getStage().setScene(Game.getInstance().inventory.getScene());
            }
        });

        buttongroup.getChildren().setAll(back);

        BorderPane pane = new BorderPane();
        pane.setBottom(buttongroup);
        pane.setCenter(optionsfenster);

        scene = new Scene(pane);
    }

    private Button makeButton(String text) {
        Button back = new Button(text);
        back.setMinWidth(buttonwidth);
        back.setPadding(padding);


        return back;

    }

    private HBox makeOption(String option, Button button) {
        HBox back = new HBox();
        Text text = new Text(option);
        text.setFill(Paint.valueOf("ffffff"));
        text.minWidth(200);
        back.setBackground(rowHintergrund);
        back.setAlignment(Pos.CENTER);
        back.setSpacing(100);
        back.setPadding(padding);

        back.getChildren().addAll(text, button);

        return back;
    }


    public Scene getScene(int stage) {
        makePane(stage);
        return scene;
    }
}
