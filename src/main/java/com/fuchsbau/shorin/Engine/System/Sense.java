package com.fuchsbau.shorin.Engine.System;

public enum Sense {
    LOW_LIGHT_VISION,
    DARKVISION,
    GREATER_DARKVISION,
    TREMORSENSE,
    SCENT,
    BLINDSIGHT,
    TRUESIGHT;

    public String displayName() {
        return switch (this) {
            case LOW_LIGHT_VISION -> "Low-Light Vision";
            case DARKVISION -> "Darkvision";
            case GREATER_DARKVISION -> "Greater Darkvision";
            case TREMORSENSE -> "Tremorsense";
            case SCENT -> "Scent";
            case BLINDSIGHT -> "Blindsight";
            case TRUESIGHT -> "Truesight";
        };
    }

    // Präzision
    public String precision() {
        return switch (this) {
            case SCENT, TREMORSENSE -> "imprecise";
            case BLINDSIGHT -> "precise";
            default -> "";
        };
    }
}