package com.fuchsbau.shorin.Engine.Map.Core;

import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.IndoorZone;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightMask;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightSource;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightingSystem;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.GameMap;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.Tile;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallSegment;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallType;
import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Options.GameOptions.BASE_TILE;
import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class MapRenderer {
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();
    private final Logger logger = FileLogger.getLogger();

    // --- View / camera ---
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    // --- Canvas / Drawing ---
    private Canvas canvas;
    private Canvas tokenCanvas;
    private ImageView colorView;   // Original-BG
    private ImageView grayView;    // Desaturiert
    private LightMask lightMask;   // Licht-Clip
    private Image backgroundImage;

    // --- Light ---
    private LightSource selectedLight = null;

    // --- Map ---
    private final GameMap gameMap;
    private final LightingSystem lightingSystem;
    private Token selectedToken;

    // Wall Preview
    private double wallPreviewX = -1, wallPreviewY = -1;
    private double wallPreviewStartX, wallPreviewStartY;
    private WallType wallPreviewType = WallType.WALL;
    private double highlightX = -1, highlightY = -1;

    // Indoor Zones
    private double zoneHighlightX = -1, zoneHighlightY = -1;
    private IndoorZone zonePreview = null;


    public boolean debug = false;

    public MapRenderer(GameMap gameMap, LightingSystem lightingSystem) {
        this.gameMap = gameMap;
        this.lightingSystem = lightingSystem;
        canvas = new Canvas(1200, 800);
        tokenCanvas = new Canvas(1200, 800);
        lightMask = new LightMask(1200, 800);

        colorView = new ImageView();
        colorView.setClip(lightMask.getLightCanvas());
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public String loadBackground() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose background image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File initialDir = PathResolver.resolveWritable("images").toFile();
        if (!initialDir.exists()) initialDir = PathResolver.resolveWritable("").toFile();
        fc.setInitialDirectory(initialDir.exists() ? initialDir : new File("."));

        File f = fc.showOpenDialog(canvas.getScene().getWindow());
        if (f == null) return null;

        backgroundImage = new Image(f.toURI().toString(), false);
        grayView.setImage(backgroundImage);
        colorView.setImage(backgroundImage);

        Path base = PathResolver.resolveWritable("");
        return base.relativize(f.toPath().toAbsolutePath())
                .toString().replace("\\", "/");
    }

    public void loadBackground(String relativePath) {
        String url = PathResolver.resolveString(relativePath);
        if (url == null) {
            logger.warning("Hintergrundbild nicht gefunden: " + relativePath);
            return;
        }
        backgroundImage = new Image(url, false);
        grayView.setImage(backgroundImage);
        colorView.setImage(backgroundImage);
        logger.info("Hintergrundbild geladen + gebaked: " + relativePath);
    }

    // ---------------- Rendering ----------------
    private Node buildCanvasPane() {
        tokenCanvas.setMouseTransparent(true);
        tokenCanvas.setPickOnBounds(false);

        // Grau-Layer: ColorAdjust desaturiert
        ColorAdjust gray = new ColorAdjust();
        gray.setSaturation(-1.0);
        grayView = new ImageView();
        grayView.setEffect(gray);
        grayView.setPreserveRatio(false);
        grayView.setMouseTransparent(true);

        // Farb-Layer: Original, mit LightMask als Clip
        colorView.setPreserveRatio(false);
        colorView.setMouseTransparent(true);


        StackPane pane = new StackPane(grayView, colorView, lightMask.getTintCanvas(), canvas, tokenCanvas);

        pane.setStyle("-fx-background-color: rgb(10,10,16);");

        // canvas für Input + Wände
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        tokenCanvas.widthProperty().bind(pane.widthProperty());
        tokenCanvas.heightProperty().bind(pane.heightProperty());

        // ImageViews auf Pane-Größe binden
        grayView.fitWidthProperty().bind(pane.widthProperty());
        grayView.fitHeightProperty().bind(pane.heightProperty());
        colorView.fitWidthProperty().bind(pane.widthProperty());
        colorView.fitHeightProperty().bind(pane.heightProperty());

        // LightMask bei Resize anpassen
        pane.widthProperty().addListener((obs, o, n) -> {
            lightMask.resize(n.doubleValue(), pane.getHeight());
        });
        pane.heightProperty().addListener((obs, o, n) -> {
            lightMask.resize(pane.getWidth(), n.doubleValue());
        });

        logger.fine("CanvasPane gebaut — grayView + colorView + lightMask");
        return pane;
    }

    private Node buildCanvasWithNameOverlayPane(TextField mapNameField) {
        Node canvasNode = buildCanvasPane();

        // TextField konfigurieren
        mapNameField.setPromptText("Map-Name");
        mapNameField.setMaxWidth(260);
        mapNameField.getStyleClass().add("map-name-field"); // falls du CSS nutzt

        // oben über dem Canvas positionieren
        StackPane.setAlignment(mapNameField, Pos.TOP_CENTER);
        StackPane.setMargin(mapNameField, new Insets(10, 10, 0, 10));

        StackPane root = new StackPane();
        root.getChildren().addAll(canvasNode, mapNameField); // Reihenfolge: Canvas unten, TextField oben

        return root;
    }

    public Node buildBattleMapPane(TextField textField) {
        Node node;
        if (textField != null) {
            node = buildCanvasWithNameOverlayPane(textField);
        } else {
            node = buildCanvasPane();
        }

        // rerender on resize
        canvas.widthProperty().addListener((obs, o, n) -> renderBattlemap());
        canvas.heightProperty().addListener((obs, o, n) -> renderBattlemap());
        canvas.widthProperty().addListener((obs, o, n) -> renderLightLayer());
        canvas.heightProperty().addListener((obs, o, n) -> renderLightLayer());

        lightingSystem.recomputeLightmapAll(gameMap);
        return node;
    }

    public Node buildWorldMapPane(TextField textField) {
        Node node;
        if (textField != null) {
            node = buildCanvasWithNameOverlayPane(textField);
        } else {
            node = buildCanvasPane();
        }

        // rerender on resize
        canvas.widthProperty().addListener((obs, o, n) -> renderWorldmap());
        canvas.heightProperty().addListener((obs, o, n) -> renderWorldmap());

        lightingSystem.recomputeLightmapAll(gameMap);
        return node;
    }


    public void renderBattlemap() {
        if (canvas == null) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.clearRect(0, 0, w, h);

        // Viewport statt drawImage
        if (backgroundImage != null) {
            double worldW = gameMap.getCols() * BASE_TILE;
            double worldH = gameMap.getRows() * BASE_TILE;

            double vpX = (camX / worldW) * backgroundImage.getWidth();
            double vpY = (camY / worldH) * backgroundImage.getHeight();
            double vpW = (w / zoom / worldW) * backgroundImage.getWidth();
            double vpH = (h / zoom / worldH) * backgroundImage.getHeight();

            Rectangle2D viewport = new Rectangle2D(vpX, vpY, vpW, vpH);
            grayView.setViewport(viewport);
            colorView.setViewport(viewport);
        }

        renderLightLayer();
        renderStrategyMap();

        renderWalls(g, camX, camY, zoom);
        if (wallPreviewX >= 0) renderWallPreview(g, camX, camY, zoom);
    }


    public void renderStrategyMap() {
        if (tokenCanvas == null) return;

        GraphicsContext g = tokenCanvas.getGraphicsContext2D();
        double w = tokenCanvas.getWidth();
        double h = tokenCanvas.getHeight();

        g.clearRect(0, 0, w, h);

        // --- sichtbare Tile-Bounds (wird von Grid + Tiles gebraucht) ---
        double worldLeft = camX;
        double worldTop = camY;
        double worldRight = camX + w / zoom;
        double worldBottom = camY + h / zoom;

        int colMin = clamp((int) Math.floor(worldLeft / BASE_TILE) - 2, 0, gameMap.getCols() - 1);
        int colMax = clamp((int) Math.ceil(worldRight / BASE_TILE) + 2, 0, gameMap.getCols() - 1);
        int rowMin = clamp((int) Math.floor(worldTop / BASE_TILE) - 2, 0, gameMap.getRows() - 1);
        int rowMax = clamp((int) Math.ceil(worldBottom / BASE_TILE) + 2, 0, gameMap.getRows() - 1);

        double step = BASE_TILE * zoom;
        double tileDraw = step + 1;
        double startX = Math.floor(((colMin * BASE_TILE) - camX) * zoom);
        double startY = Math.floor(((rowMin * BASE_TILE) - camY) * zoom);

        // --- Terrain-Tiles ---
        for (int r = rowMin; r <= rowMax; r++) {
            double y = startY + (r - rowMin) * step;
            double x = startX;
            for (int c = colMin; c <= colMax; c++) {
                Tile t = gameMap.getTile(r, c);
                if (t.hasDebugTerrain()) {
                    g.setGlobalAlpha(0.8);
                    g.setFill(t.getDebugColour());
                    g.fillRect(x, y, tileDraw, tileDraw);
                }
                x += step;
            }
        }
        g.setGlobalAlpha(1.0);

        // --- Grid ---
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

        // --- Tokens ---
        g.setFont(Font.font(12));
        for (Token t : gameMap.getTokens()) {
            double xWorld = t.col * BASE_TILE;
            double yWorld = t.row * BASE_TILE;

            double x = Math.floor((xWorld - camX) * zoom);
            double y = Math.floor((yWorld - camY) * zoom);
            double size = BASE_TILE * zoom;

            boolean selected = t == selectedToken;
            if (selected) {
                g.setStroke(Color.YELLOW);
                g.setLineWidth(2.5);
                g.strokeOval(x - 2, y - 2, size + 4, size + 4);
            }

            if (t.npcBuild != null && t.npcBuild.tokenPath != null && !t.npcBuild.tokenPath.isBlank()) {
                Image img = ImagePreLoader.getCached(t.npcBuild.tokenPath);
                if (img != null && !img.isError()) {
                    g.drawImage(img, x, y, size, size);
                } else {
                    g.setFill(Color.CORNFLOWERBLUE);
                    g.fillOval(x, y, size, size);
                }
            } else {
                g.setFill(sceneBuilder.beigeRGB);
                g.fillOval(x, y, size, size);
            }
        }

        // --- LightSource-Dots ---
        for (LightSource ls : gameMap.getLights()) {
            double x = (ls.x - camX) * zoom;
            double y = (ls.y - camY) * zoom;
            double size = BASE_TILE * zoom * 0.35;

            // Ausgewähltes Licht gelb markieren — Renderer braucht selectedLight
            if (ls == selectedLight) {
                g.setFill(Color.YELLOW);
                g.fillOval(x - size * 0.7, y - size * 0.7, size * 1.4, size * 1.4);
            }

            g.setFill(Color.color(ls.colorR, ls.colorG, ls.colorB));
            g.fillOval(x - size * 0.5, y - size * 0.5, size, size);
        }

        // --- Indoor Zonen ---

        // Highlight
        g.setLineDashes(0);
        g.setGlobalAlpha(1.0);
        if (zoneHighlightX >= 0) {
            g.setFill(Color.YELLOW);
            double hx = (zoneHighlightX - camX) * zoom;
            double hy = (zoneHighlightY - camY) * zoom;
            g.fillOval(hx - 6, hy - 6, 12, 12);
            logger.finest("ZoneHighlight gerendert @ screen(" + (int) hx + "/" + (int) hy + ")");
        }

        // Preview
        if (zonePreview != null) {
            g.setStroke(Color.rgb(255, 220, 50));
            g.setGlobalAlpha(0.8);
            g.setLineDashes(8, 4);
            double sx = (zonePreview.x - camX) * zoom;
            double sy = (zonePreview.y - camY) * zoom;
            double sw = zonePreview.width * zoom;
            double sh = zonePreview.height * zoom;
            g.strokeRect(sx, sy, sw, sh);
        }

        g.setLineDashes(8, 4);
        g.setLineWidth(1.5);
        for (IndoorZone z : gameMap.getIndoorZones()) {
            g.setStroke(Color.rgb(100, 200, 255));
            g.setGlobalAlpha(0.7);
            double sx = (z.x - camX) * zoom;
            double sy = (z.y - camY) * zoom;
            double sw = z.width * zoom;
            double sh = z.height * zoom;
            g.strokeRect(sx, sy, sw, sh);

            // Eckpunkte
            g.setFill(Color.rgb(100, 200, 255));
            g.setGlobalAlpha(0.9);
            drawZonePoint(g, z.x, z.y);
            drawZonePoint(g, z.x2, z.y2);
        }

        g.setLineDashes(0);
        g.setGlobalAlpha(1.0);

        // --- HUD ---
        g.setFill(sceneBuilder.beigeRGB);
        g.setFont(Font.font(14));
        g.fillText("Mapeditor | Zoom=" + String.format("%.2f", zoom)
                + " | Grid=" + gameMap.getRows() + "x" + gameMap.getCols(), 12, 20);

        logger.finest("tokenCanvas gerendert — Tokens=" + gameMap.getTokens().size()
                + " Lights=" + gameMap.getLights().size());
    }

    public void renderWorldmap() {
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

        // Tile ist immer 75% der Zelle, aber min 6px, max 48px
        double innerSize = clamp(step * 0.65, 6.0, 48.0);
        double radius = clamp(innerSize * 0.2, 2.0, 8.0);

        // Border
        g.setStroke(Color.BLACK);
        g.setLineWidth(1.0);

        for (int r = rowMin; r <= rowMax; r++) {
            double y = startY + (r - rowMin) * step;

            double x = startX;
            for (int c = colMin; c <= colMax; c++) {
                Tile t = gameMap.getTile(r, c);
                if (t.has(Tile.DISABLED) && !debug) {
                    continue;
                }

                // Terrain
                if (t.hasDebugTerrain() && debug) {
                    g.setFill(t.getDebugColour());
                    g.fillRect(x, y, tileDraw, tileDraw);
                }

                // zentriertes inneres "Tile"
                double ix = x + (step - innerSize) * 0.5;
                double iy = y + (step - innerSize) * 0.5;

                // Fill
                if (!t.has(Tile.DISABLED)) {
                    g.setFill(SceneBuilder.getSceneBuilder().worldMapBlue);
                    g.fillRoundRect(ix, iy, innerSize, innerSize, radius, radius);
                }

                // Border
                double bw = 1.0;
                g.strokeRoundRect(ix + bw * 0.5, iy + bw * 0.5,
                        innerSize - bw, innerSize - bw,
                        radius, radius);

                x += step;
            }
        }

        // nach Tile-Loop Alpha zurück
        g.setGlobalAlpha(1.0);

        // draw grid lines
        g.setStroke(sceneBuilder.strokeRGB);
        g.setLineWidth(1.0);

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

    private void renderLightLayer() {
        if (lightMask == null) return;
        lightMask.update(
                gameMap.getLights(),
                gameMap,
                lightingSystem.getSunDegree(),
                camX, camY, zoom,
                gameMap.getWalls()
        );
    }

    private void renderWallPreview(GraphicsContext g, double camX, double camY, double zoom) {
        g.setStroke(wallColor(wallPreviewType));
        g.setLineWidth(1.5);
        g.setLineDashes(6, 3);
        g.setGlobalAlpha(0.6);

        double sx1 = (wallPreviewStartX - camX) * zoom;
        double sy1 = (wallPreviewStartY - camY) * zoom;
        double sx2 = (wallPreviewX - camX) * zoom;
        double sy2 = (wallPreviewY - camY) * zoom;

        g.strokeLine(sx1, sy1, sx2, sy2);
        g.setLineDashes(0);
        g.setGlobalAlpha(1.0);
    }

    private void renderWalls(GraphicsContext g, double camX, double camY, double zoom) {
        g.setLineWidth(2.5);

        for (WallSegment wall : gameMap.getWalls()) {
            drawWallPoint(g, wall.x1, wall.y1, camX, camY, zoom, wallColor(wall.type));
            drawWallPoint(g, wall.x2, wall.y2, camX, camY, zoom, wallColor(wall.type));
        }

        // Ausgewählter Punkt gelb
        if (highlightX >= 0) {
            double sx = (highlightX - camX) * zoom;
            double sy = (highlightY - camY) * zoom;
            g.setFill(Color.YELLOW);
            g.fillOval(sx - 5, sy - 5, 10, 10);
        }

        for (WallSegment wall : gameMap.getWalls()) {
            g.setStroke(wallColor(wall.type));

            double sx1 = (wall.x1 - camX) * zoom;
            double sy1 = (wall.y1 - camY) * zoom;
            double sx2 = (wall.x2 - camX) * zoom;
            double sy2 = (wall.y2 - camY) * zoom;

            // Gestrichelt für spezielle Typen
            if (wall.type == WallType.INVISIBLE || wall.type == WallType.ETHEREAL) {
                g.setLineDashes(8, 4);
            } else {
                g.setLineDashes(0);
            }

            g.strokeLine(sx1, sy1, sx2, sy2);

            // Endpunkte markieren
            g.setFill(wallColor(wall.type));
            g.fillOval(sx1 - 3, sy1 - 3, 6, 6);
            g.fillOval(sx2 - 3, sy2 - 3, 6, 6);
        }

        g.setLineDashes(0);
    }

    public void refreshViews() {
        if (backgroundImage == null) return;
        if (grayView != null) grayView.setImage(backgroundImage);
        if (colorView != null) colorView.setImage(backgroundImage);
        logger.fine("Views nach Tab-Wechsel neu gesetzt");
    }

    private void drawWallPoint(GraphicsContext g, double wx, double wy,
                               double camX, double camY, double zoom, Color color) {
        double sx = (wx - camX) * zoom;
        double sy = (wy - camY) * zoom;
        g.setFill(color);
        g.fillOval(sx - 3, sy - 3, 6, 6);
    }

    private void drawZonePoint(GraphicsContext g, double wx, double wy) {
        double sx = (wx - camX) * zoom;
        double sy = (wy - camY) * zoom;
        g.fillOval(sx - 4, sy - 4, 8, 8);
    }

    private Color wallColor(WallType type) {
        return switch (type) {
            case WALL -> Color.rgb(180, 180, 190);
            case TERRAIN -> Color.rgb(100, 160, 80);
            case INVISIBLE -> Color.rgb(100, 150, 255);
            case ETHEREAL -> Color.rgb(180, 100, 255);
            case DOOR -> Color.rgb(180, 130, 60);
            case SECRET_DOOR -> Color.rgb(160, 60, 60);
            case WINDOW -> Color.rgb(100, 220, 255);
        };
    }

    public void setWallPreview(double startX, double startY, double endX, double endY, WallType type) {
        wallPreviewStartX = startX;
        wallPreviewStartY = startY;
        wallPreviewX = endX;
        wallPreviewY = endY;
        wallPreviewType = type;
    }

    public void clearWallPreview() {
        wallPreviewX = -1;
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

    public void setSelectedToken(Token selectedToken) {
        this.selectedToken = selectedToken;
    }

    public void setHighlightPoint(double x, double y) {
        highlightX = x;
        highlightY = y;
    }

    public void clearHighlight() {
        highlightX = -1;
    }

    public void setZonePreview(IndoorZone zone) {
        this.zonePreview = zone;
    }

    public void clearZonePreview() {
        this.zonePreview = null;
    }

    public void setZoneHighlight(double x, double y) {
        zoneHighlightX = x;
        zoneHighlightY = y;
    }

    public void clearZoneHighlight() {
        zoneHighlightX = -1;
    }

    public void setSelectedLight(LightSource ls) {
        this.selectedLight = ls;
    }
}
