package com.fuchsbau.shorin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class Options {

    private double with = 900;
    private double height = 700;


    public static Parent createOptionsWindow() {
        ScrollPane optionsfenster = new ScrollPane();
        optionsfenster.setPrefHeight(700);
        optionsfenster.setPrefWidth(900);
        optionsfenster.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        optionsfenster.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        VBox main = new VBox();
        main.setMinHeight(700);
        main.setMinWidth(900);
        main.setBackground(new Background(new BackgroundFill(Color.BLANCHEDALMOND, CornerRadii.EMPTY, Insets.EMPTY)));
        optionsfenster.setContent(main);

        HBox option = new HBox(30);
        option.setPrefWidth(600);
        option.setPrefHeight(150);
        option.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        main.getChildren().add(option);

        HBox buttongroup = new HBox(15);
        buttongroup.setAlignment(Pos.CENTER);
        Button back = new Button("Zurück");
        buttongroup.getChildren().add(back);
        main.getChildren().add(buttongroup);
        return optionsfenster;
    }
}
