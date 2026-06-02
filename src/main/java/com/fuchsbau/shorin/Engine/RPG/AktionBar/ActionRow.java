package com.fuchsbau.shorin.Engine.RPG.AktionBar;

import com.fuchsbau.shorin.Engine.RPG.Controls.Actionable;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.util.Map;

public interface ActionRow {
    void build(VBox container, Scene scene, Map<Integer, Actionable> actions);
}