package com.fuchsbau.shorin.Engine.RPG;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Map.MapModel;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.UI.Actionable;
import com.fuchsbau.shorin.Engine.RPG.UI.ButtonStyle;
import com.fuchsbau.shorin.Engine.RPG.UI.SimpleAction;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.CenterPanelView;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.LeftPanelView;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.RightPanelView;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.ScreenMode;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay.TextDisplayConfig;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay.TextSegment;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Game;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller zwischen Models und Views.
 * Kein Layout-Code — nur verdrahten, routen, koordinieren.
 * <p>
 * Models: MapModel, Party (@TODO), EncounterState (@TODO)
 * Views:  LeftPanelView, CenterPanelView, RightPanelView
 */
public class PlayerScreen implements Saveble {

    private final Logger logger = FileLogger.getLogger();

    // Models
    private final MapModel mapModel;
    private ScreenMode currentMode = ScreenMode.ADVENTURE;
    // @TODO private Party party;
    // @TODO private EncounterModel encounterModel;

    // Views
    private final LeftPanelView leftView;
    private final CenterPanelView centerView;
    private final RightPanelView rightView;

    private Scene scene;

    public PlayerScreen(MapModel mapModel) {
        this.mapModel = mapModel;
        this.leftView = new LeftPanelView();
        this.centerView = new CenterPanelView();
        this.rightView = new RightPanelView(mapModel);

        wireViews();
        logger.info("PlayerScreen init");
    }

    // wireViews() — Testszene direkt nach build laden
    private void wireViews() {
        rightView.setOnMinimapExpand(this::switchToBattleMap);
        leftView.setOnCharSelected(this::onCharChanged);
        logger.fine("Views verdrahtet");
    }

    // build() — Testszene nach dem Aufbau starten
    private void build() {
        BorderPane root = new BorderPane();
        root.setBackground(GameOptions.hintergrund);

        root.setLeft(leftView.build());
        root.setCenter(centerView.build());
        root.setRight(rightView.build());

        scene = new Scene(root, GameOptions.width, GameOptions.height);
        bindKeys();

        String css = CSSLoader.resolveUserOrBackupCSS();
        if (css != null) scene.getStylesheets().add(css);

        // Testszene laden sobald Scene aufgebaut ist
        Platform.runLater(this::runTestScene);

        logger.info("PlayerScreen gebaut");
    }


    private void runTestScene() {
        logger.info("Testszene start");
        currentMode = ScreenMode.ADVENTURE;

        centerView.applyConfig(new TextDisplayConfig.Builder()
                .sceneImage(ImagePaths.SHORIN_PAPER_MAP)
                .imageMode(TextDisplayConfig.ImageMode.BACKGROUND)
                .build());

        centerView.clearDisplay();
        centerView.setAdventureMode(Map.of(
                2, new SimpleAction("Sprechen", ButtonStyle.ACTION, this::startDialog)
        ));

        centerView.showSegment(
                TextSegment.narration("Du stehst am Hafendeck. Ein Wachmann lehnt gelangweilt gegen eine Kiste.").build(),
                () -> logger.fine("Intro fertig")
        );
    }

    private void startDialog() {
        logger.info("Dialog-Mode");
        currentMode = ScreenMode.DIALOG;
        centerView.clearDisplay();

        centerView.showSegment(
                TextSegment.npc("Wachmann", "Halt! Wer bist du und was willst du hier am Hafen?")
                        .image(ImagePaths.MAP_TOWER).build(),
                () -> centerView.setDialogMode(buildDialogActions(), "Was antwortest du?")
        );
    }

    private List<Actionable> buildDialogActions() {
        return List.of(
                new SimpleAction("Ich bin Händler.",  ButtonStyle.DIALOG, () -> dialogResponse("Ich bin Händler.")),
                new SimpleAction("Ich suche Arbeit.", ButtonStyle.DIALOG, () -> dialogResponse("Ich suche Arbeit.")),
                new SimpleAction("[Einschüchtern]",   ButtonStyle.ACTION,  () -> dialogResponse("Einschüchtern"))
        );
    }

    private void dialogResponse(String choice) {
        logger.info("Wahl → " + choice);
        centerView.setDialogMode(List.of(), "");

        centerView.showSegment(TextSegment.player("Du", choice).build(), () -> {
            String antwort = switch (choice) {
                case "Ich bin Händler."  -> "Händler? Dann pass auf deine Ware auf.";
                case "Ich suche Arbeit." -> "Frag beim Hafenmeister. Der sucht immer Leute.";
                default                  -> "Der Wachmann tritt einen Schritt zurück.";
            };
            centerView.showSegment(
                    TextSegment.npc("Wachmann", antwort).image(ImagePaths.MAP_TOWER).build(),
                    () -> centerView.setDialogMode(
                            List.of(new SimpleAction("Auf Wiedersehen.", ButtonStyle.DIALOG, this::endDialog)),
                            ""
                    )
            );
        });
    }

    private void endDialog() {
        logger.info("Dialog beendet → Adventure-Mode");
        currentMode = ScreenMode.ADVENTURE;

        centerView.showSegment(TextSegment.player("Du", "Auf Wiedersehen.").build(), () -> {
            centerView.clearDisplay();
            centerView.showSegment(
                    TextSegment.narration("Der Wachmann nickt. Du bist wieder allein am Hafendeck.").build(),
                    () -> centerView.setAdventureMode(Map.of(
                            2, new SimpleAction("Sprechen", ButtonStyle.ACTION, this::startDialog)
                    ))
            );
        });
    }

    // Keyboard-Routing — nur hier, nicht in Views
    private void bindKeys() {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> Main.getStage().setScene(
                        Game.getInstance().optionen.getScene(1));
                case W -> onMove(0, -1);
                case S -> onMove(0, 1);
                case A -> onMove(-1, 0);
                case D -> onMove(1, 0);
                case TAB -> onTabNextChar();
            }
        });
    }

    // Screen-Swap: TextAdventure → Battlemap
    private void switchToBattleMap() {
        logger.info("Switch → BattleMap");
        leftView.hide();
        rightView.hide();
        centerView.switchToBattleMap(mapModel, this::switchToTextAdventure);
        // @TODO EncounterModule starten
    }

    // Screen-Swap: Battlemap → TextAdventure
    private void switchToTextAdventure() {
        logger.info("Switch → TextAdventure");
        leftView.show();
        rightView.show();
        centerView.switchToTextAdventure();
        // @TODO EncounterModule pausieren
    }

    // WASD-Bewegung — @TODO Party-Formation auf Grid bewegen
    private void onMove(int dx, int dy) {
        logger.fine("Move dx=" + dx + " dy=" + dy);
        // @TODO mapModel.moveParty(dx, dy)
    }

    // TAB — nächsten Char in Party wählen
    private void onTabNextChar() {
        logger.fine("Tab → nächster Char");
        // @TODO party.selectNext(); leftView.setMembers(...)
    }

    // Char gewechselt — Views neu binden
    private void onCharChanged() {
        logger.info("Char gewechselt → Views aktualisieren");
        centerView.refresh();
        // @TODO rightView.refresh() wenn Char-spezifische Daten im RightPanel
    }

    // Saveble
    @Override
    public Scene getScene(int stage) {
        if (scene == null) build();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        scene = null;
        leftView.refresh();
        centerView.refresh();
        rightView.refresh();
        logger.info("PlayerScreen reset");
    }
}