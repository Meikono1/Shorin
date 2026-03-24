package com.fuchsbau.shorin.Engine.System;

public enum AbilityScores {
    STR, DEX, CON, INT, WIS, CHA;

    public String fullName() {
        return switch (this) {
            case STR -> "Strength";
            case DEX -> "Dexterity";
            case CON -> "Constitution";
            case INT -> "Intelligence";
            case WIS -> "Wisdom";
            case CHA -> "Charisma";
        };
    }

    @Override
    public String toString() {
        return name() + " (" + fullName() + ")";
    }
}