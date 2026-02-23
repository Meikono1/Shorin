package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Mapeditor implements Saveble {

    private static final Logger logger = FileLogger.getLogger();

    private Image backgroundImage;
    private boolean drawBackground = true;

    // --- Grid model ---
    private int rows = 30;
    private int cols = 30;

    private Tile[][] grid = new Tile[rows][cols];
    private final List<LightSource> lights = new ArrayList<>();

    // --- View / camera ---
    private static final double BASE_TILE = 24.0; // world px
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    private boolean panning = false;
    private double lastMouseX, lastMouseY;

    // --- Painting ---
    private Tool currentTool = Tool.WALL;
    private boolean painting = false;

    // --- Tokens (chars) ---
    private final List<Token> tokens = new ArrayList<>();

    // --- UI ---
    private Canvas canvas;
    private Label toolLabel;
    private TextField mapNameField;

    public Mapeditor() {
        // init grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = Tile.empty();
            }
        }
    }

    @Override
    public Scene getScene(int stage) {
        canvas = new Canvas(1200, 800);

        BorderPane root = new BorderPane();
        root.setLeft(buildPalette());
        root.setCenter(buildCanvasPane());

        Scene scene = new Scene(root, 1400, 900);
        setupInputHandlers();
        render();

        String cssUrl = CSSLoader.resolveUserOrBackupCSS();
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl);
        } else {
            logger.warning("CSS not found: css/main.css");
        }

        return scene;
    }

    // ---------------- UI ----------------

    private Node buildPalette() {
        VBox box = new VBox(8);
        box.setPrefWidth(220);
        box.setStyle("-fx-padding: 10; -fx-background-color: rgba(20,20,28,0.95);");

        toolLabel = new Label("Tool: " + currentTool);
        toolLabel.setTextFill(Color.WHITE);

        Label hint = new Label("Klick oder Drag&Drop auf die Map.");
        hint.setTextFill(Color.rgb(200, 200, 220));

        box.getChildren().addAll(toolLabel, hint, new Separator());

        // --- Save Controls ---
        Label saveLabel = new Label("Save");
        saveLabel.setTextFill(Color.WHITE);

        mapNameField = new TextField("my_map");
        mapNameField.setPromptText("Map name");
        mapNameField.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = SceneBuilder.getSceneBuilder().makeButton("Save Map");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            try {
                String name = mapNameField.getText().trim();
                if (name.isEmpty()) return;

                if (!name.endsWith(".txt")) name += ".txt";

                File dir = new File("maps");
                if (!dir.exists()) dir.mkdirs();

                File out = new File(dir, name);
                saveMap(out);
                System.out.println("Saved: " + out.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(saveLabel, mapNameField, saveBtn, new Separator());


        box.getChildren().add(makeToolItem("Wall", Tool.WALL));
        box.getChildren().add(makeToolItem("Door", Tool.DOOR));
        box.getChildren().add(makeToolItem("Difficult", Tool.DIFFICULT));
        box.getChildren().add(makeToolItem("Hazard", Tool.HAZARD));
        box.getChildren().add(makeToolItem("Erase", Tool.ERASE));

        // --- Grid Controls ---
        Label gridLabel = new Label("Grid");
        gridLabel.setTextFill(Color.WHITE);

        Button addRowTop = SceneBuilder.getSceneBuilder().makeButton("+ Row Top");
        addRowTop.setOnAction(e -> {
            addRowTop();
            render();
        });

        Button addRowBottom = SceneBuilder.getSceneBuilder().makeButton("+ Row Bottom");
        addRowBottom.setOnAction(e -> {
            addRowBottom();
            render();
        });

        Button addColLeft = SceneBuilder.getSceneBuilder().makeButton("+ Col Left");
        addColLeft.setOnAction(e -> {
            addColLeft();
            render();
        });

        Button addColRight = SceneBuilder.getSceneBuilder().makeButton("+ Col Right");
        addColRight.setOnAction(e -> {
            addColRight();
            render();
        });

        Button remRowTop = SceneBuilder.getSceneBuilder().makeButton("- Row Top");
        remRowTop.setOnAction(e -> {
            removeRowTop();
            render();
        });

        Button remRowBottom = SceneBuilder.getSceneBuilder().makeButton("- Row Bottom");
        remRowBottom.setOnAction(e -> {
            removeRowBottom();
            render();
        });

        Button remColLeft = SceneBuilder.getSceneBuilder().makeButton("- Col Left");
        remColLeft.setOnAction(e -> {
            removeColLeft();
            render();
        });

        Button remColRight = SceneBuilder.getSceneBuilder().makeButton("- Col Right");
        remColRight.setOnAction(e -> {
            removeColRight();
            render();
        });

        gridLabel.setTextFill(Color.WHITE);

        TextField rowsField = new TextField(String.valueOf(rows));
        rowsField.setPrefWidth(70);

        TextField colsField = new TextField(String.valueOf(cols));
        colsField.setPrefWidth(70);

        Button applySize = SceneBuilder.getSceneBuilder().makeButton("Apply");
        applySize.setOnAction(e -> {
            int newRows = parsePositiveInt(rowsField.getText(), rows);
            int newCols = parsePositiveInt(colsField.getText(), cols);

            resizeGrid(newRows, newCols);
            render();
        });

        HBox fieldsRow = new HBox(8,
                new Label("R:"), rowsField,
                new Label("C:"), colsField
        );

        VBox sizeBox = new VBox(6,
                fieldsRow,
                applySize
        );


        Button loadBg = new Button("Load Background...");
        loadBg.setOnAction(e -> loadBackground());

        VBox gridControls = new VBox(4,
                sizeBox,
                new Separator(),
                addRowTop, addRowBottom,
                addColLeft, addColRight,
                new Separator(),
                remRowTop, remRowBottom,
                remColLeft, remColRight,
                loadBg
        );
        box.getChildren().addAll(gridLabel, gridControls, new Separator());

        Label lightLabel = new Label("Lights");
        box.getChildren().addAll(lightLabel,
                makeLightItem("Candle (5/15)", LightPreset.CANDLE),
                makeLightItem("Torch (20/40ft)", LightPreset.TORCH),
                makeLightItem("Lantern (30/60ft)", LightPreset.LANTERN),
                new Separator()
        );

        // --- Tokens ---
        box.getChildren().add(makeTokenItem("Char: Fighter"));
        box.getChildren().add(makeTokenItem("Char: Goblin"));


        ScrollPane scroll = SceneBuilder.getSceneBuilder().createScrollPane();
        scroll.setContent(box);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: rgba(20,20,28,0.95);");

        return scroll;
    }

    private Node buildCanvasPane() {
        StackPane pane = new StackPane(canvas);
        pane.setStyle("-fx-background-color: rgb(10,10,16);");

        // resize canvas with pane
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // rerender on resize
        canvas.widthProperty().addListener((obs, o, n) -> render());
        canvas.heightProperty().addListener((obs, o, n) -> render());

        return pane;
    }

    private Label makeToolItem(String name, Tool tool) {
        Label l = new Label(name);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-background-color: rgba(40,40,60,0.6); -fx-padding: 8; -fx-background-radius: 10;");

        // click selects tool
        l.setOnMouseClicked(e -> {
            currentTool = tool;
            toolLabel.setText("Tool: " + currentTool);
        });

        // drag starts "tool drag"
        l.setOnDragDetected(e -> {
            Dragboard db = l.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("TOOL:" + tool.name());
            db.setContent(cc);
            e.consume();
        });

        return l;
    }

    private Label makeTokenItem(String name) {
        Label l = new Label(name);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-background-color: rgba(60,40,40,0.6); -fx-padding: 8; -fx-background-radius: 10;");

        l.setOnDragDetected(e -> {
            Dragboard db = l.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("TOKEN:" + name);
            db.setContent(cc);
            e.consume();
        });

        return l;
    }

    private Node makeLightItem(String label, LightPreset preset) {
        Label item = new Label(label);
        item.setTextFill(Color.WHITE);
        item.setStyle("-fx-padding: 6 8; -fx-background-color: rgba(40,40,60,0.6); -fx-background-radius: 8;");
        item.setMaxWidth(Double.MAX_VALUE);

        item.setOnDragDetected(e -> {
            Dragboard db = item.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();

            // Format: LIGHT:<name>:<brightTiles>:<intensity>
            cc.putString("LIGHT:" + preset.name()
                    + ":" + preset.brightTiles()
                    + ":" + preset.dimTiles()
                    + ":" + preset.intensity);
            db.setContent(cc);

            e.consume();
        });

        return item;
    }

    // ---------------- Input ----------------

    private void setupInputHandlers() {
        // Mouse press
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Optional: mit SHIFT = Light entfernen, sonst Pan
                if (e.isShiftDown()) {
                    int col = screenToCol(e.getX());
                    int row = screenToRow(e.getY());
                    if (inBounds(row, col) && removeLightAt(row, col)) {
                        recomputeLightmapAll();
                        render();
                    }
                    e.consume();
                    return;
                }

                panning = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                e.consume();
                return;
            }

            if (e.getButton() == MouseButton.PRIMARY) {
                painting = true;
                applyAt(e.getX(), e.getY(), false);
                e.consume();
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.SECONDARY) panning = false;
            if (e.getButton() == MouseButton.PRIMARY) painting = false;
            e.consume();
        });

        canvas.setOnMouseDragged(e -> {
            if (panning && e.isSecondaryButtonDown()) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                camX -= dx / zoom;
                camY -= dy / zoom;
                render();
                e.consume();
                return;
            }

            if (painting && e.isPrimaryButtonDown()) {
                applyAt(e.getX(), e.getY(), true);
                e.consume();
            }
        });

        // Zoom: wheel
        canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            double oldZoom = zoom;
            double factor = Math.pow(1.0015, e.getDeltaY());
            zoom = clamp(zoom * factor, 0.2, 6.0);

            double mx = e.getX();
            double my = e.getY();

            double worldXBefore = screenToWorldX(mx, oldZoom);
            double worldYBefore = screenToWorldY(my, oldZoom);
            double worldXAfter = screenToWorldX(mx, zoom);
            double worldYAfter = screenToWorldY(my, zoom);

            camX += (worldXBefore - worldXAfter);
            camY += (worldYBefore - worldYAfter);

            render();
            e.consume();
        });

        // Drag&Drop
        canvas.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (!db.hasString()) return;

            String s = db.getString();
            if (s.startsWith("TOOL:") || s.startsWith("TOKEN:") || s.startsWith("LIGHT:")) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        canvas.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean ok = false;

            if (db.hasString()) {
                String s = db.getString();

                if (s.startsWith("TOOL:")) {
                    currentTool = Tool.valueOf(s.substring("TOOL:".length()));
                    toolLabel.setText("Tool: " + currentTool);
                    ok = true;

                } else if (s.startsWith("TOKEN:")) {
                    int[] rc = pickTile(e.getX(), e.getY());
                    if (rc != null) {
                        tokens.add(new Token(rc[0], rc[1], s.substring("TOKEN:".length())));
                        render();
                        ok = true;
                    }

                } else if (s.startsWith("LIGHT:")) {
                    String[] p = s.split(":");
                    int brightTiles = Integer.parseInt(p[2]);
                    int dimtiles = Integer.parseInt(p[3]);
                    float intensity = Float.parseFloat(p[4]);

                    int col = screenToCol(e.getX());
                    int row = screenToRow(e.getY());

                    if (inBounds(row, col)) {
                        addOrReplaceLight(row, col, brightTiles, dimtiles, intensity);
                        recomputeLightmapAll();
                        render();
                        ok = true;
                    }
                }
            }

            e.setDropCompleted(ok);
            e.consume();
        });
    }

    private void applyAt(double sx, double sy, boolean isDragPaint) {
        int[] rc = pickTile(sx, sy);
        if (rc == null) return;

        int r = rc[0], c = rc[1];
        Tile tile = grid[r][c];

        switch (currentTool) {
            case WALL -> {
                tile.clearAll();
                tile.add(Tile.WALL);
            }

            case DOOR -> {
                if (!tile.has(Tile.DOOR)) {
                    tile.clearAll();
                    tile.add(Tile.DOOR);
                    tile.add(Tile.DOOR_OPEN);
                } else {
                    tile.set(Tile.DOOR_OPEN, !tile.has(Tile.DOOR_OPEN));
                }
            }

            case DIFFICULT -> {
                tile.add(Tile.DIFFICULT);
            }

            case HAZARD -> {
                tile.add(Tile.HAZARDOUS);
            }

            case ERASE -> {
                tile.clearAll();
            }
        }

        render();
    }

    private int[] pickTile(double sx, double sy) {
        double wx = screenToWorldX(sx, zoom);
        double wy = screenToWorldY(sy, zoom);

        int c = (int) Math.floor(wx / BASE_TILE);
        int r = (int) Math.floor(wy / BASE_TILE);

        if (r < 0 || r >= rows || c < 0 || c >= cols) return null;
        return new int[]{r, c};
    }

    // ---------------- Rendering ----------------

    private void render() {
        if (canvas == null) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();


        g.setFill(Color.rgb(10, 10, 16));
        g.fillRect(0, 0, w, h);

        if (drawBackground && backgroundImage != null) {
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
                Tile t = grid[r][c];

                double xWorld = c * BASE_TILE;
                double x = Math.floor((xWorld - camX) * zoom);

                // 1) Terrain nur wenn gesetzt
                if (t.flags != 0) {
                    g.setFill(colorFor(t));
                    g.fillRect(x, y, tileScreen + 1, tileScreen + 1);
                }

                // 2) Light overlay IMMER (auch bei empty tiles / background)
                float b = t.getBrightness();
                double darkAlpha = 1.0 - Math.max(0, Math.min(1, b));

                if (darkAlpha > 0.0001) {
                    g.setFill(Color.rgb(0, 0, 0, darkAlpha));
                    g.fillRect(x, y, tileScreen + 1, tileScreen + 1);
                }
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
        g.fillText("Mapeditor | Tool=" + currentTool + " | Zoom=" + String.format("%.2f", zoom)
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

    private double screenToWorldX(double sx, double z) {
        return camX + sx / z;
    }

    private double screenToWorldY(double sy, double z) {
        return camY + sy / z;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public void reset() {

    }

    // ---------------- Row Buttons ----------------
    private void saveMap(File file) throws IOException {

        try (BufferedWriter w = Files.newBufferedWriter(file.toPath())) {

            // Header
            w.write(rows + " " + cols);
            w.newLine();

            // Grid
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    w.write(Integer.toString(grid[r][c].flags));
                    if (c < cols - 1) w.write(" ");
                }
                w.newLine();
            }
        }
    }

    private void loadBackground() {
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

    private int screenToCol(double screenX) {
        double worldX = camX + screenX / zoom;
        return (int) Math.floor(worldX / BASE_TILE);
    }

    private int screenToRow(double screenY) {
        double worldY = camY + screenY / zoom;
        return (int) Math.floor(worldY / BASE_TILE);
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private void recomputeLightmapAll() {
        // reset
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile t = grid[r][c];
                t.setBrightness(0f);
                t.setLightlevel(Lightlevel.DARKNESS);
            }
        }

        // apply all lights
        for (LightSource ls : lights) {
            applyLight(ls);
        }
    }

    private void applyLight(LightSource ls) {
        int brTiles = ls.brightTiles;  // das sind Tiles (5ft pro Tile)
        int dimTiles = ls.dimTiles;

        // Bounding box in Tiles
        int radTiles = Math.max(1, dimTiles);

        int r0 = clamp(ls.row - radTiles, 0, rows - 1);
        int r1 = clamp(ls.row + radTiles, 0, rows - 1);
        int c0 = clamp(ls.col - radTiles, 0, cols - 1);
        int c1 = clamp(ls.col + radTiles, 0, cols - 1);

        for (int r = r0; r <= r1; r++) {
            int dr = Math.abs(r - ls.row);
            for (int c = c0; c <= c1; c++) {
                int dc = Math.abs(c - ls.col);

                int distTiles = distanceFt(dc, dr);
                if (distTiles > dimTiles) continue;

                // Level
                Lightlevel lvl = (distTiles <= brTiles) ? Lightlevel.BRIGHT_LIGHT : Lightlevel.DIM_LIGHT;

                // Brightness: Ende bright = 0.5, dann bis 0
                float b = brightnessAtDistanceTiles(distTiles, brTiles, dimTiles, ls.intensity);
                if (b <= 0f) continue;

                Tile t = grid[r][c];

                if (b > t.getBrightness()) t.setBrightness(b);

                // Level-Priorität: BRIGHT > DIM > DARKNESS
                if (lvl == Lightlevel.BRIGHT_LIGHT) {
                    t.setLightlevel(Lightlevel.BRIGHT_LIGHT);
                } else if (t.getLightlevel() == Lightlevel.DARKNESS) {
                    t.setLightlevel(Lightlevel.DIM_LIGHT);
                }
            }
        }
    }

    private int distanceFt(int dxTiles, int dyTiles) {
        int max = Math.max(dxTiles, dyTiles);
        int min = Math.min(dxTiles, dyTiles);
        return max + (min / 2);
    }

    private float brightnessAtDistanceFt(int distFt, int brightFt, int dimFt, float intensity) {
        if (distFt <= brightFt) {
            float t = (brightFt == 0) ? 1f : (distFt / (float) brightFt); // 0..1
            float base = 1.0f + (0.5f - 1.0f) * t; // 1.0 -> 0.5
            return base * intensity;
        }
        if (distFt <= dimFt) {
            float t = (dimFt == brightFt) ? 1f : ((distFt - brightFt) / (float) (dimFt - brightFt)); // 0..1
            float base = 0.5f + (0.0f - 0.5f) * t; // 0.5 -> 0.0
            return base * intensity;
        }
        return 0f;
    }

    private float brightnessAtDistanceTiles(int d, int bright, int dim, float intensity) {
        if (d <= bright) {
            float t = (bright == 0) ? 1f : (d / (float) bright);
            float base = 1.0f + (0.5f - 1.0f) * t;
            return base * intensity;
        }
        if (d <= dim) {
            float t = (dim == bright) ? 1f : ((d - bright) / (float) (dim - bright));
            float base = 0.5f + (0.0f - 0.5f) * t;
            return base * intensity;
        }
        return 0f;
    }

    private void addOrReplaceLight(int row, int col, int brightTiles, int dimtiles, float intensity) {
        lights.removeIf(l -> l.row == row && l.col == col);
        lights.add(new LightSource(row, col, brightTiles, dimtiles, intensity));
    }

    private boolean removeLightAt(int row, int col) {
        int before = lights.size();
        lights.removeIf(l -> l.row == row && l.col == col);
        return lights.size() != before;
    }

    private void resizeGrid(int newRows, int newCols) {
        if (newRows <= 0 || newCols <= 0) return;

        Tile[][] newGrid = new Tile[newRows][newCols];

        // alles erstmal leer füllen
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                newGrid[r][c] = Tile.empty();
            }
        }

        // alten Inhalt kopieren (nur soweit möglich)
        int copyRows = Math.min(rows, newRows);
        int copyCols = Math.min(cols, newCols);

        for (int r = 0; r < copyRows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, copyCols);
        }

        grid = newGrid;
        rows = newRows;
        cols = newCols;
    }

    private void addRowTop() {
        Tile[][] newGrid = new Tile[rows + 1][cols];

        // neue erste Zeile
        for (int c = 0; c < cols; c++) newGrid[0][c] = Tile.empty();

        // alten Inhalt 1 nach unten kopieren
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r + 1], 0, cols);
        }

        grid = newGrid;
        rows++;
    }

    private void addRowBottom() {
        Tile[][] newGrid = new Tile[rows + 1][cols];

        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols);
        }
        for (int c = 0; c < cols; c++) newGrid[rows][c] = Tile.empty();

        grid = newGrid;
        rows++;
    }

    private void addColLeft() {
        Tile[][] newGrid = new Tile[rows][cols + 1];

        for (int r = 0; r < rows; r++) {
            newGrid[r][0] = Tile.empty();
            System.arraycopy(grid[r], 0, newGrid[r], 1, cols);
        }

        grid = newGrid;
        cols++;
    }

    private void addColRight() {
        Tile[][] newGrid = new Tile[rows][cols + 1];

        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols);
            newGrid[r][cols] = Tile.empty();
        }

        grid = newGrid;
        cols++;
    }

    private void removeRowTop() {
        if (rows <= 1) return;

        Tile[][] newGrid = new Tile[rows - 1][cols];
        for (int r = 1; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r - 1], 0, cols);
        }

        grid = newGrid;
        rows--;
    }

    private void removeRowBottom() {
        if (rows <= 1) return;

        Tile[][] newGrid = new Tile[rows - 1][cols];
        for (int r = 0; r < rows - 1; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols);
        }

        grid = newGrid;
        rows--;
    }

    private void removeColLeft() {
        if (cols <= 1) return;

        Tile[][] newGrid = new Tile[rows][cols - 1];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 1, newGrid[r], 0, cols - 1);
        }

        grid = newGrid;
        cols--;
    }

    private void removeColRight() {
        if (cols <= 1) return;

        Tile[][] newGrid = new Tile[rows][cols - 1];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols - 1);
        }

        grid = newGrid;
        cols--;
    }

    private int parsePositiveInt(String s, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    public static void loadMap(File file, Map map) throws IOException {

        try (BufferedReader r = Files.newBufferedReader(file.toPath())) {

            String[] header = r.readLine().split(" ");
            int rows = Integer.parseInt(header[0]);
            int cols = Integer.parseInt(header[1]);

            Tile[][] grid = new Tile[rows][cols];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    grid[i][j] = new Tile(0);

            String bgLine = r.readLine();
            if (bgLine != null && bgLine.startsWith("BG=")) {
                //backgroundImagePath = bgLine.substring(3);
                //loadBackgroundFromResources(backgroundImagePath);
            }

            for (int rIdx = 0; rIdx < rows; rIdx++) {
                String[] parts = r.readLine().split(" ");
                for (int cIdx = 0; cIdx < cols; cIdx++) {
                    grid[rIdx][cIdx].flags = Integer.parseInt(parts[cIdx]);
                }
            }
            map.setTiles(grid, cols, rows);
        }
    }


    // ---------------- Types ----------------

    private enum TileType {EMPTY, WALL, DOOR, DIFFICULT, HAZARD}

    private enum Tool {WALL, DOOR, DIFFICULT, HAZARD, ERASE}
}
