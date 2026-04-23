package com.fuchsbau.shorin.test.Dicetray;

import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class DiceTrayOverlayBuilder {

    public static AnchorPane buildDiceOverlay() {
        AnchorPane root = new AnchorPane();

        // frei positionierbarer Container
        StackPane diceTrayPane = new StackPane();
        diceTrayPane.setPickOnBounds(false);

        // Größenbeispiel: später an Fenstergröße binden
        diceTrayPane.setPrefSize(320, 220);
        diceTrayPane.setMinSize(200, 140);
        diceTrayPane.setMaxSize(420, 300);

        // unten rechts anheften
        AnchorPane.setRightAnchor(diceTrayPane, 20.0);
        AnchorPane.setBottomAnchor(diceTrayPane, 20.0);

        // 1. ganz hinten: full background canvas
        Canvas backgroundCanvas = new Canvas(320, 220);

        // 2. darüber: Tray-Canvas
        Canvas trayCanvas = new Canvas(320, 220);

        // 3. darüber: 3D-SubScene
        Group world = new Group();
        SubScene dice3dScene = new SubScene(world, 320, 220, true, SceneAntialiasing.BALANCED);
        dice3dScene.setFill(Color.TRANSPARENT);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateX(0);
        camera.setTranslateY(-260);
        camera.setTranslateZ(-420);
        camera.setRotationAxis(javafx.scene.transform.Rotate.X_AXIS);
        camera.setRotate(-55);
        dice3dScene.setCamera(camera);

        // Größen an Pane binden
        backgroundCanvas.widthProperty().bind(diceTrayPane.widthProperty());
        backgroundCanvas.heightProperty().bind(diceTrayPane.heightProperty());

        trayCanvas.widthProperty().bind(diceTrayPane.widthProperty());
        trayCanvas.heightProperty().bind(diceTrayPane.heightProperty());

        dice3dScene.widthProperty().bind(diceTrayPane.widthProperty());
        dice3dScene.heightProperty().bind(diceTrayPane.heightProperty());

        // Reihenfolge: hinten -> vorne
        diceTrayPane.getChildren().addAll(backgroundCanvas, trayCanvas, dice3dScene);

        // Beispielrender
        backgroundCanvas.widthProperty().addListener((obs, o, n) -> drawBackground(backgroundCanvas));
        backgroundCanvas.heightProperty().addListener((obs, o, n) -> drawBackground(backgroundCanvas));

        trayCanvas.widthProperty().addListener((obs, o, n) -> drawTray(trayCanvas));
        trayCanvas.heightProperty().addListener((obs, o, n) -> drawTray(trayCanvas));

        drawBackground(backgroundCanvas);
        drawTray(trayCanvas);

        root.getChildren().add(diceTrayPane);
        return root;
    }

    private static void drawBackground(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.clearRect(0, 0, w, h);

        // kompletter Hintergrund
        g.setFill(Color.rgb(8, 10, 14, 0.55));
        g.fillRoundRect(0, 0, w, h, 18, 18);
    }

    private static void drawTray(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.clearRect(0, 0, w, h);

        double pad = 14;
        double trayX = pad;
        double trayY = pad;
        double trayW = w - pad * 2;
        double trayH = h - pad * 2;

        // Trayboden
        g.setFill(Color.rgb(70, 52, 38, 0.95));
        g.fillRoundRect(trayX, trayY, trayW, trayH, 20, 20);

        // Rand
        g.setStroke(Color.rgb(120, 90, 60, 1.0));
        g.setLineWidth(4);
        g.strokeRoundRect(trayX, trayY, trayW, trayH, 20, 20);

        // optionale innere Schattierung
        g.setStroke(Color.rgb(35, 25, 18, 0.7));
        g.setLineWidth(2);
        g.strokeRoundRect(trayX + 3, trayY + 3, trayW - 6, trayH - 6, 16, 16);
    }
}