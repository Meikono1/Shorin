package com.eppersindustries.shorin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;


public class Scenecreator {


    BorderPane startbildschirm = new BorderPane();
        startbildschirm.setPrefHeight(700);
        startbildschirm.setPrefWidth(900);

    VBox top = new VBox();
        top.setPadding(new
    Label patch = new Label(););
        top.setAlignment(Pos.CENTER);
    HBox boxone = new HBox();
        top.getChildren().

    Insets(10,0,0,0)
        startbildschirm.setTop(top);

    add(patch);
        boxone.setBackground(new

    Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        boxone.setSpacing(10);
        boxone.setPrefHeight(100);
        boxone.setAlignment(Pos.CENTER);
}
