package com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces;

public interface Hideable {

    void show();

    void hide();

    boolean isVisible();

    // Toggle — spart if/else im Controller
    default void toggle() {
        if (isVisible()) hide();
        else show();
    }
}
