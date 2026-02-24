package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.Map.Core.*;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class Mapeditor implements Saveble {
    private static final Logger logger = FileLogger.getLogger();
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();

    private boolean drawBackground = true;
    private boolean panning = false;
    private double lastMouseX, lastMouseY;

    // --- Painting ---
    private Tool currentTool = Tool.WALL;
    private boolean painting = false;

    // --- UI ---
    private Label toolLabel;
    private TextField mapNameField;

    private MapRenderer mapRenderer;
    private MutableGameMap gameMap;
    private LightingSystem lightingSystem;

    public Mapeditor() {
        lightingSystem = new LightingSystem();
        gameMap = new MutableGameMap();
        this.mapRenderer = new MapRenderer(gameMap, lightingSystem);
    }

    @Override
    public Scene getScene(int stage) {
        BorderPane root = new BorderPane();

        setupInputHandlers();


        root.setLeft(buildPalette());
        root.setCenter(mapRenderer.buildCanvasPane());

        Scene scene = new Scene(root, 1400, 900);

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
        hint.setTextFill(sceneBuilder.whiteRGB);

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
                new MapSaverLoader().saveMap(out, mapRenderer.getGameMap());
                System.out.println("Saved: " + out.getAbsolutePath());
            } catch (Exception ex) {
                logger.severe("Error");
                logger.severe(ex.getMessage());
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
            gameMap.addRowTop();
            mapRenderer.render();
        });

        Button addRowBottom = SceneBuilder.getSceneBuilder().makeButton("+ Row Bottom");
        addRowBottom.setOnAction(e -> {
            gameMap.addRowBottom();
            mapRenderer.render();
        });

        Button addColLeft = SceneBuilder.getSceneBuilder().makeButton("+ Col Left");
        addColLeft.setOnAction(e -> {
            gameMap.addColLeft();
            mapRenderer.render();
        });

        Button addColRight = SceneBuilder.getSceneBuilder().makeButton("+ Col Right");
        addColRight.setOnAction(e -> {
            gameMap.addColRight();
            mapRenderer.render();
        });

        Button remRowTop = SceneBuilder.getSceneBuilder().makeButton("- Row Top");
        remRowTop.setOnAction(e -> {
            gameMap.removeRowTop();
            mapRenderer.render();
        });

        Button remRowBottom = SceneBuilder.getSceneBuilder().makeButton("- Row Bottom");
        remRowBottom.setOnAction(e -> {
            gameMap.removeRowBottom();
            mapRenderer.render();
        });

        Button remColLeft = SceneBuilder.getSceneBuilder().makeButton("- Col Left");
        remColLeft.setOnAction(e -> {
            gameMap.removeColLeft();
            mapRenderer.render();
        });

        Button remColRight = SceneBuilder.getSceneBuilder().makeButton("- Col Right");
        remColRight.setOnAction(e -> {
            gameMap.removeColRight();
            mapRenderer.render();
        });

        gridLabel.setTextFill(Color.WHITE);

        TextField rowsField = new TextField(String.valueOf(gameMap.getRows()));
        rowsField.setPrefWidth(70);

        TextField colsField = new TextField(String.valueOf(gameMap.getCols()));
        colsField.setPrefWidth(70);

        Button applySize = SceneBuilder.getSceneBuilder().makeButton("Apply");
        applySize.setOnAction(e -> {
            int newRows = parsePositiveInt(rowsField.getText(), gameMap.getRows());
            int newCols = parsePositiveInt(colsField.getText(), gameMap.getCols());

            gameMap.resizeGrid(newRows, newCols);
            mapRenderer.render();
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
        loadBg.setOnAction(e -> mapRenderer.loadBackground());

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
        Canvas canvas = mapRenderer.getCanvas();

        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Optional: mit SHIFT = Light entfernen, sonst Pan
                if (e.isShiftDown()) {
                    int col = mapRenderer.screenToCol(e.getX());
                    int row = mapRenderer.screenToRow(e.getY());
                    if (gameMap.inBounds(row, col) && gameMap.removeLightAt(row, col)) {
                        lightingSystem.recomputeLightmapAll(gameMap);
                        mapRenderer.render();
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

                mapRenderer.setCamX(mapRenderer.getCamX() - dx / mapRenderer.getZoom());
                mapRenderer.setCamY(mapRenderer.getCamY() - dy / mapRenderer.getZoom());
                mapRenderer.render();
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
            double oldZoom = mapRenderer.getZoom();
            double factor = Math.pow(1.0015, e.getDeltaY());
            mapRenderer.setZoom(clamp(mapRenderer.getZoom() * factor, 0.2, 6.0));

            double mx = e.getX();
            double my = e.getY();

            double worldXBefore = mapRenderer.screenToWorldX(mx, oldZoom);
            double worldYBefore = mapRenderer.screenToWorldY(my, oldZoom);
            double worldXAfter = mapRenderer.screenToWorldX(mx, mapRenderer.getZoom());
            double worldYAfter = mapRenderer.screenToWorldY(my, mapRenderer.getZoom());

            mapRenderer.setCamX(mapRenderer.getCamX() + (worldXBefore - worldXAfter));
            mapRenderer.setCamY(mapRenderer.getCamY() + (worldYBefore - worldYAfter));

            mapRenderer.render();
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
                    int[] rc = mapRenderer.pickTile(e.getX(), e.getY());
                    if (rc != null) {
                        gameMap.getTokens().add(new Token(rc[0], rc[1], s.substring("TOKEN:".length())));
                        mapRenderer.render();
                        ok = true;
                    }

                } else if (s.startsWith("LIGHT:")) {
                    String[] p = s.split(":");
                    int brightTiles = Integer.parseInt(p[2]);
                    int dimtiles = Integer.parseInt(p[3]);
                    float intensity = Float.parseFloat(p[4]);

                    int col = mapRenderer.screenToCol(e.getX());
                    int row = mapRenderer.screenToRow(e.getY());

                    if (gameMap.inBounds(row, col)) {
                        gameMap.addOrReplaceLight(row, col, brightTiles, dimtiles, intensity);
                        lightingSystem.recomputeLightmapAll(gameMap);
                        mapRenderer.render();
                        ok = true;
                    }
                }
            }

            e.setDropCompleted(ok);
            e.consume();
        });
    }

    private void applyAt(double sx, double sy, boolean isDragPaint) {
        int[] rc = mapRenderer.pickTile(sx, sy);
        if (rc == null) return;

        int r = rc[0], c = rc[1];
        Tile tile = gameMap.getTile(r, c);

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

        lightingSystem.recomputeLightmapAll(gameMap);
        mapRenderer.render();
    }


    @Override
    public void reset() {

    }

    // ---------------- Row Buttons ----------------
    private int parsePositiveInt(String s, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }


    // ---------------- Types ----------------
    private enum Tool {WALL, DOOR, DIFFICULT, HAZARD, ERASE}
}
