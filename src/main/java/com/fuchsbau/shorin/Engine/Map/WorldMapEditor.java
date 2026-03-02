package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.Map.Core.*;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.MainScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Options.GameOptions.BASE_TILE;
import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class WorldMapEditor implements Saveble {
    private final Logger logger = FileLogger.getLogger();

    private final MapRenderer mapRenderer;
    private final MutableGameMap gameMap;
    private final LightingSystem lightingSystem;
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();

    // Camera
    private double lastMouseX, lastMouseY;
    private boolean panning = false;

    // Tools
    private Tool currentTool = Tool.WALL;
    private boolean painting = false;

    // Locations
    private final List<MapSaverLoader.LocationMarker> locations = new ArrayList<>();
    private MapSaverLoader.LocationMarker selectedLocation = null;

    // Map-Liste
    private static final File MAPS_DIR = new File("maps/overworld");
    private final ObservableList<String> mapFileNames = FXCollections.observableArrayList();
    private ListView<String> mapListView;

    // UI
    private Label toolLabel;
    private final TextField mapSavingName = new TextField("world");

    // Zones
    private final List<LocationZone> zones = new ArrayList<>();
    private LocationZone selectedZone = null;
    private int zoneStartRow = -1, zoneStartCol = -1;
    private int zonePreviewRow = -1, zonePreviewCol = -1;
    private Label zoneNameDisplay;

    // Trigger
    private ListView<ZoneTrigger> triggerListView;
    private ComboBox<ZoneTrigger.TriggerType> triggerTypeCombo;
    private TextField triggerParam1Field, triggerParam2Field, triggerParam3Field;
    private Label param1Label, param2Label, param3Label;

    // Konstruktor ────────────────────────────────────────────────────────────
    public WorldMapEditor() {
        this.gameMap = new MutableGameMap();
        this.lightingSystem = new LightingSystem();
        this.mapRenderer = new MapRenderer(gameMap, lightingSystem);
        this.mapRenderer.debug = true;
    }

    // --- INPUT ---
    private void setupInputHandlers() {
        Canvas canvas = mapRenderer.getCanvas();

        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                panning = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                e.consume();
                return;
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                switch (currentTool) {
                    case LOCATION_SELECT -> selectLocationAt(e.getX(), e.getY());
                    case ZONE -> {
                        int[] rc = mapRenderer.pickTile(e.getX(), e.getY());
                        if (rc != null) {
                            zoneStartRow = rc[0];
                            zoneStartCol = rc[1];
                            zonePreviewRow = rc[0];
                            zonePreviewCol = rc[1];
                        }
                    }
                    case ZONE_SELECT -> selectZoneAt(e.getX(), e.getY());
                    default -> {
                        painting = true;
                        applyAt(e.getX(), e.getY(), false);
                    }
                }
                e.consume();
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.SECONDARY) panning = false;
            if (e.getButton() == MouseButton.PRIMARY && currentTool == Tool.ZONE && zoneStartRow >= 0)
                finishZonePlacement(e.getX(), e.getY());
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
                renderAll();
                e.consume();
                return;
            }
            if (e.isPrimaryButtonDown() && currentTool == Tool.ZONE && zoneStartRow >= 0) {
                int[] rc = mapRenderer.pickTile(e.getX(), e.getY());
                if (rc != null) {
                    zonePreviewRow = rc[0];
                    zonePreviewCol = rc[1];
                    renderAll();
                }
                e.consume();
                return;
            }
            if (painting && e.isPrimaryButtonDown()) {
                applyAt(e.getX(), e.getY(), true);
                e.consume();
            }
        });

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
            renderAll();
            e.consume();
        });

        canvas.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                String s = db.getString();
                if (s.startsWith("TOOL:") || s.startsWith("TOKEN:")) {
                    e.acceptTransferModes(TransferMode.COPY);
                }
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
                        renderAll();
                        ok = true;
                    }
                }
            }
            e.setDropCompleted(ok);
            e.consume();
        });
    }

    // ---TILE TOOLS---
    private void applyAt(double sx, double sy, boolean isDrag) {
        int[] rc = mapRenderer.pickTile(sx, sy);
        if (rc == null) return;

        int r = rc[0], c = rc[1];
        Tile tile = gameMap.getTile(r, c);

        switch (currentTool) {
            case DISABLED -> tile.add(Tile.DISABLED);
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
            case ERASE -> tile.clearAll();
            default -> {
            }
        }

        renderAll();
    }

    // ---LOCATION TOOLS---
    private void selectLocationAt(double sx, double sy) {
        int[] rc = mapRenderer.pickTile(sx, sy);
        selectedLocation = null;

        if (rc != null) {
            for (MapSaverLoader.LocationMarker loc : locations) {
                if (Math.abs(loc.row - rc[0]) < loc.tileRadius &&
                        Math.abs(loc.col - rc[1]) < loc.tileRadius) {
                    selectedLocation = loc;
                    break;
                }
            }
        }
        renderAll();
    }

    // ---MAP-LISTE---
    private void refreshMapList() {
        mapFileNames.clear();
        if (!MAPS_DIR.exists()) {
            MAPS_DIR.mkdirs();
            return;
        }
        File[] files = MAPS_DIR.listFiles(
                (dir, name) -> name.endsWith(".shorin"));
        if (files == null) return;

        for (File f : files) {
            mapFileNames.add(f.getName());
        }
    }

    private void loadSelectedFromList(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        File file = new File(MAPS_DIR, fileName);
        loadMapFromFile(file);
    }

    // ---SAVE / LOAD---
    private void saveMap() {
        String name = mapSavingName.getText().trim();
        if (name.isEmpty()) {
            logger.warning("Kein Dateiname angegeben");
            return;
        }
        if (!name.endsWith(".shorin")) name += ".shorin";

        if (!MAPS_DIR.exists()) MAPS_DIR.mkdirs();

        File out = new File(MAPS_DIR, name);
        try {
            new MapSaverLoader().saveWorldMap(out, gameMap, locations);
            logger.info("WorldMap gespeichert: " + out.getAbsolutePath());
            refreshMapList();
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadMapViaChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("WorldMap laden");
        fc.setInitialDirectory(MAPS_DIR.exists() ? MAPS_DIR : new File("."));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Shorin Maps", "*.shorin"));

        File file = fc.showOpenDialog(mapRenderer.getCanvas().getScene().getWindow());
        if (file == null) return;
        loadMapFromFile(file);
    }

    private void loadMapFromFile(File file) {
        try {
            MapSaverLoader.LoadResult result = new MapSaverLoader().load(file);

            gameMap.resizeGrid(result.map.getRows(), result.map.getCols());
            for (int r = 0; r < result.map.getRows(); r++) {
                for (int c = 0; c < result.map.getCols(); c++) {
                    gameMap.getTile(r, c).flags = result.map.getTile(r, c).flags;
                }
            }

            locations.clear();
            locations.addAll(result.locations);

            mapSavingName.setText(file.getName().replace(".shorin", ""));
            logger.info("WorldMap geladen: " + file.getName()
                    + " (" + locations.size() + " Locations)");

            renderAll();
        } catch (Exception ex) {
            logger.severe("Fehler beim Laden: " + ex.getMessage());
        }
    }

    // ---RENDER---
    private void renderAll() {
        mapRenderer.renderWorldmap();
        renderLocations();
        renderZones();
    }


    private void renderLocations() {
        if (locations.isEmpty()) return;

        var g = mapRenderer.getCanvas().getGraphicsContext2D();
        double zoom = mapRenderer.getZoom();
        double baseTile = BASE_TILE;

        for (MapSaverLoader.LocationMarker loc : locations) {
            double worldX = loc.col * baseTile;
            double worldY = loc.row * baseTile;
            double sx = (worldX - mapRenderer.getCamX()) * zoom;
            double sy = (worldY - mapRenderer.getCamY()) * zoom;
            double size = loc.tileRadius * baseTile * zoom;

            boolean selected = loc == selectedLocation;

            g.setGlobalAlpha(0.55);
            g.setFill(selected ? Color.rgb(255, 200, 50) : Color.rgb(60, 140, 200));
            g.fillRoundRect(sx, sy, size, size, 6, 6);

            g.setGlobalAlpha(1.0);
            g.setStroke(selected ? Color.YELLOW : Color.WHITE);
            g.setLineWidth(selected ? 2.5 : 1.5);
            g.strokeRoundRect(sx, sy, size, size, 6, 6);

            g.setFill(Color.WHITE);
            g.setFont(javafx.scene.text.Font.font(Math.max(10, size * 0.25)));
            g.fillText(loc.name, sx + 4, sy + size * 0.55);
        }

        g.setGlobalAlpha(1.0);
    }

    private void renderZones() {
        var g = mapRenderer.getCanvas().getGraphicsContext2D();
        double zoom = mapRenderer.getZoom(), camX = mapRenderer.getCamX(), camY = mapRenderer.getCamY();
        double step = BASE_TILE * zoom;

        for (LocationZone zone : zones) {
            boolean sel = zone == selectedZone;
            double sx = (zone.col * BASE_TILE - camX) * zoom;
            double sy = (zone.row * BASE_TILE - camY) * zoom;
            double sw = zone.width * step, sh = zone.height * step;

            g.setGlobalAlpha(sel ? 0.35 : 0.20);
            g.setFill(sel ? Color.YELLOW : Color.CORNFLOWERBLUE);
            g.fillRect(sx, sy, sw, sh);
            g.setGlobalAlpha(sel ? 0.90 : 0.60);
            g.setStroke(sel ? Color.YELLOW : Color.CORNFLOWERBLUE);
            g.setLineWidth(sel ? 2.5 : 1.5);
            g.strokeRect(sx, sy, sw, sh);
            g.setGlobalAlpha(1.0);
            g.setFill(Color.WHITE);
            g.setFont(javafx.scene.text.Font.font(11));
            g.fillText(zone.name, sx + 4, sy + 13);
            if (zone.hasTriggers()) {
                g.setFill(sel ? Color.YELLOW : Color.LIGHTBLUE);
                g.fillText("[" + zone.getTriggers().size() + "]", sx + 4, sy + 25);
            }
        }

        if (currentTool == Tool.ZONE && zoneStartRow >= 0) {
            int minR = Math.min(zoneStartRow, zonePreviewRow), minC = Math.min(zoneStartCol, zonePreviewCol);
            double px = (minC * BASE_TILE - camX) * zoom, py = (minR * BASE_TILE - camY) * zoom;
            double pw = (Math.abs(zonePreviewCol - zoneStartCol) + 1) * step;
            double ph = (Math.abs(zonePreviewRow - zoneStartRow) + 1) * step;
            g.setGlobalAlpha(0.25);
            g.setFill(Color.LIME);
            g.fillRect(px, py, pw, ph);
            g.setGlobalAlpha(0.8);
            g.setStroke(Color.LIME);
            g.setLineWidth(1.5);
            g.strokeRect(px, py, pw, ph);
        }
        g.setGlobalAlpha(1.0);
    }

    private void finishZonePlacement(double mx, double my) {
        int[] rc = mapRenderer.pickTile(mx, my);
        if (rc == null) {
            zoneStartRow = -1;
            return;
        }
        int minR = Math.min(zoneStartRow, rc[0]), minC = Math.min(zoneStartCol, rc[1]);
        int w = Math.abs(rc[1] - zoneStartCol) + 1, h = Math.abs(rc[0] - zoneStartRow) + 1;

        TextInputDialog dialog = new TextInputDialog("Neue Zone");
        dialog.setTitle("Zone benennen");
        dialog.setHeaderText(w + "×" + h + " Tiles");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                LocationZone zone = new LocationZone(minR, minC, w, h, name);
                zones.add(zone);
                selectedZone = zone;
                updateZonePanel();
                renderAll();
            }
        });
        zoneStartRow = zoneStartCol = zonePreviewRow = zonePreviewCol = -1;
    }

    private void selectZoneAt(double sx, double sy) {
        int[] rc = mapRenderer.pickTile(sx, sy);
        selectedZone = null;
        if (rc != null) {
            int bestArea = Integer.MAX_VALUE;
            for (LocationZone z : zones) {
                if (z.contains(rc[0], rc[1]) && z.width * z.height < bestArea) {
                    bestArea = z.width * z.height;
                    selectedZone = z;
                }
            }
        }
        updateZonePanel();
        renderAll();
    }

    private void deleteSelectedZone() {
        if (selectedZone == null) return;
        zones.remove(selectedZone);
        selectedZone = null;
        updateZonePanel();
        renderAll();
    }

    private void updateZonePanel() {
        if (zoneNameDisplay == null) return;
        zoneNameDisplay.setText(selectedZone == null ? "(keine Zone ausgewählt)"
                : selectedZone.name + " (" + selectedZone.width + "×" + selectedZone.height + ")");
        refreshTriggerList();
    }

    private void refreshTriggerList() {
        triggerListView.getItems().clear();
        if (selectedZone != null) triggerListView.getItems().addAll(selectedZone.getTriggers());
    }

    private void addTriggerToSelected() {
        if (selectedZone == null || triggerTypeCombo.getValue() == null) return;
        String p1 = triggerParam1Field.getText().trim();
        ZoneTrigger t = switch (triggerTypeCombo.getValue()) {
            case TELEPORT -> ZoneTrigger.teleport(p1,
                    parseIntOrDefault(triggerParam2Field.getText(), -1),
                    parseIntOrDefault(triggerParam3Field.getText(), -1));
            case TEXT -> ZoneTrigger.text(p1);
            case NPC -> ZoneTrigger.npc(p1);
        };
        selectedZone.addTrigger(t);
        refreshTriggerList();
        triggerParam1Field.clear();
        triggerParam2Field.clear();
        triggerParam3Field.clear();
    }

    private void removeSelectedTrigger() {
        if (selectedZone == null) return;
        ZoneTrigger sel = triggerListView.getSelectionModel().getSelectedItem();
        if (sel != null) {
            selectedZone.removeTrigger(sel);
            refreshTriggerList();
        }
    }

    private void updateTriggerParams() {
        if (triggerTypeCombo.getValue() == null) return;
        boolean isTeleport = triggerTypeCombo.getValue() == ZoneTrigger.TriggerType.TELEPORT;
        param1Label.setText(switch (triggerTypeCombo.getValue()) {
            case TELEPORT -> "Zielmap:";
            case TEXT -> "Text:";
            case NPC -> "NPC-ID:";
        });
        param2Label.setVisible(isTeleport);
        triggerParam2Field.setVisible(isTeleport);
        param3Label.setVisible(isTeleport);
        triggerParam3Field.setVisible(isTeleport);
        triggerParam1Field.clear();
        triggerParam2Field.clear();
        triggerParam3Field.clear();
    }

    private int parseIntOrDefault(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private VBox buildZoneSection() {
        zoneNameDisplay = new Label("(keine Zone ausgewählt)");
        zoneNameDisplay.setTextFill(Color.WHITE);

        Button deleteZoneBtn = sceneBuilder.makeButton("Zone löschen");
        deleteZoneBtn.setMaxWidth(Double.MAX_VALUE);
        deleteZoneBtn.setOnAction(e -> deleteSelectedZone());

        triggerListView = new ListView<>();
        triggerListView.setPrefHeight(110);
        triggerListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ZoneTrigger t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setText(null);
                    return;
                }
                setText(t.toString());
                setTextFill(switch (t.type) {
                    case TELEPORT -> Color.LIGHTGREEN;
                    case TEXT -> Color.LIGHTYELLOW;
                    case NPC -> Color.LIGHTCORAL;
                });
                setStyle("-fx-background-color: transparent;");
            }
        });

        Button removeTriggerBtn = sceneBuilder.makeButton("Trigger entfernen");
        removeTriggerBtn.setMaxWidth(Double.MAX_VALUE);
        removeTriggerBtn.setOnAction(e -> removeSelectedTrigger());

        triggerTypeCombo = new ComboBox<>();
        triggerTypeCombo.getItems().addAll(ZoneTrigger.TriggerType.values());
        triggerTypeCombo.setMaxWidth(Double.MAX_VALUE);
        triggerTypeCombo.setPromptText("Trigger-Typ");
        triggerTypeCombo.setOnAction(e -> updateTriggerParams());

        param1Label = makeSmallLabel("Parameter:");
        triggerParam1Field = new TextField();
        triggerParam1Field.setMaxWidth(Double.MAX_VALUE);
        param2Label = makeSmallLabel("Spawn-Zeile:");
        param2Label.setVisible(false);
        triggerParam2Field = new TextField();
        triggerParam2Field.setMaxWidth(Double.MAX_VALUE);
        triggerParam2Field.setVisible(false);
        param3Label = makeSmallLabel("Spawn-Spalte:");
        param3Label.setVisible(false);
        triggerParam3Field = new TextField();
        triggerParam3Field.setMaxWidth(Double.MAX_VALUE);
        triggerParam3Field.setVisible(false);

        Button addTriggerBtn = sceneBuilder.makeButton("+ Trigger");
        addTriggerBtn.setMaxWidth(Double.MAX_VALUE);
        addTriggerBtn.setOnAction(e -> addTriggerToSelected());

        return new VBox(4, zoneNameDisplay, deleteZoneBtn, new Separator(),
                makeSmallLabel("Trigger:"), triggerListView, removeTriggerBtn, new Separator(),
                makeSmallLabel("Hinzufügen:"), triggerTypeCombo,
                param1Label, triggerParam1Field,
                param2Label, triggerParam2Field,
                param3Label, triggerParam3Field,
                addTriggerBtn);
    }

    // ---UI / PALETTE---
    private Node buildPalette() {
        VBox box = new VBox(8);
        box.setPrefWidth(230);
        box.setStyle("-fx-padding: 10; -fx-background-color: rgba(20,20,28,0.95);");

        // Back
        Button backBtn = sceneBuilder.makeButton("Zurueck");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> Main.getStage().setScene(new MainScreen().getScene(0)));

        //Map-Liste
        Label listLabel = makeHeaderLabel("Overworld Maps");

        mapListView = new ListView<>(mapFileNames);
        mapListView.setPrefHeight(140);
        mapListView.setStyle("-fx-background-color: rgba(30,30,40,0.9);");
        mapListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                loadSelectedFromList(mapListView.getSelectionModel().getSelectedItem());
            }
        });

        Button refreshBtn = sceneBuilder.makeButton("Liste aktualisieren");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshMapList());

        //Save / Load
        Label saveLabel = makeHeaderLabel("Datei");

        mapSavingName.setPromptText("Dateiname (ohne .shorin)");
        mapSavingName.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = sceneBuilder.makeButton("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveMap());

        Button loadBtn = sceneBuilder.makeButton("Laden");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadMapViaChooser());

        Button loadBgBtn = sceneBuilder.makeButton("Hintergrundbild");
        loadBgBtn.setMaxWidth(Double.MAX_VALUE);
        loadBgBtn.setOnAction(e -> {
            mapRenderer.loadBackground();
            renderAll();
        });

        Button newMapBtn = sceneBuilder.makeButton("Neue Map");
        newMapBtn.setMaxWidth(Double.MAX_VALUE);
        newMapBtn.setOnAction(e -> {
            gameMap.resizeGrid(30, 30);
            gameMap.clearAll();
            locations.clear();
            selectedLocation = null;
            mapSavingName.setText("new_map");
            zones.clear();
            selectedZone = null;
            updateZonePanel();
            renderAll();
        });

        //Tool-Anzeige
        toolLabel = new Label("Tool: " + currentTool);
        toolLabel.setTextFill(Color.LIGHTGRAY);

        //Terrain Tools
        Label terrainLabel = makeHeaderLabel("Terrain");

        VBox terrainTools = new VBox(4,
                makeToolBtn("Disable", Tool.DISABLED),
                makeToolBtn("Wall", Tool.WALL),
                makeToolBtn("Door", Tool.DOOR),
                makeToolBtn("Difficult", Tool.DIFFICULT),
                makeToolBtn("Hazard", Tool.HAZARD),
                makeToolBtn("Erase", Tool.ERASE)
        );

        //Grid Controls
        Label gridLabel = makeHeaderLabel("Grid");

        TextField rowsField = new TextField(String.valueOf(gameMap.getRows()));
        rowsField.setPrefWidth(55);
        TextField colsField = new TextField(String.valueOf(gameMap.getCols()));
        colsField.setPrefWidth(55);

        Button applySize = sceneBuilder.makeButton("Apply");
        applySize.setOnAction(e -> {
            gameMap.resizeGrid(
                    parsePositiveInt(rowsField.getText(), gameMap.getRows()),
                    parsePositiveInt(colsField.getText(), gameMap.getCols())
            );
            renderAll();
        });

        HBox sizeRow = new HBox(4,
                makeSmallLabel("R:"), rowsField,
                makeSmallLabel("C:"), colsField,
                applySize
        );

        VBox gridSection = new VBox(4,
                sizeRow,
                makeGridBtn("+ Oben", () -> {
                    gameMap.addRowTop();
                    renderAll();
                }),
                makeGridBtn("+ Unten", () -> {
                    gameMap.addRowBottom();
                    renderAll();
                }),
                makeGridBtn("+ Links", () -> {
                    gameMap.addColLeft();
                    renderAll();
                }),
                makeGridBtn("+ Rechts", () -> {
                    gameMap.addColRight();
                    renderAll();
                }),
                new Separator(),
                makeGridBtn("- Oben", () -> {
                    gameMap.removeRowTop();
                    renderAll();
                }),
                makeGridBtn("- Unten", () -> {
                    gameMap.removeRowBottom();
                    renderAll();
                }),
                makeGridBtn("- Links", () -> {
                    gameMap.removeColLeft();
                    renderAll();
                }),
                makeGridBtn("- Rechts", () -> {
                    gameMap.removeColRight();
                    renderAll();
                })
        );

        //Zusammenbauen
        box.getChildren().addAll(
                backBtn,
                new Separator(),
                listLabel, mapListView, refreshBtn,
                new Separator(),
                saveLabel, mapSavingName, saveBtn, loadBtn, loadBgBtn, newMapBtn,
                new Separator(),
                toolLabel,
                terrainLabel, terrainTools,
                new Separator(),
                makeHeaderLabel("Zonen"),
                makeToolBtn("Zone zeichnen", Tool.ZONE),
                makeToolBtn("Zone auswählen", Tool.ZONE_SELECT),
                buildZoneSection(),
                new Separator(),
                gridLabel, gridSection
        );

        ScrollPane scroll = sceneBuilder.createScrollPane();
        scroll.setContent(box);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setPrefWidth(240);
        scroll.setStyle("-fx-background: rgba(20,20,28,0.95);");

        return scroll;
    }

    //UI Helpers

    private Label makeHeaderLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-font-weight: bold; -fx-padding: 4 0 2 0;");
        return l;
    }

    private Label makeSmallLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.GRAY);
        return l;
    }

    private Button makeToolBtn(String name, Tool tool) {
        Button btn = sceneBuilder.makeButton(name);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            currentTool = tool;
            toolLabel.setText("Tool: " + currentTool);
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

    private Button makeGridBtn(String label, Runnable action) {
        Button btn = sceneBuilder.makeButton(label);
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

    // ---Saveble---
    @Override
    public Scene getScene(int stage) {
        BorderPane root = new BorderPane();
        setupInputHandlers();

        root.setLeft(buildPalette());
        root.setCenter(mapRenderer.buildWorldMapPane(mapSavingName));

        refreshMapList();

        Scene scene = new Scene(root, 1400, 900);
        String css = CSSLoader.resolveUserOrBackupCSS();
        if (css != null) scene.getStylesheets().add(css);

        return scene;
    }

    @Override
    public void reset() {
    }

    // ---TOOL ENUM---
    private enum Tool {
        DISABLED, WALL, DOOR, DIFFICULT, HAZARD, ERASE,
        LOCATION, LOCATION_SELECT,
        ZONE, ZONE_SELECT
    }
}