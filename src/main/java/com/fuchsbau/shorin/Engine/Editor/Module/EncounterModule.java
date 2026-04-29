package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fuchsbau.shorin.Engine.Encounter.EncounterPane;
import com.fuchsbau.shorin.Engine.Encounter.Widget.InitiativeTrackerWidget;
import com.fuchsbau.shorin.Engine.Encounter.WidgetAnchor;
import com.fuchsbau.shorin.Engine.Map.Core.MapRenderer;
import com.fuchsbau.shorin.Engine.Map.Core.MapSaverLoader;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightingSystem;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.MutableGameMap;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class EncounterModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final File MAPS_DIR =
            PathResolver.resolveWritable("maps/battle").toFile();

    // Map-Kern
    private final MutableGameMap gameMap = new MutableGameMap();
    private final LightingSystem lighting = new LightingSystem();
    private final MapRenderer mapRenderer = new MapRenderer(gameMap, lighting);

    // Encounter
    private EncounterPane encounterPane;

    // Karten-Sidebar
    private final ObservableList<String> mapFiles = FXCollections.observableArrayList();
    private ListView<String> mapListView;

    @Override
    public String getTitle() {
        return "Encounter";
    }

    @Override
    public Node buildContent() {
        // EncounterPane aufbauen
        if (encounterPane == null) {
            encounterPane = new EncounterPane(mapRenderer);
            mapRenderer.setupCanvasHandlers();

            encounterPane.addWidget(new InitiativeTrackerWidget(), WidgetAnchor.RIGHT_CENTER);
            logger.info("EncounterPane erstmalig gebaut");
        }
        return encounterPane.getRoot();
    }

    @Override
    public Node buildSidePanel() {
        // --- Kartenliste ---
        Label listLabel = new Label("Karte laden");

        TextField searchField = new TextField();
        searchField.setPromptText("Suchen...");
        searchField.textProperty().addListener((obs, ov, nv) -> {
            // Live-Filter auf mapFiles
            if (nv == null || nv.isBlank()) {
                mapListView.setItems(mapFiles);
            } else {
                ObservableList<String> filtered = FXCollections.observableArrayList(
                        mapFiles.filtered(s -> s.toLowerCase().contains(nv.toLowerCase())));
                mapListView.setItems(filtered);
            }
        });

        mapListView = new ListView<>(mapFiles);
        mapListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2) return;
            String selected = mapListView.getSelectionModel().getSelectedItem();
            if (selected != null) loadMapByName(selected);
        });
        VBox.setVgrow(mapListView, Priority.ALWAYS);

        Button refreshBtn = new Button("Aktualisieren");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshMapList());

        // --- Encounter-Steuerung ---
        Label encounterLabel = new Label("Encounter");

        Button startBtn = new Button("▶ Start");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> {
            if (encounterPane == null) return;
            encounterPane.getState().round.set(1);
            logger.info("Encounter gestartet");
        });

        Button nextTurnBtn = new Button("⏭ Nächster Zug");
        nextTurnBtn.setMaxWidth(Double.MAX_VALUE);
        nextTurnBtn.setOnAction(e -> {
            if (encounterPane == null) return;
            encounterPane.getState().nextTurn();
            logger.fine("Nächster Zug");
        });

        Button endBtn = new Button("Encounter beenden");
        endBtn.setMaxWidth(Double.MAX_VALUE);
        endBtn.setOnAction(e -> {
            if (encounterPane == null) return;
            encounterPane.getState().round.set(0);
            logger.info("Encounter beendet");
        });

        VBox panel = new VBox(6,
                listLabel, searchField, mapListView, refreshBtn,
                new Separator(),
                encounterLabel, startBtn, nextTurnBtn, endBtn
        );
        panel.setPadding(new Insets(8));
        VBox.setVgrow(mapListView, Priority.ALWAYS);

        refreshMapList();
        return panel;
    }

    // --- Karten laden ---
    private void refreshMapList() {
        mapFiles.clear();
        if (!MAPS_DIR.exists()) {
            MAPS_DIR.mkdirs();
            return;
        }
        File[] files = MAPS_DIR.listFiles(f -> f.getName().endsWith(".shorin"));
        if (files == null) return;
        for (File f : files) mapFiles.add(f.getName());
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
            gameMap.getTokens().addAll(result.tokens);
            gameMap.getLights().clear();
            gameMap.getLights().addAll(result.lights);
            gameMap.backgroundPath = result.map.backgroundPath;
            gameMap.clearWalls();
            gameMap.getWalls().addAll(result.walls);
            mapRenderer.loadBackground(gameMap.backgroundPath);
            lighting.recomputeLightmapAll(gameMap);
            mapRenderer.renderBattlemap();
            logger.info("Encounter-Karte geladen: " + fileName);
        } catch (Exception ex) {
            logger.severe("Fehler beim Laden: " + ex.getMessage());
        }
    }

    // --- Lifecycle ---
    @Override
    public void onActivate() {
        refreshMapList();
        logger.info("EncounterModule aktiviert");
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }
}