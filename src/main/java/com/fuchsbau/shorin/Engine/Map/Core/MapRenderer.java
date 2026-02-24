package com.fuchsbau.shorin.Engine.Map.Core;

import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;

import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class MapRenderer {
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();

    // --- View / camera ---
    private static final double BASE_TILE = 24.0; // world px
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;


    // --- Convas / Drawing ---
    private Canvas canvas;
    private Image backgroundImage;

    // --- Map ---
    private GameMap gameMap;
    private LightingSystem lightingSystem;
    public MapRenderer(GameMap gameMap, LightingSystem lightingSystem) {
        this.gameMap = gameMap;
        this.lightingSystem = lightingSystem;
        canvas = new Canvas(1200, 800);
    }


    public Canvas getCanvas() {
        return canvas;
    }

    public void loadBackground() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose background image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File f = fc.showOpenDialog(canvas.getScene().getWindow());
        if (f == null) return;

        backgroundImage = new Image(f.toURI().toString(), false);
        render();
    }

    // ---------------- Rendering ----------------
    public Node buildCanvasPane() {
        StackPane pane = new StackPane(canvas);
        pane.setStyle("-fx-background-color: rgb(10,10,16);");

        // resize canvas with pane
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // rerender on resize
        canvas.widthProperty().addListener((obs, o, n) -> render());
        canvas.heightProperty().addListener((obs, o, n) -> render());

        lightingSystem.recomputeLightmapAll(gameMap);
        render();

        return pane;
    }

    public void render() {
        if (canvas == null) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();


        g.setFill(sceneBuilder.blackRGB);
        g.fillRect(0, 0, w, h);

        if (backgroundImage != null) {
            double worldW = gameMap.getCols() * BASE_TILE;
            double worldH = gameMap.getRows() * BASE_TILE;

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

        int colMin = clamp((int) Math.floor(worldLeft / BASE_TILE) - 2, 0, gameMap.getCols() - 1);
        int colMax = clamp((int) Math.ceil(worldRight / BASE_TILE) + 2, 0, gameMap.getCols() - 1);
        int rowMin = clamp((int) Math.floor(worldTop / BASE_TILE) - 2, 0, gameMap.getRows() - 1);
        int rowMax = clamp((int) Math.ceil(worldBottom / BASE_TILE) + 2, 0, gameMap.getRows() - 1);

        // draw tiles
        double step = BASE_TILE * zoom;
        double tileDraw = step + 1;

        double startX = Math.floor(((colMin * BASE_TILE) - camX) * zoom);
        double startY = Math.floor(((rowMin * BASE_TILE) - camY) * zoom);

        g.setFill(Color.BLACK); // Overlay-Farbe einmal setzen

        for (int r = rowMin; r <= rowMax; r++) {
            double y = startY + (r - rowMin) * step;

            double x = startX;
            for (int c = colMin; c <= colMax; c++) {
                Tile t = gameMap.getTile(r, c);

                // Terrain
                if (t.hasDebugTerrain()) {
                    g.setGlobalAlpha(0.8);
                    g.setFill(t.getDebugColour());
                    g.fillRect(x, y, tileDraw, tileDraw);
                }

                // Light overlay (immer möglich, unabhängig von Terrain)
                float b = t.getBrightness();
                if (b < 0f) b = 0f;
                if (b > 1f) b = 1f;

                double darkAlpha = 1.0 - b;
                if (darkAlpha > 0.0001) {
                    g.setGlobalAlpha(darkAlpha);
                    g.setFill(Color.BLACK);
                    g.fillRect(x, y, tileDraw, tileDraw);
                }

                x += step;
            }
        }

        // nach Tile-Loop Alpha zurück
        g.setGlobalAlpha(1.0);


        // draw grid lines
        g.setStroke(sceneBuilder.strokeRGB);
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
        for (Token t : gameMap.getTokens()) {
            double xWorld = t.col * BASE_TILE;
            double yWorld = t.row * BASE_TILE;

            double x = Math.floor((xWorld - camX) * zoom);
            double y = Math.floor((yWorld - camY) * zoom);

            g.setFill(sceneBuilder.beigeRGB);
            g.fillText(t.name, x + 4, y + 14);
        }

        for (LightSource ls : gameMap.getLights()) {
            double xWorld = ls.col * BASE_TILE;
            double yWorld = ls.row * BASE_TILE;

            double x = Math.floor((xWorld - camX) * zoom);
            double y = Math.floor((yWorld - camY) * zoom);

            double size = BASE_TILE * zoom * 0.35;

            g.setFill(sceneBuilder.redRGB);
            g.fillOval(
                    x + (BASE_TILE * zoom - size) / 2,
                    y + (BASE_TILE * zoom - size) / 2,
                    size,
                    size
            );
        }

        // HUD
        g.setFill(sceneBuilder.beigeRGB);
        g.setFont(Font.font(14));
        g.fillText("Mapeditor | Tool=" + "currentTool" + " | Zoom=" + String.format("%.2f", zoom)
                        + " | Grid=" + gameMap.getRows() + "x" + gameMap.getCols(),
                12, 20);
    }

    //camera
    public int[] pickTile(double sx, double sy) {
        double wx = screenToWorldX(sx, zoom);
        double wy = screenToWorldY(sy, zoom);

        int c = (int) Math.floor(wx / BASE_TILE);
        int r = (int) Math.floor(wy / BASE_TILE);

        if (r < 0 || r >= getGameMap().getRows() || c < 0 || c >= getGameMap().getCols()) return null;
        return new int[]{r, c};
    }

    public double screenToWorldX(double sx, double z) {
        return camX + sx / z;
    }

    public double screenToWorldY(double sy, double z) {
        return camY + sy / z;
    }

    public int screenToCol(double screenX) {
        double worldX = camX + screenX / zoom;
        return (int) Math.floor(worldX / BASE_TILE);
    }

    public int screenToRow(double screenY) {
        double worldY = camY + screenY / zoom;
        return (int) Math.floor(worldY / BASE_TILE);
    }



    public GameMap getGameMap() {
        return gameMap;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getCamY() {
        return camY;
    }

    public void setCamY(double camY) {
        this.camY = camY;
    }

    public void setCamX(double camx) {
        this.camX = camx;
    }

    public double getCamX() {
        return camX;
    }
}
