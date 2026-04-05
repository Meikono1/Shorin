package com.fuchsbau.shorin.Engine.Map.Core.Tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LocationZone {

    // --- Geometrie ---

    /** Zeile der oberen linken Ecke (inklusive). */
    public final int row;

    /** Spalte der oberen linken Ecke (inklusive). */
    public final int col;

    /** Breite in Tiles (>= 1). */
    public final int width;

    /** Höhe in Tiles (>= 1). */
    public final int height;

    // --- Meta ---

    /** Anzeigename der Zone (z.B. "Schmiede", "Stadttor Whitebridge"). */
    public final String name;

    // --- Trigger ---

    private final List<ZoneTrigger> triggers;

    // --- Konstruktor ---

    public LocationZone(int row, int col, int width, int height, String name) {
        if (width  < 1) throw new IllegalArgumentException("width muss >= 1 sein");
        if (height < 1) throw new IllegalArgumentException("height muss >= 1 sein");

        this.row     = row;
        this.col     = col;
        this.width   = width;
        this.height  = height;
        this.name    = (name != null && !name.isEmpty()) ? name : "Zone";
        this.triggers = new ArrayList<>();
    }

    // --- Trigger-Verwaltung ---

    public void addTrigger(ZoneTrigger trigger) {
        if (trigger != null) triggers.add(trigger);
    }

    public void removeTrigger(ZoneTrigger trigger) {
        triggers.remove(trigger);
    }

    public List<ZoneTrigger> getTriggers() {
        return Collections.unmodifiableList(triggers);
    }

    public boolean hasTriggers() {
        return !triggers.isEmpty();
    }

    // --- Geometrie-Hilfsmethoden ---

    /**
     * Prüft ob ein Tile (tileRow, tileCol) innerhalb dieser Zone liegt.
     */
    public boolean contains(int tileRow, int tileCol) {
        return tileRow >= row && tileRow < row + height
                && tileCol >= col && tileCol < col + width;
    }

    /**
     * Prüft ob diese Zone sich mit einer anderen überlappt.
     */
    public boolean overlaps(LocationZone other) {
        return col < other.col + other.width
                && col + width > other.col
                && row < other.row + other.height
                && row + height > other.row;
    }

    // --- Trigger-Abfragen nach Typ ---

    /**
     * Gibt alle Trigger zurück, die beim Betreten automatisch feuern.
     */
    public List<ZoneTrigger> getOnEnterTriggers() {
        return triggers.stream()
                .filter(t -> t.activation == ZoneTrigger.ActivationMode.ON_ENTER)
                .toList();
    }

    /**
     * Gibt alle Trigger zurück, die per [E] aktiviert werden.
     */
    public List<ZoneTrigger> getOnInteractTriggers() {
        return triggers.stream()
                .filter(t -> t.activation == ZoneTrigger.ActivationMode.ON_INTERACT)
                .toList();
    }

    @Override
    public String toString() {
        return name + " [" + row + "," + col + " " + width + "x" + height + "] "
                + triggers.size() + " Trigger";
    }
}