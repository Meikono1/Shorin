package com.fuchsbau.shorin.Engine.RPG.ViewModules.ButtonControl;

import com.fuchsbau.shorin.Engine.RPG.AktionBar.ActionMenu;
import com.fuchsbau.shorin.Engine.RPG.AktionBar.ActionMode;
import com.fuchsbau.shorin.Engine.RPG.UI.Actionable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.ScreenMode;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Button-Leiste — zwei Modi:
 * ADVENTURE → ActionMenu mit Slot-Map
 * DIALOG    → List<Actionable> vom Controller
 */
public class TextControlModule implements Renderable, Hideable {

    private final Logger logger = FileLogger.getLogger();

    private VBox root;
    private VBox contentArea;
    private Text hintText;

    private ActionMenu actionMenu;
    private ScreenMode currentMode = ScreenMode.ADVENTURE;

    @Override
    public Node build() {
        hintText = new Text("");
        hintText.setFill(Color.LIGHTGRAY);
        hintText.setStyle("-fx-font-size: 12px;");

        contentArea = new VBox();
        contentArea.setFillWidth(true);

        root = new VBox(4, hintText, contentArea);
        root.setPadding(new Insets(8, 12, 8, 12));
        root.setStyle("-fx-background-color: rgba(12, 12, 20, 0.75);");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        setAdventureMode(Map.of());
        logger.fine("TextControlModule gebaut");
        return root;
    }

    public void setAdventureMode(Map<Integer, Actionable> actions) {
        currentMode = ScreenMode.ADVENTURE;
        if (root == null) return;

        contentArea.getChildren().clear();
        hintText.setText("");

        if (actionMenu == null) {
            actionMenu = new ActionMenu();
            actionMenu.setMode(ActionMode.TRAVEL);
        }

        actionMenu.setSlotActions(actions);
        contentArea.getChildren().add(actionMenu.getRoot());
        logger.fine("ADVENTURE | slots: " + actions.size());
    }

    public void setDialogMode(List<Actionable> actions, String hint) {
        currentMode = ScreenMode.DIALOG;
        if (root == null) return;

        contentArea.getChildren().clear();
        hintText.setText(hint != null ? hint : "");

        FlowPane buttonRow = new FlowPane();
        buttonRow.setHgap(8);
        buttonRow.setVgap(6);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        for (Actionable action : actions) {
            buttonRow.getChildren().add(action.buildButton());
        }

        contentArea.getChildren().add(buttonRow);
        logger.fine("DIALOG | " + actions.size() + " Buttons");
    }

    public void setScene(Scene scene) {
        if (actionMenu != null) actionMenu.setScene(scene);
    }

    public void setHint(String hint) {
        if (hintText != null) hintText.setText(hint != null ? hint : "");
    }

    public void clearHint() {
        if (hintText != null) hintText.setText("");
    }

    public ScreenMode getCurrentMode() {
        return currentMode;
    }

    @Override
    public void refresh() {
        if (currentMode == ScreenMode.ADVENTURE) setAdventureMode(Map.of());
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