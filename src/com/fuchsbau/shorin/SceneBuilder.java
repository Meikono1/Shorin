package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class SceneBuilder {

    public static BorderPane buildBorderPane(Node top, Node right, Node left, Node buttom) {
        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);

        if (top != null) {
            haupt.setTop(top);
        }
        if (buttom != null) {
            haupt.setBottom(buttom);
        }
        if (left != null) {
            haupt.setLeft(left);
        }
        if (right != null) {
            haupt.setRight(right);
        }

        return haupt;


    }

    public static BorderPane buildGameScene(HBox erste, HBox zweite, HBox dritte, Label text) {
        BorderPane haupt = new BorderPane();

        erste.setPadding(GameOptionen.padding);
        zweite.setPadding(GameOptionen.padding);
        dritte.setPadding(GameOptionen.padding);

        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);

        int lauf = erste.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            erste.getChildren().add(a);
        }

        lauf = zweite.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            zweite.getChildren().add(a);

        }

        lauf = dritte.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setMinWidth(GameOptionen.buttonwidth);
            dritte.getChildren().add(a);


        }

        VBox unten = new VBox();

        unten.getChildren().addAll(erste, zweite, dritte);

        VBox charakter = new VBox();

        Label name = new Label();
        name.setTextFill(Paint.valueOf("#7e7e7e"));
        name.setText("Dein Name");
        name.prefHeight(80);

        text.setTextFill(Paint.valueOf("7e7e7e"));


        ImageView ich = new ImageView("/images/meikonol.jpg");
        ich.setFitHeight(200);
        ich.setFitWidth(200);

        ImageView inventory = new ImageView("/images/plastic_bag.png");
        inventory.setFitHeight(200);
        inventory.setFitWidth(200);

        ImageView map = new ImageView("/images/map.jpg");
        map.setFitHeight(200);
        map.setFitWidth(200);

        charakter.getChildren().addAll(name, ich, inventory, map);

        haupt.setLeft(charakter);
        haupt.setBottom(unten);
        haupt.setCenter(text);

        haupt.setBackground(GameOptionen.hintergrund);
        return haupt;
    }


}
