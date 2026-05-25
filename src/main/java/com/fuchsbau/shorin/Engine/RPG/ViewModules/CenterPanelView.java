package com.fuchsbau.shorin.Engine.RPG.ViewModules;

import com.fuchsbau.shorin.Engine.Map.MapModel;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.AktionBar.ActionMenu;
import com.fuchsbau.shorin.Engine.RPG.UI.ButtonFactory;
import com.fuchsbau.shorin.Engine.RPG.UI.ButtonStyle;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.logging.Logger;

/**
 * Mittleres Panel — Story-Text + ActionMenu (TextAdventure-Mode)
 * oder BattleMap-Canvas (BattleMap-Mode).
 * <p>
 * Mode-Wechsel kommt vom Controller — nie selbst initiieren.
 */
public class CenterPanelView implements Renderable, Hideable {

    private final SceneBuilder sb = SceneBuilder.getSceneBuilder();
    private final Logger logger = FileLogger.getLogger();

    private StackPane root;
    private VBox textLayer;
    private Node battleLayer;

    private TextFlow storyFlow;
    private ActionMenu actionMenu;

    @Override
    public Node build() {
        storyFlow = sb.mainFlow();
        storyFlow.setPadding(new Insets(16));
        storyFlow.setBackground(Background.EMPTY);

        // @TODO Story-Text aus aktivem Place/Event
        storyFlow.getChildren().add(sb.makeText("[ Story lädt... ]"));

        ScrollPane storyScroll = new ScrollPane(storyFlow);
        storyScroll.setFitToWidth(true);
        storyScroll.setBackground(Background.EMPTY);
        storyScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        storyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(storyScroll, Priority.ALWAYS);

        actionMenu = new ActionMenu();
        actionMenu.setMode(ActionMenu.Mode.TRAVEL);

        textLayer = new VBox(storyScroll, actionMenu.getRoot());
        textLayer.setBackground(GameOptions.hintergrund);
        textLayer.setPadding(new Insets(0, 8, 8, 8));
        VBox.setVgrow(storyScroll, Priority.ALWAYS);

        root = new StackPane(textLayer);
        StackPane.setAlignment(textLayer, Pos.TOP_LEFT);

        logger.fine("CenterPanelView gebaut");
        return root;
    }

    @Override
    public void refresh() {
        // @TODO Story-Text aus aktivem Place neu laden
        logger.fine("CenterPanelView refresh");
    }

    // Scene-Ref für ActionMenu KeyHandler
    public void setScene(Scene scene) {
        if (actionMenu != null) actionMenu.setScene(scene);
    }

    // Mode-Wechsel kommt vom Controller
    public void setMode(ActionMenu.Mode mode) {
        if (actionMenu != null) actionMenu.setMode(mode);
        logger.fine("ActionMenu Mode → " + mode);
    }

    // Story-Text setzen — vom aktiven Place/Event
    public void setStoryText(String text) {
        if (storyFlow == null) return;
        storyFlow.getChildren().clear();
        storyFlow.getChildren().add(new Text(text));
        logger.fine("StoryText gesetzt | " + text.length() + " Zeichen");
    }

    // Screen-Swap: TextAdventure → BattleMap
    // onBack-Callback kommt vom Controller (switchToTextAdventure)
    public void switchToBattleMap(MapModel mapModel, Runnable onBack) {
        if (root == null) return;

        // BattleMap-Layer aufbauen
        // @TODO EncounterPane aus EncounterModule holen statt direkt bauen
        Node mapNode = mapModel.getRenderer().buildBattleMapPane(null);

        // Zurück-Button
        javafx.scene.control.Button backBtn = ButtonFactory.make(
                ButtonStyle.MENU, "◀ Zurück", onBack);
        backBtn.setStyle(backBtn.getStyle() + "-fx-font-size: 11px;");

        BorderPane battleRoot = new BorderPane(mapNode);
        battleRoot.setTop(new HBox(backBtn));
        ((HBox) battleRoot.getTop()).setPadding(new Insets(4));

        battleLayer = battleRoot;
        root.getChildren().add(battleLayer);
        textLayer.setVisible(false);
        textLayer.setManaged(false);

        mapModel.getRenderer().renderBattlemap();
        logger.info("CenterPanelView → BattleMap");
    }

    // Screen-Swap: BattleMap → TextAdventure
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

    public TextFlow getStoryFlow() {
        return storyFlow;
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