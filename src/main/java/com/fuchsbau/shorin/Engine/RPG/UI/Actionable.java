package com.fuchsbau.shorin.Engine.RPG.UI;

import javafx.scene.control.Button;


public interface Actionable {

    String getLabel();

    ButtonStyle getStyle();

    // Ob der Button gerade ausführbar ist
    boolean canExecute();

    // Warum nicht ausführbar
    default String getDisabledReason() {
        return "";
    }

    void execute();

    // Baut den fertigen Button — ButtonFactory
    default Button buildButton() {
        if (!canExecute()) {
            return ButtonFactory.makeDisabled(getStyle(), getLabel(), getDisabledReason());
        }
        return ButtonFactory.makeFullWidth(getStyle(), getLabel(), this::execute);
    }
}