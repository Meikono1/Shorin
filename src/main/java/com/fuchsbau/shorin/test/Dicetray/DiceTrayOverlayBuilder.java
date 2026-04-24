package com.fuchsbau.shorin.test.Dicetray;

import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static javafx.scene.SceneAntialiasing.BALANCED;

public class DiceTrayOverlayBuilder {
    private final StackPane diceTrayPane = new StackPane();

    private final Canvas backgroundCanvas = new Canvas(320, 220);
    private final Canvas trayCanvas = new Canvas(320, 220);

    private final Group world = new Group();
    private final SubScene dice3dScene = new SubScene(world, 320, 220, true, BALANCED);

    public DiceTrayOverlayBuilder() {
        build();
    }

    private void build() {
        diceTrayPane.setPickOnBounds(false);

        diceTrayPane.setPrefSize(320, 220);
        diceTrayPane.setMinSize(200, 140);
        diceTrayPane.setMaxSize(420, 300);

        AnchorPane.setRightAnchor(diceTrayPane, 20.0);
        AnchorPane.setBottomAnchor(diceTrayPane, 20.0);

        dice3dScene.setFill(Color.TRANSPARENT);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateX(0);
        camera.setTranslateY(-500);
        camera.setTranslateZ(-420);
        camera.setRotationAxis(javafx.scene.transform.Rotate.X_AXIS);
        camera.setRotate(-55);

        dice3dScene.setCamera(camera);

        backgroundCanvas.widthProperty().bind(diceTrayPane.widthProperty());
        backgroundCanvas.heightProperty().bind(diceTrayPane.heightProperty());

        trayCanvas.widthProperty().bind(diceTrayPane.widthProperty());
        trayCanvas.heightProperty().bind(diceTrayPane.heightProperty());

        dice3dScene.widthProperty().bind(diceTrayPane.widthProperty());
        dice3dScene.heightProperty().bind(diceTrayPane.heightProperty());

        diceTrayPane.getChildren().addAll(backgroundCanvas, trayCanvas, dice3dScene);

        backgroundCanvas.widthProperty().addListener((obs, o, n) -> drawBackground(backgroundCanvas));
        backgroundCanvas.heightProperty().addListener((obs, o, n) -> drawBackground(backgroundCanvas));

        trayCanvas.widthProperty().addListener((obs, o, n) -> drawTray(trayCanvas));
        trayCanvas.heightProperty().addListener((obs, o, n) -> drawTray(trayCanvas));

        drawBackground(backgroundCanvas);
        drawTray(trayCanvas);

    }

    public Group getWorld() {
        return world;
    }

    public StackPane getDiceTrayPane() {
        return diceTrayPane;
    }

    public void attachBottomRight(AnchorPane parent,
                                  double percentSize,
                                  double minW, double minH,
                                  double maxW, double maxH) {
        parent.getChildren().add(diceTrayPane);

        AnchorPane.setRightAnchor(diceTrayPane, 10.0);
        AnchorPane.setBottomAnchor(diceTrayPane, 10.0);

        diceTrayPane.prefWidthProperty().bind(parent.widthProperty().multiply(percentSize));
        diceTrayPane.prefHeightProperty().bind(parent.heightProperty().multiply(percentSize));

        diceTrayPane.setMinSize(minW, minH);
        diceTrayPane.setMaxSize(maxW, maxH);
    }

    private static void drawBackground(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.clearRect(0, 0, w, h);
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

        g.setFill(Color.rgb(70, 52, 38, 0.95));
        g.fillRoundRect(trayX, trayY, trayW, trayH, 20, 20);

        g.setStroke(Color.rgb(120, 90, 60, 1.0));
        g.setLineWidth(4);
        g.strokeRoundRect(trayX, trayY, trayW, trayH, 20, 20);

        g.setStroke(Color.rgb(35, 25, 18, 0.7));
        g.setLineWidth(2);
        g.strokeRoundRect(trayX + 3, trayY + 3, trayW - 6, trayH - 6, 16, 16);
    }
}