package com.fuchsbau.shorin.Optionen;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Mainscreen;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class GameOption {

    public static boolean delete;
    public static double width = 1500;
    public static double height = 900;
    public static double buttonwidth = 160;
    public static double itembuttonwidth = 100;
    public static double imagewidth = 220;
    public static double imageheight = 220;
    public static int textsize = 25;
    public static Insets padding = new Insets(5, 10, 5, 10);
    public static Background hintergrund = new Background(new BackgroundFill(Color.valueOf("13141c"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Background testbackground = new Background(new BackgroundFill(Color.valueOf("23423c"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Background rowHintergrund = new Background(new BackgroundFill(Color.valueOf("0c0c12"), CornerRadii.EMPTY, Insets.EMPTY));
    public static Paint player = Paint.valueOf("3094ff");
    public static Paint timestamp = Paint.valueOf("734b4b");
    public static Paint highlightBlue = Paint.valueOf("4b6673");
    public static Paint missionDescription = Paint.valueOf("638387");
    public static Paint armies = Paint.valueOf("6b6f48");
    public static Paint ortcolor = Paint.valueOf("409970");
    public static Paint goodPaint = Paint.valueOf("589214");
    public static Paint goldPaint = Paint.valueOf("878707");
    public static String currencyname = "Gold";

    private Scene scene;

    public static void toggleDelete() {
        delete = !delete;
    }


    private void makePane(int stage) {
        NumberBinding binding;

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


        HBox buttongroup = SceneBuilder.makeButtonrow();
        buttongroup.setBackground(GameOption.rowHintergrund);
        buttongroup.setPrefHeight(100);

        Button back = SceneBuilder.makeButton(buttongroup);
        back.setText("Back");
        back.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin");
            if (stage == 0) {
                Main.getStage().setScene(new Mainscreen().getScene(0));
            } else {
                Main.getStage().setScene(Game.getInstance().inventory.getScene());
            }
        });

        Button save = SceneBuilder.makeButton(buttongroup);
        if (stage != 0) {
            save.setText("Save game");
            save.setOnMouseClicked(event -> {
                File file = new File("save01.txt");
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(Game.getInstance().saveEverything());
                    fileWriter.close();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        buttongroup.getChildren().setAll(back, save);

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

    public String save() {
        return new StringBuilder().toString();
    }
}
