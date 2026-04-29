package com.fuchsbau.shorin.Engine.Encounter;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class EncounterTransition {

    public enum Direction {
        FROM_RIGHT, FROM_LEFT, FROM_TOP, FROM_BOTTOM, FADE, NONE
    }

    private static final Duration DURATION = Duration.millis(380);

    public static void playIn(Node node, Direction dir) {
        if (dir == Direction.NONE) return;

        node.setOpacity(0);
        node.setTranslateX(0);
        node.setTranslateY(0);

        TranslateTransition tt = new TranslateTransition(DURATION, node);
        FadeTransition ft = new FadeTransition(DURATION, node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        tt.setToX(0);
        tt.setToY(0);

        switch (dir) {
            case FROM_RIGHT -> {
                tt.setFromX(160);
                tt.setFromY(0);
            }
            case FROM_LEFT -> {
                tt.setFromX(-160);
                tt.setFromY(0);
            }
            case FROM_TOP -> {
                tt.setFromX(0);
                tt.setFromY(-100);
            }
            case FROM_BOTTOM -> {
                tt.setFromX(0);
                tt.setFromY(100);
            }
            case FADE -> {
                tt.setFromX(0);
                tt.setFromY(0);
            }
        }

        new ParallelTransition(tt, ft).play();
    }
}