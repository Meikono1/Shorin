package com.fuchsbau.shorin.test;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class TestLightMask {

    private double lastCx, lastCy;

    private final Canvas canvas;
    private final GraphicsContext gc;
    private static final double RADIUS = 250.0;

    public TestLightMask(double w, double h) {
        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();
    }

    public void moveTo(double cx, double cy) {
        lastCx = cx;
        lastCy = cy;
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Alles transparent → colorLayer komplett unsichtbar
        gc.clearRect(0, 0, w, h);

        // Lichtkreis weiß/opak → colorLayer dort sichtbar
        RadialGradient light = new RadialGradient(
                0, 0, cx, cy, RADIUS,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 255, 255, 1.0)),  // Zentrum: sichtbar
                new Stop(0.6, Color.rgb(255, 255, 255, 1.0)),  // Plateau
                new Stop(1.0, Color.rgb(255, 255, 255, 0.0))   // Rand: weich auslaufen
        );

        gc.setFill(light);
        gc.fillOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);
    }

    public void resize(double w, double h) {
        canvas.setWidth(w);
        canvas.setHeight(h);
        moveTo(lastCx, lastCy);  // neu zeichnen mit aktueller Position
    }

    public Canvas getCanvas() { return canvas; }
}