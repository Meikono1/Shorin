package com.fuchsbau.shorin;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class SceneBuilder {

    public static BorderPane buildBorderPane(double height, double width, Node top, Node right, Node left, Node buttom) {
        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(height);
        haupt.setPrefWidth(width);

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
