package com.fuchsbau.shorin.Engine.Options;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.MainScreen;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.SceneBuilder;
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


public class GameOptions {

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
    public static Background rowHintergrundTrans40 = new Background(new BackgroundFill(Color.valueOf("0c0c12").deriveColor(0, 1, 1, 0.40), CornerRadii.EMPTY, Insets.EMPTY));
    public static Paint player = Paint.valueOf("3094ff");
    public static Paint timestamp = Paint.valueOf("734b4b");
    public static Paint highlightBlue = Paint.valueOf("4b6673");
    public static Paint missionDescription = Paint.valueOf("638387");
    public static Paint armies = Paint.valueOf("6b6f48");
    public static Paint cityColor = Paint.valueOf("409970");
    public static Paint raceColor = Paint.valueOf("472db2");
    public static Paint goodPaint = Paint.valueOf("589214");
    public static Paint goldPaint = Paint.valueOf("878707");
    public static String currency = "Gold";
    public static String currentLanguage = "EN";
    public static Paint textColour = Paint.valueOf("#e8e8e8");
    public static String UserCssFilename = "user-style.css";

    private Scene scene;

    public static void toggleDelete() {
        delete = !delete;
    }


    private void makePane(int stage) {
        ScrollPane optionWindow = new ScrollPane();
        optionWindow.setBackground(hintergrund);
        optionWindow.setFitToWidth(true);
        optionWindow.setFitToHeight(true);
        optionWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        optionWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        //VBox inside Scrollpane
        VBox center = new VBox();
        center.setBackground(hintergrund);
        center.setAlignment(Pos.CENTER);
        center.setPadding(padding);
        optionWindow.setContent(center);

        Button back = SceneBuilder.getSceneBuilder().makeButton(1, "Back");
        back.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin");
            if (stage == 0) {
                Main.getStage().setScene(new MainScreen().getScene(0));
            } else {
                Main.getStage().setScene(Game.getInstance().inventory.getScene());
            }
        });
        SceneBuilder.getSceneBuilder().addButton(back, 1);

        Button save = SceneBuilder.getSceneBuilder().makeButton(1, "Save game");
        if (stage != 0) {
            save.setOnMouseClicked(event -> {
                File file = new File("save01.txt");
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(Game.getInstance().saveEverything());
                    fileWriter.close();
                    file.createNewFile();
                } catch (IOException e) {
                    FileLogger.getLogger().severe(e.toString());
                }
            });
        }
        SceneBuilder.getSceneBuilder().addButton(save, 1);

        scene = SceneBuilder.getSceneBuilder().makeGameOption(optionWindow);
    }

    // TODO: 22.01.2023 Ausbauen ?, erweitern ?, umbauen ?
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
        SceneBuilder.getSceneBuilder().resetButtonrows();
        makePane(stage);
        return scene;
    }

    public String save() {
        return new StringBuilder().toString();
    }
}
