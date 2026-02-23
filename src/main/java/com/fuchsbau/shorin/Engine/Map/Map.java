package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Map {
    private final Logger logger = FileLogger.getLogger();
    // --- meta ---
    public final String mapId;
    public final Path mapFile;

    private final List<Token> tokens = new ArrayList<>();

    // --- layers ---
    private Tile[][] tiles;             // terrain flags + occupant ptrs (occupants typically managed elsewhere)
    private int cols, rows;
    private float[][] light;            // 0..1, optional

    // --- background ---
    private String backgroundName;      // optional filename (relative)
    private Image backgroundImage;      // lazy-loaded

    private Canvas canvas = new Canvas(1200, 800);

    // --- View / camera ---
    private static final double BASE_TILE = 24.0; // world px
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    public Map(String mapId, File mapFile) {
        this.mapId = Objects.requireNonNull(mapId);
        this.mapFile = Objects.requireNonNull(mapFile.toPath());
        try {
            Mapeditor.loadMap(mapFile, this);
        } catch (IOException e) {
            logger.severe("File not found: " + mapFile.toPath());
        }
    }

    private void render() {
        if (canvas == null) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();


        g.setFill(Color.rgb(10, 10, 16));
        g.fillRect(0, 0, w, h);

        if (backgroundImage != null) {
            double worldW = cols * BASE_TILE;
            double worldH = rows * BASE_TILE;

            double sx = (-camX) * zoom;
            double sy = (-camY) * zoom;
            double sw = worldW * zoom;
            double sh = worldH * zoom;

            g.drawImage(backgroundImage, sx, sy, sw, sh);
        }

        double tileScreen = BASE_TILE * zoom;

        // visible tile bounds
        double worldLeft = camX;
        double worldTop = camY;
        double worldRight = camX + w / zoom;
        double worldBottom = camY + h / zoom;

        int colMin = clamp((int) Math.floor(worldLeft / BASE_TILE) - 2, 0, cols - 1);
        int colMax = clamp((int) Math.ceil(worldRight / BASE_TILE) + 2, 0, cols - 1);
        int rowMin = clamp((int) Math.floor(worldTop / BASE_TILE) - 2, 0, rows - 1);
        int rowMax = clamp((int) Math.ceil(worldBottom / BASE_TILE) + 2, 0, rows - 1);

        // draw tiles
        g.setGlobalAlpha(0.8);
        for (int r = rowMin; r <= rowMax; r++) {
            double yWorld = r * BASE_TILE;
            double y = Math.floor((yWorld - camY) * zoom);

            for (int c = colMin; c <= colMax; c++) {
                if (tiles[r][c].flags == 0) continue;

                double xWorld = c * BASE_TILE;
                double x = Math.floor((xWorld - camX) * zoom);

                g.setFill(colorFor(tiles[r][c]));

                // slight overdraw to avoid seams
                g.fillRect(x, y, tileScreen + 1, tileScreen + 1);
            }
        }
        g.setGlobalAlpha(1.0);


        // draw grid lines
        g.setStroke(Color.rgb(160, 160, 255, 0.20));
        g.setLineWidth(2.0);

        double x0 = Math.floor(((colMin * BASE_TILE) - camX) * zoom);
        double x1 = Math.floor((((colMax + 1) * BASE_TILE) - camX) * zoom);
        double y0 = Math.floor(((rowMin * BASE_TILE) - camY) * zoom);
        double y1 = Math.floor((((rowMax + 1) * BASE_TILE) - camY) * zoom);

        for (int c = colMin; c <= colMax + 1; c++) {
            double xLine = Math.floor(((c * BASE_TILE) - camX) * zoom);
            g.strokeLine(xLine, y0, xLine, y1);
        }
        for (int r = rowMin; r <= rowMax + 1; r++) {
            double yLine = Math.floor(((r * BASE_TILE) - camY) * zoom);
            g.strokeLine(x0, yLine, x1, yLine);
        }

        // draw tokens
        g.setFont(Font.font(12));
        for (Token t : tokens) {
            double xWorld = t.col * BASE_TILE;
            double yWorld = t.row * BASE_TILE;

            double x = Math.floor((xWorld - camX) * zoom);
            double y = Math.floor((yWorld - camY) * zoom);

            g.setFill(Color.rgb(230, 230, 255, 0.90));
            g.fillText(t.name, x + 4, y + 14);
        }

        // HUD
        g.setFill(Color.rgb(230, 230, 255, 0.85));
        g.setFont(Font.font(14));
        g.fillText("Mapeditor | Tool=" + " | Zoom=" + String.format("%.2f", zoom)
                        + " | Grid=" + rows + "x" + cols,
                12, 20);
    }

    private Color colorFor(Tile t) {
        if (t.has(Tile.WALL)) return Color.rgb(80, 80, 90);
        if (t.has(Tile.DOOR)) return Color.rgb(120, 90, 40);
        if (t.has(Tile.GREATER_DIFFICULT)) return Color.rgb(30, 120, 30);
        if (t.has(Tile.DIFFICULT)) return Color.rgb(40, 90, 40);
        if (t.has(Tile.HAZARDOUS)) return Color.rgb(140, 40, 40);

        return Color.rgb(28, 28, 40);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public void setTiles(Tile[][] grid, int cols, int rows) {
        this.tiles = grid;
        this.cols = cols;
        this.rows = rows;
    }
}
