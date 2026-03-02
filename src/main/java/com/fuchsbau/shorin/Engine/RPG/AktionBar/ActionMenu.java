package com.fuchsbau.shorin.Engine.RPG.AktionBar;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class ActionMenu {

    public enum Mode {TRAVEL, DIALOG, COMBAT}

    private final SceneBuilder sb = SceneBuilder.getSceneBuilder();
    private final VBox root;
    private Scene sceneRef; // wird von PlayerScreen gesetzt für KeyHandler

    private Mode currentMode = Mode.TRAVEL;

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
        bindKeys();
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        refresh();
    }

    private void refresh() {
        root.getChildren().clear();
        switch (currentMode) {
            case TRAVEL -> new TravelingActionBox().build(root, sceneRef);
            case DIALOG -> new DialogActionBox().build(root, sceneRef);
            case COMBAT -> new CombatActionBox().build(root, sceneRef);
        }
    }

    private void bindKeys() {
        if (sceneRef == null) return;
        sceneRef.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> { /* Options – bleibt in PlayerScreen */ }
                default -> forwardKey(e.getCode());
            }
        });
    }

    private void forwardKey(KeyCode code) {
        // TODO: aktive Row informieren
    }
}