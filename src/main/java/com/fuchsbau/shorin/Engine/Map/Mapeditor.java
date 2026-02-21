package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.RPG.Saveble;
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

        Button loadBg = new Button("Load Background...");
        loadBg.setOnAction(e -> loadBackground());

        VBox gridControls = new VBox(4,
                addRowTop, addRowBottom,
                addColLeft, addColRight,
                new Separator(),
                remRowTop, remRowBottom,
                remColLeft, remColRight,
                loadBg
        );


        box.getChildren().addAll(gridLabel, gridControls, new Separator());

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

    // ---------------- Input ----------------

    private void setupInputHandlers() {
        // Pan: right mouse
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                panning = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                painting = true;
                applyAt(e.getX(), e.getY(), false);
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.SECONDARY) panning = false;
            if (e.getButton() == MouseButton.PRIMARY) painting = false;
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
                return;
            }

            if (painting && e.isPrimaryButtonDown()) {
                applyAt(e.getX(), e.getY(), true);
            }
        });

        // Zoom: wheel
        canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            double oldZoom = zoom;
            double factor = Math.pow(1.0015, e.getDeltaY());
            zoom = clamp(zoom * factor, 0.2, 6.0);

            // zoom around cursor
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

        // Drag&Drop onto canvas
        canvas.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString()) e.acceptTransferModes(TransferMode.COPY);
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
                if (grid[r][c].flags == 0) continue;

                double xWorld = c * BASE_TILE;
                double x = Math.floor((xWorld - camX) * zoom);

                g.setFill(colorFor(grid[r][c]));
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


    // ---------------- Types ----------------

    private enum TileType {EMPTY, WALL, DOOR, DIFFICULT, HAZARD}

    private enum Tool {WALL, DOOR, DIFFICULT, HAZARD, ERASE}

    private static final class Token {
        final int row, col;
        final String name;

        Token(int row, int col, String name) {
            this.row = row;
            this.col = col;
            this.name = name;
        }
    }
}
