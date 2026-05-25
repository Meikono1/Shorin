package com.fuchsbau.shorin.Engine.RPG.UI;

/**
 * Einfache Actionable-Implementierung für einmalige Aktionen.
 * Ersetzt anonyme innere Klassen im Controller.
 */
public class SimpleAction implements Actionable {

    private final String label;
    private final ButtonStyle style;
    private final Runnable action;

    public SimpleAction(String label, ButtonStyle style, Runnable action) {
        this.label = label;
        this.style = style;
        this.action = action;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ButtonStyle getStyle() {
        return style;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void execute() {
        action.run();
    }
}