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

public class HexCanvasPanStressTest extends Application {

    private final boolean[] inRange = new boolean[COLS * ROWS];
    private static final int FEET_PER_HEX = 5;
    private static final int RADIUS_FT = 30;
    private static final int RADIUS_STEPS = RADIUS_FT / FEET_PER_HEX; // 6


    private static final int COLS = 400;
    private static final int ROWS = 400;

    private static final double BASE_SIZE = 10.0; // radius at zoom=1
    private static final double SQRT3 = Math.sqrt(3);

    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    private long lastFpsTime = 0;
    private int frames = 0;
    private double fps = 0;
    private int lastDrawn = 0;

    // Selection state
    private final boolean[] selected = new boolean[COLS * ROWS];

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(1200, 800);
        GraphicsContext g = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // Pan (drag)
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

            camX -= dx / zoom;
            camY -= dy / zoom;
        });

        // Zoom around cursor
        canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            double oldZoom = zoom;
            double factor = Math.pow(1.0015, e.getDeltaY());
            zoom = clamp(zoom * factor, 0.2, 4.0);

            double mx = e.getX();
            double my = e.getY();
            double worldXBefore = screenToWorldX(mx, oldZoom);
            double worldYBefore = screenToWorldY(my, oldZoom);
            double worldXAfter  = screenToWorldX(mx, zoom);
            double worldYAfter  = screenToWorldY(my, zoom);

            camX += (worldXBefore - worldXAfter);
            camY += (worldYBefore - worldYAfter);
            e.consume();
        });

        // Click to toggle tile (Canvas picking)
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (dragging) return;

            double wx = screenToWorldX(e.getX(), zoom);
            double wy = screenToWorldY(e.getY(), zoom);

            int[] rc = pickOddRPointy(wx, wy, BASE_SIZE);
            int row = rc[0];
            int col = rc[1];

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                // optional: toggle selected
                int idx = row * COLS + col;
                selected[idx] = !selected[idx];

                // NEW: mark 30ft radius
                markRadius(row, col, RADIUS_STEPS);
            }
        });


        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long now) {
                render(g, canvas.getWidth(), canvas.getHeight());
                updateFps(now);
            }
        };
        timer.start();

        stage.setTitle("Hex Canvas Stress Test (400x400) - Pan/Zoom/Click");
        stage.setScene(new Scene(root, 1200, 800, Color.rgb(10, 10, 16)));
        stage.show();
    }

    private void render(GraphicsContext g, double w, double h) {
        g.setFill(Color.rgb(10, 10, 16));
        g.fillRect(0, 0, w, h);

        // World geometry uses BASE_SIZE, screen geometry uses BASE_SIZE*zoom
        double sizeScreen = BASE_SIZE * zoom;

        double hexWScreen = SQRT3 * sizeScreen;
        double hexHScreen = 2 * sizeScreen;
        double xSpacingScreen = hexWScreen;
        double ySpacingScreen = 1.5 * sizeScreen;

        // Viewport in world coords
        double worldLeft = camX;
        double worldTop = camY;
        double worldRight = camX + w / zoom;
        double worldBottom = camY + h / zoom;

        // World spacings (at zoom=1)
        double hexWWorld = SQRT3 * BASE_SIZE;
        double hexHWorld = 2 * BASE_SIZE;
        double xSpacingWorld = hexWWorld;
        double ySpacingWorld = 1.5 * BASE_SIZE;

        double marginX = hexWWorld;
        double marginY = hexHWorld;

        int rowMin = (int) Math.floor((worldTop - marginY) / ySpacingWorld);
        int rowMax = (int) Math.ceil((worldBottom + marginY) / ySpacingWorld);
        rowMin = clamp(rowMin, 0, ROWS - 1);
        rowMax = clamp(rowMax, 0, ROWS - 1);

        g.setLineWidth(1.0);
        g.setStroke(Color.rgb(160, 160, 255, 0.35));

        int drawn = 0;

        for (int row = rowMin; row <= rowMax; row++) {
            double rowOffsetWorld = ((row & 1) == 1) ? (hexWWorld / 2.0) : 0.0;

            double cyWorld = row * ySpacingWorld;

            int colMin = (int) Math.floor((worldLeft - marginX - rowOffsetWorld) / xSpacingWorld);
            int colMax = (int) Math.ceil((worldRight + marginX - rowOffsetWorld) / xSpacingWorld);
            colMin = clamp(colMin, 0, COLS - 1);
            colMax = clamp(colMax, 0, COLS - 1);

            for (int col = colMin; col <= colMax; col++) {
                int idx = row * COLS + col;

                double cxWorld = col * xSpacingWorld + rowOffsetWorld;

                double cx = (cxWorld - camX) * zoom;
                double cy = (cyWorld - camY) * zoom;

                if (selected[idx]) {
                    g.setFill(Color.rgb(90, 90, 160, 0.90));
                } else if (inRange[idx]) {
                    g.setFill(Color.rgb(120, 80, 40, 0.85)); // Range highlight
                } else {
                    g.setFill(Color.rgb(28, 28, 40, 0.85));
                }




                drawHex(g, cx, cy, sizeScreen);
                drawn++;
            }
        }

        lastDrawn = drawn;

        g.setFill(Color.rgb(230, 230, 255, 0.85));
        g.setFont(Font.font(14));
        g.fillText("FPS: " + String.format("%.1f", fps)
                        + " | Drawn: " + lastDrawn
                        + " | Zoom: " + String.format("%.2f", zoom),
                12, 22);
    }

    private void drawHex(GraphicsContext g, double cx, double cy, double size) {
        double[] xs = new double[6];
        double[] ys = new double[6];

        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(30 + 60.0 * i);
            xs[i] = cx + size * Math.cos(a);
            ys[i] = cy + size * Math.sin(a);
        }

        g.fillPolygon(xs, ys, 6);
        g.strokePolygon(xs, ys, 6);

    }

    // --- Picking: world pixel -> odd-r offset (row,col), pointy-top ---
    // Uses pixel->axial->round->odd-r conversion.
    private static int[] pickOddRPointy(double wx, double wy, double size) {
        // Convert world pixel to axial fractional (q,r) for pointy-top
        // https://www.redblobgames.com/grids/hexagons/ (formulas)
        double q = (SQRT3 / 3.0 * wx - 1.0 / 3.0 * wy) / size;
        double r = (2.0 / 3.0 * wy) / size;

        // Cube round
        double x = q;
        double z = r;
        double y = -x - z;

        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rz = (int) Math.round(z);

        double dx = Math.abs(rx - x);
        double dy = Math.abs(ry - y);
        double dz = Math.abs(rz - z);

        if (dx > dy && dx > dz) {
            rx = -ry - rz;
        } else if (dy > dz) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }

        // axial rounded
        int aq = rx;
        int ar = rz;

        // axial -> odd-r offset (col,row)
        int row = ar;
        int col = aq + (row - (row & 1)) / 2;

        return new int[]{row, col};
    }

    private double screenToWorldX(double sx, double z) { return camX + sx / z; }
    private double screenToWorldY(double sy, double z) { return camY + sy / z; }

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

    private void markRadius(int centerRow, int centerCol, int radiusSteps) {
        // clear previous
        java.util.Arrays.fill(inRange, false);

        // odd-r offset -> axial
        int[] axial = oddRToAxial(centerRow, centerCol);
        int cq = axial[0];
        int cr = axial[1];

        // Iterate all hexes within cube distance <= radius (O(r^2))
        for (int dq = -radiusSteps; dq <= radiusSteps; dq++) {
            int drMin = Math.max(-radiusSteps, -dq - radiusSteps);
            int drMax = Math.min(radiusSteps, -dq + radiusSteps);

            for (int dr = drMin; dr <= drMax; dr++) {
                int q = cq + dq;
                int r = cr + dr;

                // axial -> odd-r offset
                int[] rc = axialToOddR(q, r);
                int row = rc[0];
                int col = rc[1];

                if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                    inRange[row * COLS + col] = true;
                }
            }
        }

        // Optional: ensure center is included
        inRange[centerRow * COLS + centerCol] = true;
    }

    private static int[] oddRToAxial(int row, int col) {
        // odd-r (row offset) -> axial (q,r)
        int q = col - (row - (row & 1)) / 2;
        int r = row;
        return new int[]{q, r};
    }

    private static int[] axialToOddR(int q, int r) {
        // axial -> odd-r
        int row = r;
        int col = q + (row - (row & 1)) / 2;
        return new int[]{row, col};
    }


    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    public static void main(String[] args) { launch(args); }
}
