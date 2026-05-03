package com.fuchsbau.shorin.Engine.System.Misc;

public enum Proficiency {
    UNTRAINED(0), TRAINED(2), EXPERT(4), MASTER(6), LEGENDARY(8);

    public final int value;

    Proficiency(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getProficiencyValue(int level) {
        if (this.equals(UNTRAINED)) {
            return value;
        } else return value + level;
    }

    public String shortLabel() {
        return switch (this) {
            case UNTRAINED -> "U";
            case TRAINED -> "T";
            case EXPERT -> "E";
            case MASTER -> "M";
            case LEGENDARY -> "L";
        };
    }
}