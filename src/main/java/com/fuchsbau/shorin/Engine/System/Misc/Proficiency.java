package com.fuchsbau.shorin.Engine.System.Misc;

public enum Proficiency {
    UNTRAINED, TRAINED, EXPERT, MASTER, LEGENDARY;

    public String shortLabel() {
        return switch (this) {
            case UNTRAINED  -> "U";
            case TRAINED    -> "T";
            case EXPERT     -> "E";
            case MASTER     -> "M";
            case LEGENDARY  -> "L";
        };
    }
}