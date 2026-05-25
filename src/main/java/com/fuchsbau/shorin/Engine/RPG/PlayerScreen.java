package com.fuchsbau.shorin.Engine.RPG;

import com.fuchsbau.shorin.Engine.Map.MapModel;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.AktionBar.ActionMenu;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.CenterPanelView;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.LeftPanelView;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.RightPanelView;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Game;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.util.logging.Logger;

/**
 * Controller zwischen Models und Views.
 * Kein Layout-Code — nur verdrahten, routen, koordinieren.
 *
 * Models: MapModel, Party (@TODO), EncounterState (@TODO)
 * Views:  LeftPanelView, CenterPanelView, RightPanelView
 */
public class PlayerScreen implements Saveble {

    private final Logger logger = FileLogger.getLogger();

    // Models
    private final MapModel mapModel;
    // @TODO private Party party;
    // @TODO private EncounterModel encounterModel;

    // Views
    private final LeftPanelView  leftView;
    private final CenterPanelView centerView;
    private final RightPanelView  rightView;

    private Scene scene;

    public PlayerScreen(MapModel mapModel) {
        this.mapModel  = mapModel;
        this.leftView  = new LeftPanelView();
        this.centerView = new CenterPanelView();
        this.rightView  = new RightPanelView(mapModel);

        wireViews();
        logger.info("PlayerScreen init");
    }

    // Views verdrahten — Callbacks registrieren
    private void wireViews() {
        // Minimap-Expand → Battlemap-Mode
        rightView.setOnMinimapExpand(this::switchToBattleMap);

        // Char-Wechsel im LeftPanel → Center + Right informieren
        leftView.setOnCharSelected(this::onCharChanged);

        logger.fine("Views verdrahtet");
    }

    // Scene aufbauen — nur einmal, dann cachen (Saveble-Pattern)
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

        // ActionMenu bekommt Scene-Ref für KeyHandler
        centerView.setScene(scene);

        // @TODO party laden und an leftView übergeben
        // leftView.setMembers(party.getMembers(), party.getActive());

        logger.info("PlayerScreen gebaut");
    }

    // Keyboard-Routing — nur hier, nicht in Views
    private void bindKeys() {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> Main.getStage().setScene(
                        Game.getInstance().optionen.getScene(1));
                case W -> onMove(0, -1);
                case S -> onMove(0,  1);
                case A -> onMove(-1, 0);
                case D -> onMove(1,  0);
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

    // ActionMenu-Mode wechseln — vom Place oder Event ausgelöst
    public void setMode(ActionMenu.Mode mode) {
        centerView.setMode(mode);
        logger.info("Mode → " + mode);
    }

    // Story-Text setzen — vom aktiven Place ausgelöst
    // @TODO durch Event-System ersetzen
    public void setStoryText(String text) {
        centerView.setStoryText(text);
        logger.fine("Story gesetzt | " + text.length() + " Zeichen");
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