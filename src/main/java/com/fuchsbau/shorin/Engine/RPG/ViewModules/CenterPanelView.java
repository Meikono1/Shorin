package com.fuchsbau.shorin.Engine.RPG.ViewModules;

import com.fuchsbau.shorin.Engine.Map.MapModel;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.UI.Actionable;
import com.fuchsbau.shorin.Engine.RPG.UI.ButtonFactory;
import com.fuchsbau.shorin.Engine.RPG.UI.ButtonStyle;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.ButtonControl.TextControlModule;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay.TextAdventureDisplayView;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay.TextDisplayConfig;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay.TextSegment;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Mittleres Panel.
 * TextAdventure-Mode: TextAdventureDisplayView + TextControlModule
 * BattleMap-Mode: MapRenderer-Canvas + Zurück-Button
 * <p>
 * Kein Spielzustand — alles kommt vom PlayerScreen (Controller).
 */
public class CenterPanelView implements Renderable, Hideable {

    private final Logger logger = FileLogger.getLogger();

    // Sub-Views
    private final TextAdventureDisplayView displayView = new TextAdventureDisplayView();
    private final TextControlModule controlModule = new TextControlModule();

    private VBox root;
    private BorderPane textLayer;
    private Node battleLayer;

    @Override
    public Node build() {
        Node display = displayView.build();
        Node control = controlModule.build();

        textLayer = new BorderPane();
        textLayer.setCenter(display);
        textLayer.setBottom(control);
        textLayer.setBackground(GameOptions.hintergrund);

        root = new VBox(textLayer);
        VBox.setVgrow(textLayer, Priority.ALWAYS);

        logger.fine("CenterPanelView gebaut");
        return root;
    }

    @Override
    public void refresh() {
        displayView.refresh();
        controlModule.refresh();
        logger.fine("CenterPanelView refresh");
    }

    // Segment anzeigen — kommt vom Controller/Place
    public void showSegment(TextSegment segment, Runnable onDone) {
        displayView.show(segment, onDone);
    }

    // Config für neue Szene setzen
    public void applyConfig(TextDisplayConfig config) {
        displayView.applyConfig(config);
    }

    // Display leeren — neue Szene
    public void clearDisplay() {
        displayView.clearDisplay();
        controlModule.clearHint();
    }

    public void setHint(String hint) {
        controlModule.setHint(hint);
    }

    // Screen-Swap → BattleMap
    public void switchToBattleMap(MapModel mapModel, Runnable onBack) {
        if (root == null) return;

        // @TODO EncounterModule statt direkt buildBattleMapPane
        Node mapNode = mapModel.getRenderer().buildBattleMapPane(null);

        Button backBtn = ButtonFactory.make(ButtonStyle.MENU, "◀ Zurück", onBack);

        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(4));

        BorderPane battleRoot = new BorderPane(mapNode);
        battleRoot.setTop(topBar);

        battleLayer = battleRoot;
        root.getChildren().add(battleLayer);

        textLayer.setVisible(false);
        textLayer.setManaged(false);

        mapModel.getRenderer().renderBattlemap();
        logger.info("CenterPanelView → BattleMap");
    }

    // Screen-Swap → TextAdventure
    public void switchToTextAdventure() {
        if (root == null) return;
        if (battleLayer != null) {
            root.getChildren().remove(battleLayer);
            battleLayer = null;
        }
        textLayer.setVisible(true);
        textLayer.setManaged(true);
        logger.info("CenterPanelView → TextAdventure");
    }

    public void setScene(Scene scene) {
        controlModule.setScene(scene);
    }

    // Adventure-Mode
    public void setAdventureMode(Map<Integer, Actionable> actions) {
        controlModule.setAdventureMode(actions);
    }

    public void setAdventureMode() {
        controlModule.setAdventureMode(Map.of());
    }

    // Dialog-Mode
    public void setDialogMode(List<Actionable> actions, String hint) {
        controlModule.setDialogMode(actions, hint);
    }

    @Override
    public void show() {
        root.setVisible(true);
        root.setManaged(true);
    }

    @Override
    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    @Override
    public boolean isVisible() {
        return root != null && root.isVisible();
    }
}