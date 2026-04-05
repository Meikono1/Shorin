package com.fuchsbau.shorin.Engine.Editor.Module.BattleMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import com.fuchsbau.shorin.Engine.Map.Core.*;
import com.fuchsbau.shorin.Engine.Map.LightPreset;
import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Engine.RPG.GameClock;
import com.fuchsbau.shorin.Engine.System.NpcBuild;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class BattleMapModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();

    private final MutableGameMap gameMap = new MutableGameMap();
    private final LightingSystem lighting = new LightingSystem();
    private final MapRenderer mapRenderer = new MapRenderer(gameMap, lighting);

    // Camera
    private double lastMouseX, lastMouseY;
    private boolean panning = false;
    private boolean painting = false;

    // Tool
    private final ToggleGroup sidebarGroup = new ToggleGroup();
    private final BorderPane sidebarContainer = new BorderPane();
    private Tool currentTool = Tool.WALL;
    private Label toolLabel;
    private final Map<String, NpcBuild> npcs = new HashMap<>();
    private final ObservableList<NpcBuild> npcBuildObservableList = FXCollections.observableArrayList();
    private boolean drawingActive = false;
    private boolean tokensActive = false;
    private Token selectedToken = null;

    // Maps
    private static final File MAPS_DIR = PathResolver.resolveWritable("maps/battle").toFile();
    private final ObservableList<String> mapFiles = FXCollections.observableArrayList();
    private ListView<String> mapListView;
    private String backgroundImage;

    // Lights
    private TextField lightNameField;
    private Spinner<Integer> lightBrightSpinner;
    private Spinner<Integer> lightDimSpinner;
    private Spinner<Double> lightIntSpinner;
    private LightSource selectedLight = null;
    private ListView<LightSource> lightListView;

    private TextField mapNameField = new TextField("neue_karte");

    // Sound
    private SoundPoint selectedSound = null;
    private ListView<SoundPoint> soundListView;
    private Spinner<Integer> soundRadiusSpinner;
    private Spinner<Double> soundVolumeSpinner;
    private Spinner<Double> soundEasingSpinner;
    private Spinner<Double> soundMinLightSpinner;
    private CheckBox soundLoopBox;
    private CheckBox soundConstrainBox;
    private CheckBox soundLightBox;
    private Label soundPathLabel;

    private final ObservableList<SoundPoint> soundPoints = FXCollections.observableArrayList();


    @Override
    public String getTitle() {
        return "BattleMap";
    }

    private void setupInputHandlers() {
        Canvas canvas = mapRenderer.getCanvas();

        // --- Mouse Press ---
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                if (e.isShiftDown()) {
                    // Shift + Rechtsklick = Licht entfernen
                    boolean removed = gameMap.removeLightNear(
                            e.getX(), e.getY(),
                            mapRenderer.getCamX(), mapRenderer.getCamY(),
                            mapRenderer.getZoom(), 10.0);
                    if (removed) {
                        lighting.recomputeLightmapAll(gameMap);
                        mapRenderer.renderBattlemap();
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
                // Token-Auswahl immer prüfen — unabhängig vom Panel
                Token hit = pickToken(e.getX(), e.getY());
                if (hit != null) {
                    selectedToken = hit;
                    mapRenderer.setSelectedToken(selectedToken);
                    mapRenderer.renderBattlemap();
                    e.consume();
                    return;
                }

                // Zeichnen nur wenn Draw-Panel aktiv
                if (drawingActive) {
                    painting = true;
                    applyAt(e.getX(), e.getY());
                }
                e.consume();
            }
        });

        // --- Mouse Released ---
        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.SECONDARY) panning = false;
            if (e.getButton() == MouseButton.PRIMARY) painting = false;
            e.consume();
        });

        // --- Mouse Dragged ---
        canvas.setOnMouseDragged(e -> {
            if (panning && e.isSecondaryButtonDown()) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                mapRenderer.setCamX(mapRenderer.getCamX() - dx / mapRenderer.getZoom());
                mapRenderer.setCamY(mapRenderer.getCamY() - dy / mapRenderer.getZoom());
                mapRenderer.renderBattlemap();
                e.consume();
                return;
            }
            if (painting && e.isPrimaryButtonDown()) {
                applyAt(e.getX(), e.getY());
                e.consume();
            }
            if (tokensActive && selectedToken != null && e.isPrimaryButtonDown() && !painting) {
                int[] rc = mapRenderer.pickTile(e.getX(), e.getY());
                if (rc != null && gameMap.inBounds(rc[0], rc[1])) {
                    selectedToken.row = rc[0];
                    selectedToken.col = rc[1];
                    mapRenderer.renderBattlemap();
                }
                e.consume();
            }
        });

        // --- Zoom ---
        canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            double oldZoom = mapRenderer.getZoom();
            double factor = Math.pow(1.0015, e.getDeltaY());
            mapRenderer.setZoom(clamp(mapRenderer.getZoom() * factor, 0.2, 6.0));

            double wx = mapRenderer.screenToWorldX(e.getX(), oldZoom);
            double wy = mapRenderer.screenToWorldY(e.getY(), oldZoom);
            double wxA = mapRenderer.screenToWorldX(e.getX(), mapRenderer.getZoom());
            double wyA = mapRenderer.screenToWorldY(e.getY(), mapRenderer.getZoom());

            mapRenderer.setCamX(mapRenderer.getCamX() + (wx - wxA));
            mapRenderer.setCamY(mapRenderer.getCamY() + (wy - wyA));
            mapRenderer.renderBattlemap();
            e.consume();
        });

        // --- Drag Over ---
        canvas.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (!db.hasString()) {
                e.consume();
                return;
            }
            String s = db.getString();
            if (s.startsWith("TOOL:") || s.startsWith("NPC:") || s.startsWith("CHAR:") || s.startsWith("LIGHT:"))
                e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });

        // --- Drag Dropped ---
        canvas.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean ok = false;

            if (db.hasString()) {
                String s = db.getString();

                if (s.startsWith("TOOL:")) {
                    currentTool = Tool.valueOf(s.substring("TOOL:".length()));
                    if (toolLabel != null) toolLabel.setText("Tool: " + currentTool);
                    ok = true;

                } else if (s.startsWith("NPC:") || s.startsWith("CHAR:")) {
                    int[] rc = mapRenderer.pickTile(e.getX(), e.getY());
                    if (rc != null) {
                        gameMap.getTokens().add(new Token(rc[0], rc[1], npcs.get(s.split(":")[1])));
                        mapRenderer.renderBattlemap();
                        logger.fine("Token platziert: " + s + " @ " + rc[0] + "/" + rc[1]);
                        ok = true;
                    }

                } else if (s.startsWith("LIGHT:")) {
                    String[] parts = s.split(":");
                    if (parts.length == 5) {
                        int[] rc = mapRenderer.pickTile(e.getX(), e.getY());
                        if (rc != null) {
                            double wx = mapRenderer.screenToWorldX(e.getX(), mapRenderer.getZoom());
                            double wy = mapRenderer.screenToWorldY(e.getY(), mapRenderer.getZoom());
                            int bright = Integer.parseInt(parts[2]);
                            int dim = Integer.parseInt(parts[3]);
                            float intens = Float.parseFloat(parts[4]);
                            gameMap.addOrReplaceLight(wx, wy, bright, dim, intens);
                            lighting.recomputeLightmapAll(gameMap);
                            mapRenderer.renderBattlemap();
                            // ListView aktualisieren wenn Light-Panel aktiv
                            if (lightListView != null)
                                lightListView.getItems().setAll(gameMap.getLights());
                            ok = true;
                        }
                    }
                }
            }
            e.setDropCompleted(ok);
            e.consume();
        });

        // Pfeiltasten auf Canvas
        mapRenderer.getCanvas().setFocusTraversable(true);
        mapRenderer.getCanvas().setOnKeyPressed(e -> {
            if (selectedToken == null || !tokensActive) return;
            int dr = 0, dc = 0;
            switch (e.getCode()) {
                case UP -> dr = -1;
                case DOWN -> dr = 1;
                case LEFT -> dc = -1;
                case RIGHT -> dc = 1;
                default -> {
                    return;
                }
            }
            int newRow = selectedToken.row + dr;
            int newCol = selectedToken.col + dc;
            if (gameMap.inBounds(newRow, newCol)) {
                selectedToken.row = newRow;
                selectedToken.col = newCol;
                mapRenderer.renderBattlemap();
                logger.fine("Token bewegt: " + selectedToken.name + " → " + newRow + "/" + newCol);
            }
            e.consume();
        });

        logger.fine("Input Handler registriert");
    }

    private void applyAt(double sx, double sy) {
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
            case DIFFICULT -> tile.add(Tile.DIFFICULT);
            case HAZARD -> tile.add(Tile.HAZARDOUS);
            case DISABLED -> tile.add(Tile.DISABLED);
            case ERASE -> tile.clearAll();
        }

        mapRenderer.renderBattlemap();
    }

    @Override
    public Node buildContent() {
        Node canvas = mapRenderer.buildBattleMapPane(null);
        setupInputHandlers();
        logger.info("BattleMapModule Content gebaut");
        return canvas;
    }

    @Override
    public Node buildToolbar() {
        // --- Kartenverwaltung ---
        mapNameField = new TextField("neue_karte");
        mapNameField.setPromptText("Kartenname...");
        mapNameField.setPrefWidth(160);

        Button newBtn = new Button("Neu");
        Button saveBtn = new Button("Speichern");
        Button loadBtn = new Button("Laden");

        newBtn.setOnAction(e -> newMap());
        saveBtn.setOnAction(e -> saveMap());
        loadBtn.setOnAction(e -> loadMap());

        // --- Sidebar-Switcher ---
        ToggleButton mapsBtn = makeSidebarBtn("Karten", "MAPS");
        ToggleButton drawBtn = makeSidebarBtn("Zeichnen", "DRAW");
        ToggleButton lightBtn = makeSidebarBtn("Licht", "LIGHT");
        ToggleButton tokenBtn = makeSidebarBtn("Tokens", "TOKENS");
        ToggleButton soundBtn = makeSidebarBtn("Sound", "SOUND");

        // Standard: Zeichnen
        mapsBtn.setSelected(true);

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        HBox toolbar = new HBox(6,
                mapNameField, newBtn, saveBtn, loadBtn,
                sep,
                mapsBtn, drawBtn, lightBtn, tokenBtn, soundBtn
        );
        toolbar.setPadding(new Insets(4));
        return toolbar;
    }

    private ToggleButton makeSidebarBtn(String label, String mode) {
        ToggleButton btn = new ToggleButton(label);
        btn.setToggleGroup(sidebarGroup);
        btn.setOnAction(e -> switchSidebar(mode));
        return btn;
    }

    private Node buildMapsPanel() {
        // --- Kartenverwaltung ---
        mapNameField.setPromptText("Kartenname...");
        mapNameField.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveMap());

        Button loadBtn = new Button("Laden");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadMap());

        Button newBtn = new Button("Neue Karte");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> newMap());

        Button loadBgBtn = new Button("Hintergrundbild");
        loadBgBtn.setMaxWidth(Double.MAX_VALUE);
        loadBgBtn.setOnAction(e -> {
            String path = mapRenderer.loadBackground();
            if (path == null) return;
            backgroundImage = path;

            mapRenderer.renderBattlemap();
        });

        // --- Kartenliste ---
        Label listLabel = new Label("Verfügbare Karten");

        mapListView = new ListView<>(mapFiles);
        mapListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selected = mapListView.getSelectionModel().getSelectedItem();
                if (selected != null) loadMapByName(selected);
            }
        });
        VBox.setVgrow(mapListView, Priority.ALWAYS);

        Button refreshBtn = new Button("Aktualisieren");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshMapList());

        VBox panel = new VBox(6,
                mapNameField, saveBtn, loadBtn, newBtn, loadBgBtn,
                new Separator(),
                listLabel, mapListView, refreshBtn
        );
        panel.setPadding(new Insets(8));
        panel.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(mapListView, Priority.ALWAYS);

        refreshMapList();
        return panel;
    }

    private void refreshMapList() {
        mapFiles.clear();
        if (!MAPS_DIR.exists()) {
            MAPS_DIR.mkdirs();
            return;
        }
        File[] files = MAPS_DIR.listFiles();
        if (files == null) return;
        for (File f : files) {
            mapFiles.add(f.getName());
        }
        logger.fine("Kartenliste aktualisiert: " + mapFiles.size());
    }

    private void loadMapByName(String fileName) {
        File file = new File(MAPS_DIR, fileName);
        if (!file.exists()) return;
        try {
            MapSaverLoader.LoadResult result = new MapSaverLoader().load(file);
            gameMap.resizeGrid(result.map.getRows(), result.map.getCols());
            for (int r = 0; r < result.map.getRows(); r++)
                for (int c = 0; c < result.map.getCols(); c++)
                    gameMap.getTile(r, c).flags = result.map.getTile(r, c).flags;
            gameMap.getTokens().clear();
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
            mapNameField.setText(fileName.replace(".shorin", ""));
            logger.info("Karte geladen: " + fileName);
        } catch (Exception ex) {
            logger.severe("Fehler beim Laden: " + ex.getMessage());
        }
    }

    private void newMap() {
        gameMap.resizeGrid(20, 20);
        gameMap.clearAll();
        gameMap.getTokens().clear();
        mapNameField.setText("neue_karte");
        lighting.recomputeLightmapAll(gameMap);
        mapRenderer.renderBattlemap();
        logger.info("Neue BattleMap erstellt");
    }

    private void saveMap() {
        String name = mapNameField.getText().trim();
        if (name.isBlank()) return;
        if (!name.endsWith(".shorin")) name += ".shorin";

        try {
            File dir = PathResolver.resolveWritable("maps/battle").toFile();
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, name);
            new MapSaverLoader().saveMap(out, gameMap, lighting);
            logger.info("BattleMap gespeichert: " + out.getAbsolutePath());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadMap() {
        FileChooser fc = new FileChooser();
        fc.setTitle("BattleMap laden");
        fc.setInitialDirectory(MAPS_DIR.exists() ? MAPS_DIR : new File("."));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Shorin Maps", "*.shorin"));

        File file = fc.showOpenDialog(mapRenderer.getCanvas().getScene().getWindow());
        if (file == null) return;
        loadMapByName(file.getName());
        refreshMapList();
    }

    @Override
    public Node buildSidePanel() {
        sidebarContainer.setMaxHeight(Double.MAX_VALUE);
        sidebarContainer.setCenter(buildMapsPanel());
        return sidebarContainer;
    }

    private void switchSidebar(String mode) {
        drawingActive = mode.equals("DRAW");
        tokensActive = mode.equals("TOKENS");
        Node panel = switch (mode) {
            case "MAPS" -> buildMapsPanel();
            case "DRAW" -> buildDrawPanel();
            case "LIGHT" -> buildLightPanel();
            case "TOKENS" -> buildTokenPanel();
            case "SOUND" -> buildSoundPanel();
            default -> new Label("?");
        };
        sidebarContainer.setCenter(panel);
        logger.fine("Sidebar gewechselt: " + mode);
    }

    private Button makeToolBtn(String name, Tool tool) {
        Button btn = new Button(name);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            currentTool = tool;
            toolLabel.setText("Tool: " + tool);
            logger.fine("Tool gewechselt: " + tool);
        });
        btn.setOnDragDetected(e -> {
            Dragboard db = btn.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("TOOL:" + tool.name());
            db.setContent(cc);
            e.consume();
        });
        return btn;
    }

    private Node buildTokenPanel() {
        // --- NPCs ---
        Label npcLabel = new Label("NPCs");

        TextField npcSearch = new TextField();
        npcSearch.setPromptText("NPC suchen...");

        FilteredList<NpcBuild> filtered = new FilteredList<>(npcBuildObservableList, n -> true);
        npcSearch.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(n ->
                        nv == null || nv.isBlank() ||
                                n.name.toLowerCase().contains(nv.toLowerCase()) ||
                                String.valueOf(n.level).contains(nv)));

        ListView<NpcBuild> npcList = new ListView<>(filtered);
        npcList.setPrefHeight(200);
        npcList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NpcBuild n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setText(null);
                    return;
                }
                setText("[" + n.level + "] " + n.name);
            }
        });
        npcList.setOnDragDetected(e -> {
            NpcBuild selected = npcList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Dragboard db = npcList.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("NPC:" + selected.name);
            db.setContent(cc);
            logger.fine("NPC Drag: " + selected.name);
            e.consume();
        });

        // --- Charaktere ---
        Label charLabel = new Label("Charaktere");

        ObservableList<String> chars = FXCollections.observableArrayList();
        ListView<String> charList = new ListView<>(chars);
        charList.setPrefHeight(120);
        charList.setOnDragDetected(e -> {
            String selected = charList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Dragboard db = charList.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("CHAR:" + selected);
            db.setContent(cc);
            logger.fine("Char Drag: " + selected);
            e.consume();
        });

        Button refreshCharsBtn = new Button("Aktualisieren");
        refreshCharsBtn.setMaxWidth(Double.MAX_VALUE);
        refreshCharsBtn.setOnAction(e -> loadChars(chars));

        // --- Platzierte Tokens ---
        Label placedLabel = new Label("Platziert");

        ListView<Token> placedList = new ListView<>();
        placedList.setPrefHeight(120);
        placedList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Token t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setText(null);
                    return;
                }
                setText(t.name + " @ " + t.row + "/" + t.col);
            }
        });
        placedList.getItems().setAll(gameMap.getTokens());

        Button removeTokenBtn = new Button("✕ Entfernen");
        removeTokenBtn.setMaxWidth(Double.MAX_VALUE);
        removeTokenBtn.setOnAction(e -> {
            Token selected = placedList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            gameMap.getTokens().remove(selected);
            placedList.getItems().remove(selected);
            mapRenderer.renderBattlemap();
            logger.fine("Token entfernt: " + selected.name);
        });

        // Chars laden
        loadChars(chars);

        VBox panel = new VBox(8,
                npcLabel, npcSearch, npcList,
                new Separator(),
                charLabel, charList, refreshCharsBtn,
                new Separator(),
                placedLabel, placedList, removeTokenBtn
        );
        panel.setPadding(new Insets(8));
        VBox.setVgrow(npcList, Priority.ALWAYS);
        return panel;
    }

    private Token pickToken(double sx, double sy) {
        int[] rc = mapRenderer.pickTile(sx, sy);
        if (rc == null) return null;

        for (Token t : gameMap.getTokens()) {
            if (t.row == rc[0] && t.col == rc[1]) return t;
        }
        return null;
    }

    private void loadChars(ObservableList<String> chars) {
        chars.clear();
        chars.add("(noch keine Charaktere)");
        logger.fine("Charaktere geladen: " + chars.size());
    }

    private Node buildDrawPanel() {
        toolLabel = new Label("Tool: " + currentTool);

        VBox tools = new VBox(4,
                toolLabel,
                new Separator(),
                makeToolBtn("Wall", Tool.WALL),
                makeToolBtn("Door", Tool.DOOR),
                makeToolBtn("Difficult", Tool.DIFFICULT),
                makeToolBtn("Hazard", Tool.HAZARD),
                makeToolBtn("Disabled", Tool.DISABLED),
                makeToolBtn("Erase", Tool.ERASE)
        );

        // --- Grid Controls ---
        Label gridLabel = new Label("Grid");

        TextField rowsField = new TextField(String.valueOf(gameMap.getRows()));
        rowsField.setPrefWidth(55);
        TextField colsField = new TextField(String.valueOf(gameMap.getCols()));
        colsField.setPrefWidth(55);

        Button applySize = new Button("Apply");
        applySize.setOnAction(e -> {
            int rows = parsePositiveInt(rowsField.getText(), gameMap.getRows());
            int cols = parsePositiveInt(colsField.getText(), gameMap.getCols());
            gameMap.resizeGrid(rows, cols);
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
            logger.fine("Grid angepasst: " + rows + "x" + cols);
        });

        HBox sizeRow = new HBox(4,
                new Label("R:"), rowsField,
                new Label("C:"), colsField,
                applySize
        );

        VBox gridSection = new VBox(4,
                sizeRow,
                makeGridBtn("+ Oben", () -> {
                    gameMap.addRowTop();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                makeGridBtn("+ Unten", () -> {
                    gameMap.addRowBottom();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                makeGridBtn("+ Links", () -> {
                    gameMap.addColLeft();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                makeGridBtn("+ Rechts", () -> {
                    gameMap.addColRight();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                new Separator(),
                makeGridBtn("- Oben", () -> {
                    gameMap.removeRowTop();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                makeGridBtn("- Unten", () -> {
                    gameMap.removeRowBottom();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                makeGridBtn("- Links", () -> {
                    gameMap.removeColLeft();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                }),
                makeGridBtn("- Rechts", () -> {
                    gameMap.removeColRight();
                    lighting.recomputeLightmapAll(gameMap);
                    mapRenderer.renderBattlemap();
                })
        );

        VBox panel = new VBox(8, tools, new Separator(), gridLabel, gridSection);
        panel.setPadding(new Insets(8));
        return panel;
    }

    private Button makeGridBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private int parsePositiveInt(String s, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private Node buildLightPanel() {
        // --- Uhrzeit-Anzeige ---
        Label timeLabel = new Label();
        timeLabel.textProperty().bind(GameClock.getInstance().timeStringProperty());

        Button plusHalfBtn = new Button("+30min");
        plusHalfBtn.setOnAction(e -> {
            GameClock.getInstance().setTotalTurns(
                    GameClock.getInstance().getTotalTurns() + GameClock.TURNS_PER_HOUR / 2.0);
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
        });

        Button plusHourBtn = new Button("+1h");
        plusHourBtn.setOnAction(e -> {
            GameClock.getInstance().setTotalTurns(
                    GameClock.getInstance().getTotalTurns() + GameClock.TURNS_PER_HOUR);
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
        });

        Button minusHalfBtn = new Button("-30min");
        minusHalfBtn.setOnAction(e -> {
            GameClock.getInstance().setTotalTurns(
                    Math.max(0, GameClock.getInstance().getTotalTurns() - GameClock.TURNS_PER_HOUR / 2.0));
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
        });

        Button minusHourBtn = new Button("-1h");
        minusHourBtn.setOnAction(e -> {
            GameClock.getInstance().setTotalTurns(
                    Math.max(0, GameClock.getInstance().getTotalTurns() - GameClock.TURNS_PER_HOUR));
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
        });

        HBox timeControls = new HBox(4, minusHourBtn, minusHalfBtn, plusHalfBtn, plusHourBtn);

        // --- Tageslicht ---
        Label daylightLabel = new Label("Tageslicht");

        CheckBox daylightBox = new CheckBox("Tageslicht aktiv");
        daylightBox.setSelected(GameClock.getInstance().getHour() == 16);
        daylightBox.setOnAction(e -> {
            int hour = daylightBox.isSelected() ? 16 : 0;
            double turns = hour * GameClock.TURNS_PER_HOUR;
            GameClock.getInstance().setTotalTurns(turns);
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
            logger.fine("Tageslicht: " + hour + " Uhr");
        });

        // --- Prefabs ---
        Label prefabLabel = new Label("Prefabs");
        VBox prefabs = new VBox(4);
        for (LightPreset preset : LightPreset.values()) {
            Button btn = new Button(preset.label
                    + "  " + preset.brightFt + "/" + preset.dimFt + "ft");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnDragDetected(e -> {
                Dragboard db = btn.startDragAndDrop(TransferMode.COPY);
                ClipboardContent cc = new ClipboardContent();
                cc.putString("LIGHT:" + preset.name()
                        + ":" + preset.brightTiles()
                        + ":" + preset.dimTiles()
                        + ":" + preset.intensity);
                db.setContent(cc);
                e.consume();
            });
            prefabs.getChildren().add(btn);
        }

        // --- Platzierte Lichter ---
        Label placedLabel = new Label("Platziert");

        lightListView = new ListView<>();
        lightListView.setPrefHeight(120);
        lightListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LightSource ls, boolean empty) {
                super.updateItem(ls, empty);
                if (empty || ls == null) {
                    setText(null);
                    return;
                }
                setText(ls.label.isBlank()
                        ? ls.brightTiles + "/" + ls.dimTiles + "t"
                        : ls.label + " (" + ls.brightTiles + "/" + ls.dimTiles + "t)");
            }
        });
        lightListView.getItems().setAll(gameMap.getLights());
        lightListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, ov, nv) -> loadLightIntoForm(nv));

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelectedLight());

        // --- Parameter ---
        Label paramLabel = new Label("Parameter");

        Label nameLabel = new Label("Name");
        TextField nameField = new TextField();
        nameField.setPromptText("optional...");
        nameField.setMaxWidth(Double.MAX_VALUE);

        Label brightLabel = new Label("Bright (tiles)");
        Spinner<Integer> brightSpinner = new Spinner<>(0, 99, 4);
        brightSpinner.setEditable(true);
        brightSpinner.setMaxWidth(Double.MAX_VALUE);

        Label dimLabel = new Label("Dim (tiles)");
        Spinner<Integer> dimSpinner = new Spinner<>(0, 99, 8);
        dimSpinner.setEditable(true);
        dimSpinner.setMaxWidth(Double.MAX_VALUE);

        Label intensityLabel = new Label("Intensität");
        Spinner<Double> intensitySpinner = new Spinner<>(0.1, 3.0, 1.0, 0.1);
        intensitySpinner.setEditable(true);
        intensitySpinner.setMaxWidth(Double.MAX_VALUE);

        Button applyBtn = new Button("Anwenden");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setOnAction(e -> {
            if (selectedLight == null) return;
            selectedLight.label = nameField.getText().trim();
            selectedLight.brightTiles = brightSpinner.getValue();
            selectedLight.dimTiles = dimSpinner.getValue();
            selectedLight.intensity = intensitySpinner.getValue().floatValue();
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
            lightListView.refresh();
            logger.fine("Licht angepasst: " + selectedLight.label);
        });

        this.lightNameField = nameField;
        this.lightBrightSpinner = brightSpinner;
        this.lightDimSpinner = dimSpinner;
        this.lightIntSpinner = intensitySpinner;

        VBox paramSection = new VBox(4,
                paramLabel, new Separator(),
                nameLabel, nameField,
                brightLabel, brightSpinner,
                dimLabel, dimSpinner,
                intensityLabel, intensitySpinner,
                applyBtn
        );

        VBox panel = new VBox(8,
                daylightLabel, daylightBox, timeLabel, timeControls,
                new Separator(),
                new Separator(),
                prefabLabel, prefabs,
                new Separator(),
                placedLabel, lightListView, deleteBtn,
                new Separator(),
                paramSection
        );
        panel.setPadding(new Insets(8));
        return panel;
    }

    private void loadLightIntoForm(LightSource ls) {
        selectedLight = ls;
        if (ls == null) return;
        lightNameField.setText(ls.label);
        lightBrightSpinner.getValueFactory().setValue(ls.brightTiles);
        lightDimSpinner.getValueFactory().setValue(ls.dimTiles);
        lightIntSpinner.getValueFactory().setValue((double) ls.intensity);
    }

    private void deleteSelectedLight() {
        if (selectedLight == null) return;
        gameMap.getLights().remove(selectedLight);
        lightListView.getItems().remove(selectedLight);
        selectedLight = null;
        lighting.recomputeLightmapAll(gameMap);
        mapRenderer.renderBattlemap();
        logger.fine("Licht gelöscht");
    }

    private Node buildSoundPanel() {
        // --- Liste ---
        Label listLabel = new Label("Soundpunkte");

        soundListView = new ListView<>(soundPoints);
        soundListView.setPrefHeight(120);
        soundListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SoundPoint s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    return;
                }
                String file = s.soundPath.isBlank() ? "(kein Sound)"
                        : s.soundPath.substring(s.soundPath.lastIndexOf('/') + 1);
                setText(file + " r:" + s.radiusTiles);
            }
        });
        soundListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, ov, nv) -> loadSoundIntoForm(nv));

        Button addBtn = new Button("+ Hinzufügen");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            SoundPoint sp = new SoundPoint();
            soundPoints.add(sp);
            soundListView.getSelectionModel().select(sp);
            logger.fine("Soundpunkt hinzugefügt");
        });

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            if (selectedSound == null) return;
            soundPoints.remove(selectedSound);
            selectedSound = null;
            logger.fine("Soundpunkt gelöscht");
        });

        HBox btnRow = new HBox(4, addBtn, deleteBtn);

        // --- Parameter ---
        Label paramLabel = new Label("Parameter");

        // Sound Datei
        soundPathLabel = new Label("(kein Sound)");
        soundPathLabel.setWrapText(true);

        Button chooseSound = new Button("Sound auswählen...");
        chooseSound.setMaxWidth(Double.MAX_VALUE);
        chooseSound.setOnAction(e -> chooseSoundFile());

        // Radius
        soundRadiusSpinner = new Spinner<>(1, 50, 5);
        soundRadiusSpinner.setEditable(true);
        soundRadiusSpinner.setMaxWidth(Double.MAX_VALUE);
        soundRadiusSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedSound != null) selectedSound.radiusTiles = nv;
        });

        // Lautstärke
        soundVolumeSpinner = new Spinner<>(0.0, 1.0, 1.0, 0.05);
        soundVolumeSpinner.setEditable(true);
        soundVolumeSpinner.setMaxWidth(Double.MAX_VALUE);
        soundVolumeSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedSound != null) selectedSound.volume = nv.floatValue();
        });

        // Easing — Lautstärke am Rand
        soundEasingSpinner = new Spinner<>(0.0, 1.0, 0.0, 0.05);
        soundEasingSpinner.setEditable(true);
        soundEasingSpinner.setMaxWidth(Double.MAX_VALUE);
        soundEasingSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedSound != null) selectedSound.easing = nv.floatValue();
        });

        // Loop
        soundLoopBox = new CheckBox("Loop");
        soundLoopBox.setOnAction(e -> {
            if (selectedSound != null) selectedSound.loop = soundLoopBox.isSelected();
        });

        // Constrain by Wall
        soundConstrainBox = new CheckBox("Wände blockieren Sound");
        soundConstrainBox.setOnAction(e -> {
            if (selectedSound != null) selectedSound.constrainByWall = soundConstrainBox.isSelected();
        });

        // Requires Light
        soundLightBox = new CheckBox("Benötigt Licht");
        soundLightBox.setOnAction(e -> {
            if (selectedSound != null) {
                selectedSound.requiresLight = soundLightBox.isSelected();
                soundMinLightSpinner.setDisable(!soundLightBox.isSelected());
            }
        });

        // Min Light Level
        soundMinLightSpinner = new Spinner<>(0.0, 1.0, 0.0, 0.05);
        soundMinLightSpinner.setEditable(true);
        soundMinLightSpinner.setMaxWidth(Double.MAX_VALUE);
        soundMinLightSpinner.setDisable(true);
        soundMinLightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedSound != null) selectedSound.minLightLevel = nv.floatValue();
        });

        VBox paramSection = new VBox(6,
                paramLabel, new Separator(),
                new Label("Sound"), soundPathLabel, chooseSound,
                new Label("Radius (tiles)"), soundRadiusSpinner,
                new Label("Lautstärke"), soundVolumeSpinner,
                new Label("Easing (Rand)"), soundEasingSpinner,
                soundLoopBox,
                soundConstrainBox,
                soundLightBox,
                new Label("Min. Lichtlevel"), soundMinLightSpinner
        );

        ScrollPane paramScroll = new ScrollPane(paramSection);
        paramScroll.setFitToWidth(true);
        VBox.setVgrow(paramScroll, Priority.ALWAYS);

        VBox panel = new VBox(8,
                listLabel, soundListView, btnRow,
                new Separator(),
                paramScroll
        );
        panel.setPadding(new Insets(8));
        panel.setMaxHeight(Double.MAX_VALUE);
        return panel;
    }

    private void loadSoundIntoForm(SoundPoint sp) {
        selectedSound = sp;
        if (sp == null) return;

        soundPathLabel.setText(sp.soundPath.isBlank() ? "(kein Sound)"
                : sp.soundPath.substring(sp.soundPath.lastIndexOf('/') + 1));
        soundRadiusSpinner.getValueFactory().setValue(sp.radiusTiles);
        soundVolumeSpinner.getValueFactory().setValue((double) sp.volume);
        soundEasingSpinner.getValueFactory().setValue((double) sp.easing);
        soundLoopBox.setSelected(sp.loop);
        soundConstrainBox.setSelected(sp.constrainByWall);
        soundLightBox.setSelected(sp.requiresLight);
        soundMinLightSpinner.setDisable(!sp.requiresLight);
        soundMinLightSpinner.getValueFactory().setValue((double) sp.minLightLevel);
        logger.fine("Sound geladen: " + sp.soundPath);
    }

    private void chooseSoundFile() {
        if (selectedSound == null) return;

        File initialDir = PathResolver.resolveWritable("sounds").toFile();
        if (!initialDir.exists()) initialDir = PathResolver.resolveWritable("").toFile();

        FileChooser fc = new FileChooser();
        fc.setTitle("Sound auswählen");
        fc.setInitialDirectory(initialDir.exists() ? initialDir : new File("."));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.ogg"));

        File file = fc.showOpenDialog(null);
        if (file == null) return;

        // Relativen Pfad ab user.dir speichern
        Path base = PathResolver.resolveWritable("");
        Path rel = base.relativize(file.toPath().toAbsolutePath());
        selectedSound.soundPath = rel.toString().replace("\\", "/");
        soundPathLabel.setText(file.getName());
        logger.info("Sound gesetzt: " + selectedSound.soundPath);
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    @Override
    public void onActivate() {
        loadNpcs();
        mapRenderer.renderBattlemap();
        logger.info("BattleMapModule aktiviert");
    }

    private void loadNpcs() {
        npcBuildObservableList.clear();
        List<NpcBuild> loaded = EditorIO.load("Ingame/npcs.json",
                new TypeReference<>() {
                }, new ArrayList<>());
        for (NpcBuild npcBuild : loaded) {
            npcs.put(npcBuild.name, npcBuild);
        }
        npcBuildObservableList.addAll(loaded);
        logger.info("NPCs geladen: " + npcs.size());
    }
}
