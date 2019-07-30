package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

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

}
