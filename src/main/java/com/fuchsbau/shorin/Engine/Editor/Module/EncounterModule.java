package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fuchsbau.shorin.Engine.Encounter.EncounterPane;
import com.fuchsbau.shorin.Engine.Encounter.EncounterState;
import com.fuchsbau.shorin.Engine.Encounter.EncounterTransition;
import com.fuchsbau.shorin.Engine.Encounter.Widget.InitiativeTrackerWidget;
import com.fuchsbau.shorin.Engine.Encounter.WidgetAnchor;
import com.fuchsbau.shorin.Engine.Map.Core.MapRenderer;
import com.fuchsbau.shorin.Engine.Map.Core.MapSaverLoader;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightingSystem;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.MutableGameMap;
import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Engine.System.NonPlayerCharacter;
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

    // NPC-Liste für den Battlemap-Editor
    private final ObservableList<NonPlayerCharacter> npcList = FXCollections.observableArrayList();

    // aktive Transition-Richtung (per Dropdown wählbar)
    private EncounterTransition.Direction activeDirection = EncounterTransition.Direction.FROM_RIGHT;

    // =========================================================

    @Override
    public String getTitle() {
        return "Encounter";
    }

    // Content
    @Override
    public Node buildContent() {
        if (encounterPane == null) {
            logger.info("EncounterPane erstmalig gebaut");

            encounterPane = new EncounterPane(mapRenderer);
            mapRenderer.setupCanvasHandlers();

            encounterPane.addWidget(new InitiativeTrackerWidget(), WidgetAnchor.RIGHT_CENTER);

            logger.info("Widgets registriert: InitiativeTracker + DebugOverlay");
        }

        Node root = encounterPane.getRoot();
        logger.fine("buildContent → Transition: " + activeDirection);
        return root;
    }

    //  SidePanel -
    @Override
    public Node buildSidePanel() {
        logger.fine("buildSidePanel gestartet");
        return new VBox(8,
                buildMapSection(),
                new Separator(),
                buildEncounterControlSection(),
                new Separator(),
                buildTransitionSection(),
                new Separator(),
                buildNpcEditorSection()
        );
    }

    //  Karten-Sektion --
    private Node buildMapSection() {
        Label listLabel = new Label("Karte laden");

        TextField searchField = new TextField();
        searchField.setPromptText("Suchen...");
        searchField.textProperty().addListener((obs, ov, nv) -> {
            if (nv == null || nv.isBlank()) {
                mapListView.setItems(mapFiles);
            } else {
                ObservableList<String> filtered = FXCollections.observableArrayList(
                        mapFiles.filtered(s -> s.toLowerCase().contains(nv.toLowerCase())));
                mapListView.setItems(filtered);
            }
            logger.fine("Kartenfilter: '" + nv + "' → " + mapListView.getItems().size() + " Treffer");
        });

        mapListView = new ListView<>(mapFiles);
        mapListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2) return;
            String selected = mapListView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            logger.info("Karte ausgewählt: " + selected);
            loadMapByName(selected);
        });
        VBox.setVgrow(mapListView, Priority.ALWAYS);

        Button refreshBtn = new Button("Aktualisieren");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> {
            logger.fine("Kartenliste wird manuell aktualisiert");
            refreshMapList();
        });

        VBox box = new VBox(4, listLabel, searchField, mapListView, refreshBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    //  Encounter-Steuerung
    private Node buildEncounterControlSection() {
        Label label = new Label("Encounter");

        // Combat-Toggle — steuert die Initiative-Leiste
        Button combatToggle = new Button("⚔ Combat: AUS");
        combatToggle.setMaxWidth(Double.MAX_VALUE);
        combatToggle.setOnAction(e -> {
            if (encounterPane == null) { logger.warning("combatToggle: kein EncounterPane"); return; }
            boolean next = !encounterPane.inCombatProperty().get();
            encounterPane.inCombatProperty().set(next);
            combatToggle.setText("⚔ Combat: " + (next ? "AN" : "AUS"));
            logger.info("inCombat → " + next);
        });

        Button startBtn = new Button("▶ Start");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> {
            if (encounterPane == null) { logger.warning("Start: kein EncounterPane"); return; }
            syncInitiativeFromMap();
            if (encounterPane.getState().initiative.isEmpty()) {
                logger.warning("Start: keine Tokens auf der Karte");
                return;
            }
            encounterPane.getState().round.set(1);
            encounterPane.getState().actionsUsed.set(0);
            encounterPane.getState().activeToken.set(encounterPane.getState().initiative.getFirst());
            logger.info("Encounter gestartet – " + encounterPane.getState().initiative.size() + " Tokens");
        });

        Button nextTurnBtn = new Button("⏭ Nächster Zug");
        nextTurnBtn.setMaxWidth(Double.MAX_VALUE);
        nextTurnBtn.setOnAction(e -> {
            if (encounterPane == null) { logger.warning("NextTurn: kein EncounterPane"); return; }
            encounterPane.getState().nextTurn();
            logger.fine("Nächster Zug – Runde " + encounterPane.getState().round.get());
        });

        Button resetBtn = new Button("↺ Reset");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> {
            if (encounterPane == null) { logger.warning("Reset: kein EncounterPane"); return; }
            encounterPane.getState().round.set(0);
            encounterPane.getState().actionsUsed.set(0);
            encounterPane.getState().activeToken.set(null);
            encounterPane.inCombatProperty().set(false);
            combatToggle.setText("⚔ Combat: AUS");
            logger.info("Encounter zurückgesetzt");
        });

        Button endBtn = new Button("■ Beenden");
        endBtn.setMaxWidth(Double.MAX_VALUE);
        endBtn.setOnAction(e -> {
            if (encounterPane == null) { logger.warning("End: kein EncounterPane"); return; }
            encounterPane.getState().round.set(0);
            encounterPane.inCombatProperty().set(false);
            combatToggle.setText("⚔ Combat: AUS");
            logger.info("Encounter beendet");
        });

        VBox box = new VBox(4, label, combatToggle, startBtn, nextTurnBtn, resetBtn, endBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    // Transition-Picker
    private Node buildTransitionSection() {
        Label label = new Label("Einblend-Animation");

        ComboBox<EncounterTransition.Direction> dirBox = new ComboBox<>();
        dirBox.getItems().setAll(EncounterTransition.Direction.values());
        dirBox.setValue(activeDirection);
        dirBox.setMaxWidth(Double.MAX_VALUE);
        dirBox.setOnAction(e -> {
            activeDirection = dirBox.getValue();
            logger.info("Transition geändert → " + activeDirection);
        });

        Button previewBtn = new Button("Vorschau");
        previewBtn.setMaxWidth(Double.MAX_VALUE);
        previewBtn.setOnAction(e -> {
            if (encounterPane == null) {
                logger.warning("Preview: kein EncounterPane");
                return;
            }
            logger.fine("Transition-Vorschau: " + activeDirection);
            EncounterTransition.playIn(encounterPane.getRoot(), activeDirection);
        });

        VBox box = new VBox(4, label, dirBox, previewBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    // NPC-Editor (Battlemap)
    private Node buildNpcEditorSection() {
        Label label = new Label("NPC – Battlemap");

        // NPC-Liste
        ListView<NonPlayerCharacter> npcListView = new ListView<>(npcList);
        npcListView.setPrefHeight(160);
        npcListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NonPlayerCharacter n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty || n == null ? null : "[" + n.level + "] " + n.name);
            }
        });
        npcListView.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null) logger.fine("NPC ausgewählt: " + nv.name + " (Level " + nv.level + ")");
        });

        // NPC auf Karte setzen (Drag wäre ideal – hier Button-Fallback)
        Button placeBtn = new Button("→ Auf Karte setzen");
        placeBtn.setMaxWidth(Double.MAX_VALUE);
        placeBtn.setOnAction(e -> {
            NonPlayerCharacter selected = npcListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                logger.warning("placeNpc: kein NPC ausgewählt");
                return;
            }
            if (encounterPane == null) {
                logger.warning("placeNpc: kein EncounterPane");
                return;
            }

            // Default: Mitte der Karte
            int row = gameMap.getRows() / 2;
            int col = gameMap.getCols() / 2;
            gameMap.getTokens().add(new Token(row, col, selected));
            mapRenderer.renderBattlemap();
            logger.info("NPC platziert: " + selected.name + " @ " + row + "/" + col);
        });

        Button removeBtn = new Button("✕ Entfernen");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setOnAction(e -> {
            NonPlayerCharacter selected = npcListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                logger.warning("removeNpc: kein NPC ausgewählt");
                return;
            }

            // Alle Tokens dieses NPCs von der Karte räumen
            int before = gameMap.getTokens().size();
            gameMap.getTokens().removeIf(t -> t.Statblock == selected);
            int removed = before - gameMap.getTokens().size();
            mapRenderer.renderBattlemap();
            logger.info("NPC entfernt: " + selected.name + " → " + removed + " Token(s) gelöscht");
        });

        Button refreshNpcBtn = new Button("Aktualisieren");
        refreshNpcBtn.setMaxWidth(Double.MAX_VALUE);
        refreshNpcBtn.setOnAction(e -> {
            logger.fine("NPC-Liste wird aktualisiert");
            refreshNpcList();
        });

        VBox box = new VBox(4, label, npcListView, placeBtn, removeBtn, refreshNpcBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    // Karten laden
    private void refreshMapList() {
        mapFiles.clear();
        if (!MAPS_DIR.exists()) {
            MAPS_DIR.mkdirs();
            logger.fine("MAPS_DIR angelegt: " + MAPS_DIR.getAbsolutePath());
            return;
        }
        File[] files = MAPS_DIR.listFiles(f -> f.getName().endsWith(".shorin"));
        if (files == null) return;
        for (File f : files) mapFiles.add(f.getName());
        logger.fine("Kartenliste aktualisiert: " + mapFiles.size() + " Dateien");
    }

    private void loadMapByName(String fileName) {
        File file = new File(MAPS_DIR, fileName);
        if (!file.exists()) {
            logger.warning("Datei nicht gefunden: " + fileName);
            return;
        }
        try {
            logger.info("Lade Karte: " + fileName);
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

            syncInitiativeFromMap();

            logger.info("Karte geladen: " + fileName
                    + " | Tiles " + result.map.getRows() + "×" + result.map.getCols()
                    + " | Tokens: " + result.tokens.size()
                    + " | Lichter: " + result.lights.size()
                    + " | Wände: " + result.walls.size());

        } catch (Exception ex) {
            logger.severe("Fehler beim Laden von " + fileName + ": " + ex.getMessage());
        }
    }

    private void syncInitiativeFromMap() {
        if (encounterPane == null) return;
        EncounterState state = encounterPane.getState();

        state.initiative.clear();
        state.activeToken.set(null);
        state.initiative.addAll(gameMap.getTokens());

        logger.info("Initiative befüllt: " + state.initiative.size() + " Token(s) von der Karte");
    }

    // NPC-Liste
    private void refreshNpcList() {
        npcList.clear();
        logger.fine("NPC-Liste geleert (Repository noch nicht angebunden)");
    }

    @Override
    public void onActivate() {
        refreshMapList();
        refreshNpcList();
        logger.info("EncounterModule aktiviert | MAPS_DIR: " + MAPS_DIR.getAbsolutePath());
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    private Button debugBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("""
                -fx-background-color: #2a2a3a;
                -fx-text-fill: #cccccc;
                -fx-font-size: 11px;
                -fx-padding: 3 6 3 6;
                -fx-background-radius: 4;
                """);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}