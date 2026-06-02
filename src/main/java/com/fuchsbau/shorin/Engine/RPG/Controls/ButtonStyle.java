package com.fuchsbau.shorin.Engine.RPG.Controls;

/**
 * Alle Button-Typen im Spiel.
 * CSS-Klassen kommen aus main.css — @TODO-Klassen noch nicht im CSS.
 */
public enum ButtonStyle {

    MENU(new String[]{"menu-button"}),
    ACTION(new String[]{"menu-button", "action-button"}),
    STAT(new String[]{"stat-button"}),
    ICON(new String[]{"info-icon"}),

    // --- @TODO: CSS noch ergänzen ---
    SPELL(new String[]{"menu-button", "spell-button"}),
    DIALOG(new String[]{"menu-button", "dialog-button"}),
    DANGER(new String[]{"menu-button", "btn-danger"}),
    COMBAT(new String[]{"menu-button", "combat-button"});

    public final String[] cssClasses;

    ButtonStyle(String[] cssClasses) {
        this.cssClasses = cssClasses;
    }
}