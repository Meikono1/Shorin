package com.fuchsbau.shorin.Engine.RPG.Controls;

import com.fuchsbau.shorin.Engine.Logger.FileLogger;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import java.util.logging.Logger;

/**
 * Einzige Stelle wo Buttons gebaut werden.
 * Kein inline-CSS, kein getStyleClass().add() verstreut in Views.
 */
public class ButtonFactory {

    private static final Logger logger = FileLogger.getLogger();

    // Basis
    public static Button make(ButtonStyle style, String label) {
        Button btn = new Button(label);
        btn.getStyleClass().addAll(style.cssClasses);
        logger.finest("Button: '" + label + "' | " + style.name());
        return btn;
    }

    // Mit Action
    public static Button make(ButtonStyle style, String label, Runnable action) {
        Button btn = make(style, label);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // Mit Tooltip
    public static Button make(ButtonStyle style, String label, String tooltip, Runnable action) {
        Button btn = make(style, label, action);
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }

    // Volle Breite
    public static Button makeFullWidth(ButtonStyle style, String label, Runnable action) {
        Button btn = make(style, label, action);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    // Deaktiviert
    public static Button makeDisabled(ButtonStyle style, String label, String reason) {
        Button btn = make(style, label);
        btn.setDisable(true);
        Tooltip.install(btn, new Tooltip(reason));
        logger.fine("Button disabled: '" + label + "' | Grund: " + reason);
        return btn;
    }

    private ButtonFactory() {
    }
}