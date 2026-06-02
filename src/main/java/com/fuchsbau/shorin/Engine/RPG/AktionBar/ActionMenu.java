package com.fuchsbau.shorin.Engine.RPG.AktionBar;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.Controls.Actionable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Hält den aktiven ActionRow und rendert ihn bei Mode-Wechsel neu.
 * Slot-Belegung kommt als Map<Integer, Actionable> vom Controller.
 */
public class ActionMenu {

    private final VBox root;
    private Scene sceneRef;
    private ActionMode currentMode = ActionMode.TRAVEL;
    private Map<Integer, Actionable> slotActions = Map.of();

    public ActionMenu() {
        root = new VBox(4);
        root.setPadding(new Insets(8));
        root.setBackground(GameOptions.rowHintergrundTrans40);
        root.setMinHeight(140);
    }

    public VBox getRoot() {
        return root;
    }

    public void setScene(Scene scene) {
        this.sceneRef = scene;
    }

    public void setMode(ActionMode mode) {
        this.currentMode = mode;
        refresh();
    }

    public void setSlotActions(Map<Integer, Actionable> actions) {
        this.slotActions = actions;
        refresh();
    }

    private void refresh() {
        root.getChildren().clear();
        ActionRow row = switch (currentMode) {
            case TRAVEL -> new TravelingActionBox();
            case DIALOG -> new DialogActionBox();
            case COMBAT -> new CombatActionBox();
        };
        row.build(root, sceneRef, slotActions);
    }
}