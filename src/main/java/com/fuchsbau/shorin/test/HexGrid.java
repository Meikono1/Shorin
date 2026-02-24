package com.fuchsbau.shorin.test;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class HexGrid extends Application {

    // Grid
    private static final int COLS = 400;
    private static final int ROWS = 400;

    // Hex geometry (pointy-top), size is "radius" center->corner at zoom=1.0
    private static final double BASE_SIZE = 10.0;
    private static final double SQRT3 = Math.sqrt(3);

    // Camera
    private double camX = 0;   // world-space offset (pixels)
    private double camY = 0;
    private double zoom = 1.0;

    // Drag
    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    // HUD
    private long lastFpsTime = 0;
    private int frames = 0;
    private double fps = 0;
    private int lastDrawn = 0;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(1200, 800);
        GraphicsContext g = canvas.getGraphicsContext2D();

        // Resize with window
        StackPane root = new StackPane(canvas);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // Mouse: pan
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                dragging = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });
        canvas.setOnMouseReleased(e -> dragging = false);
        canvas.setOnMouseDragged(e -> {
            if (!dragging) return;
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            // Move camera opposite to mouse movement (screen->world)
            camX -= dx / zoom;
            camY -= dy / zoom;
        });

        // Mouse wheel: zoom (zoom around cursor)
        canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            double oldZoom = zoom;
            double factor = Math.pow(1.0015, e.getDeltaY()); // smooth
            zoom = clamp(zoom * factor, 0.2, 4.0);

            // Zoom around mouse position
            double mx = e.getX();
            double my = e.getY();
            double worldXBefore = screenToWorldX(mx, canvas.getWidth(), oldZoom);
            double worldYBefore = screenToWorldY(my, canvas.getHeight(), oldZoom);
            double worldXAfter = screenToWorldX(mx, canvas.getWidth(), zoom);
            double worldYAfter = screenToWorldY(my, canvas.getHeight(), zoom);

            camX += (worldXBefore - worldXAfter);
            camY += (worldYBefore - worldYAfter);
            e.consume();
        });

        // Render loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render(g, canvas.getWidth(), canvas.getHeight());
                updateFps(now);
            }
        };
        timer.start();

        stage.setTitle("Hex Canvas Stress Test (400x400) - Pan & Zoom");
        stage.setScene(new Scene(root, 1200, 800, Color.rgb(10, 10, 16)));
        stage.show();
    }

    private void render(GraphicsContext g, double w, double h) {
        // Clear
        g.setFill(Color.rgb(10, 10, 16));
        g.fillRect(0, 0, w, h);

        double size = BASE_SIZE * zoom;
        double hexW = SQRT3 * size;
        double hexH = 2 * size;
        double xSpacing = hexW;
        double ySpacing = 1.5 * size;

        // Viewport in world coords
        double worldLeft = camX;
        double worldTop = camY;
        double worldRight = camX + w / zoom;
        double worldBottom = camY + h / zoom;

        // Conservative culling bounds for odd-r layout:
        // centerX = col*hexW + (row%2)*hexW/2
        // centerY = row*ySpacing
        // Include half-hex margin so strokes/corners don’t pop.
        double marginX = hexW;
        double marginY = hexH;

        int rowMin = (int) Math.floor((worldTop - marginY) / ySpacing);
        int rowMax = (int) Math.ceil((worldBottom + marginY) / ySpacing);

        rowMin = clamp(rowMin, 0, ROWS - 1);
        rowMax = clamp(rowMax, 0, ROWS - 1);

        // Styling
        g.setLineWidth(1.0);
        g.setStroke(Color.rgb(160, 160, 255, 0.35));
        g.setFill(Color.rgb(28, 28, 40, 0.85));

        int drawn = 0;

        // Draw visible rows
        for (int row = rowMin; row <= rowMax; row++) {
            double rowOffset = ((row & 1) == 1) ? (hexW / 2.0) : 0.0;
            double cyWorld = row * ySpacing;

            // Solve col bounds from: col*hexW + rowOffset within [worldLeft..worldRight]
            int colMin = (int) Math.floor((worldLeft - marginX - rowOffset) / xSpacing);
            int colMax = (int) Math.ceil((worldRight + marginX - rowOffset) / xSpacing);

            colMin = clamp(colMin, 0, COLS - 1);
            colMax = clamp(colMax, 0, COLS - 1);

            for (int col = colMin; col <= colMax; col++) {
                double cxWorld = col * xSpacing + rowOffset;

                // world->screen
                double cx = (cxWorld - camX) * zoom;
                double cy = (cyWorld - camY) * zoom;

                // Draw hex (pointy-top)
                drawHex(g, cx, cy, size);
                drawn++;
            }
        }

        lastDrawn = drawn;

        // HUD
        g.setFill(Color.rgb(230, 230, 255, 0.85));
        g.setFont(Font.font(14));
        g.fillText("FPS: " + String.format("%.1f", fps)
                        + " | Drawn: " + lastDrawn
                        + " | Zoom: " + String.format("%.2f", zoom)
                        + " | Cam: (" + (int) camX + "," + (int) camY + ")",
                12, 22);
    }

    private void drawHex(GraphicsContext g, double cx, double cy, double size) {
        double[] xs = new double[6];
        double[] ys = new double[6];

        // pointy-top: 30° + 60°*i
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(30 + 60.0 * i);
            xs[i] = cx + size * Math.cos(a);
            ys[i] = cy + size * Math.sin(a);
        }

        g.fillPolygon(xs, ys, 6);
        g.strokePolygon(xs, ys, 6);
    }

    private void updateFps(long now) {
        frames++;
        if (lastFpsTime == 0) lastFpsTime = now;

        long elapsed = now - lastFpsTime;
        if (elapsed >= 1_000_000_000L) {
            fps = frames * 1_000_000_000.0 / elapsed;
            frames = 0;
            lastFpsTime = now;
        }
    }

    private double screenToWorldX(double sx, double canvasW, double z) {
        return camX + sx / z;
    }

    private double screenToWorldY(double sy, double canvasH, double z) {
        return camY + sy / z;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

