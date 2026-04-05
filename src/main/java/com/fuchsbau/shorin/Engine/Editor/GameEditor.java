package com.fuchsbau.shorin.Engine.Editor;

import com.fuchsbau.shorin.Engine.Editor.Module.*;
import com.fuchsbau.shorin.Engine.Editor.Module.Actions.ActionModule;
import com.fuchsbau.shorin.Engine.Editor.Module.BattleMap.BattleMapModule;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.MainScreen;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GameEditor {

    private final Logger logger = FileLogger.getLogger();

    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("File");
    private final Menu editMenu = new Menu("Edit");
    private final Menu viewMenu = new Menu("View");
    private final Menu helpMenu = new Menu("Help");

    private final List<Menu> activeModuleMenus = new ArrayList<>();

    private final ToolBar groupBar = new ToolBar();
    private final TabPane editorTabs = new TabPane();
    private final BorderPane root = new BorderPane();

    // Alle Gruppen mit ihren Modulen
    private final List<ModuleGroup> groups = new ArrayList<>();

    // Bereiche die beim Tab-Wechsel neu befüllt werden
    private final ToolBar moduleToolbar = new ToolBar();
    private final ScrollPane moduleSidePane = new ScrollPane();

    // ────────────────────────────────────────────────────────────
    public Scene buildScene() {
        // --- Feste Menüleiste ---
        buildFileMenu();
        buildEditMenu();
        buildViewMenu();
        buildHelpMenu();
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

        // --- Gruppen + Module definieren ---
        // Neue Gruppe → hier eintragen, Rest passt sich automatisch an
        groups.addAll(List.of(
                new ModuleGroup("Welt", List.of(
                        new WorldMapModule(),
                        new BattleMapModule(),
                        new ShopModule(),
                        new ScenarioModule(),
                        new CharacterModule()
                )),
                new ModuleGroup("System", List.of(
                        new ClassModule(),
                        new FeatModule(),
                        new ActionModule(),
                        new SpellModule(),
                        new ArchetypeModule(),
                        new AncestryModule(),
                        new BackgroundModule(),
                        new ConditionModule(),
                        new TraitModule()
                )),
                new ModuleGroup("Items", List.of(
                        new ItemModule(),
                        new CraftingModule()
                )),
                new ModuleGroup("Inhalte", List.of(
                        new NpcModule(),
                        new DialogModule(),
                        new QuestModule(),
                        new EventModule(),
                        new SceneModule()
                ))
        ));

        // --- Gruppen-Leiste bauen ---
        for (ModuleGroup group : groups) {
            Button groupBtn = new Button(group.name());
            groupBtn.getStyleClass().add("editor-group-btn");
            groupBtn.setOnAction(e -> activateGroup(group));
            groupBar.getItems().add(groupBtn);
        }

        // --- Tab-Leiste vorbereiten ---
        editorTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- Side-Panel vorbereiten ---
        moduleSidePane.setFitToWidth(true);
        moduleSidePane.setPannable(true);
        moduleSidePane.setPrefWidth(220);

        // --- Layout zusammenbauen ---
        VBox topArea = new VBox(menuBar, groupBar, editorTabs, moduleToolbar);
        root.setTop(topArea);
        root.setLeft(moduleSidePane);

        // Erste Gruppe direkt aktivieren
        if (!groups.isEmpty()) activateGroup(groups.getFirst());

        Scene scene = new Scene(root, 1400, 900);
        return scene;
    }

    private void activateGroup(ModuleGroup group) {
        editorTabs.getTabs().clear();

        for (EditorModule module : group.modules()) {
            Tab tab = new Tab(module.getTitle());
            // Kein Content im Tab — Center wird von activateModule gesetzt
            tab.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) activateModule(module);
                else module.onDeactivate();
            });
            editorTabs.getTabs().add(tab);
        }

        // Erstes Modul der Gruppe direkt aktivieren
        if (!group.modules().isEmpty()) activateModule(group.modules().getFirst());

        logger.info("Gruppe aktiviert: " + group.name());
    }

    private void activateModule(EditorModule module) {
        // Menüs tauschen
        menuBar.getMenus().removeAll(activeModuleMenus);
        activeModuleMenus.clear();
        activeModuleMenus.addAll(module.getMenus());
        menuBar.getMenus().addAll(activeModuleMenus);

        // Toolbar neu befüllen
        moduleToolbar.getItems().clear();
        Node toolbar = module.buildToolbar();
        if (toolbar != null) moduleToolbar.getItems().add(toolbar);

        // SidePanel + Center setzen
        moduleSidePane.setContent(module.buildSidePanel());
        moduleSidePane.setFitToWidth(true);
        moduleSidePane.setFitToHeight(true);

        root.setCenter(module.buildContent());
        module.onActivate();

        logger.info("Modul aktiviert: " + module.getTitle());
    }

    // --- File (immer sichtbar) ---
    private void buildFileMenu() {
        MenuItem back = new MenuItem("Hauptmenü");
        back.setOnAction(e -> Main.getStage().setScene(new MainScreen().getScene(0)));

        // Später: Neu, Speichern, Laden, Export
        fileMenu.getItems().addAll(back, new SeparatorMenuItem());
    }

    // --- Edit (immer sichtbar) ---
    private void buildEditMenu() {
        // Später: Undo, Redo, Einstellungen
    }

    // --- View (immer sichtbar) ---
    private void buildViewMenu() {
        // Später: Debug-Toggle, Zoom reset, Grid an/aus
    }

    // --- Help (immer sichtbar) ---
    private void buildHelpMenu() {
        // Später: Shortcuts, About
    }

    private record ModuleGroup(String name, List<EditorModule> modules) {
    }
}
