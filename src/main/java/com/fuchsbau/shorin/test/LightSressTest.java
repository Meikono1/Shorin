package com.fuchsbau.shorin.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Objects;

public class LightSressTest extends Application {

    private static final int W = 800, H = 600;

    @Override
    public void start(Stage stage) {
        String url = Objects.requireNonNull(getClass().getResource("/images/Worldmap/plyport.jpg")).toExternalForm();

        ImageView grayLayer = new ImageView(new Image(url));
        ColorAdjust gray = new ColorAdjust();
        gray.setSaturation(-1.0);
        grayLayer.setEffect(gray);

        ImageView colorLayer = new ImageView(new Image(url));

        TestLightMask mask = new TestLightMask(W, H);
        colorLayer.setClip(mask.getCanvas());

        Pane root = new Pane(grayLayer, colorLayer);
        root.setOnMouseMoved(e -> mask.moveTo(e.getX(), e.getY()));

        // Bilder binden
        grayLayer.fitWidthProperty().bind(root.widthProperty());
        grayLayer.fitHeightProperty().bind(root.heightProperty());
        colorLayer.fitWidthProperty().bind(root.widthProperty());
        colorLayer.fitHeightProperty().bind(root.heightProperty());

        // Maske bei Größenänderung neu erstellen
        root.widthProperty().addListener((obs, o, n) ->
                mask.resize(n.doubleValue(), root.getHeight()));
        root.heightProperty().addListener((obs, o, n) ->
                mask.resize(root.getWidth(), n.doubleValue()));

        Scene scene = new Scene(root, W, H);
        stage.setScene(scene);
        stage.setFullScreen(true);   // oder stage.setMaximized(true)
        stage.show();
    }
}


