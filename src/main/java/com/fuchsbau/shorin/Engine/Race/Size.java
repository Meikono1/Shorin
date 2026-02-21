package com.fuchsbau.shorin.Engine.Race;


public enum Size {
    TINY("Smaller than 60 cm"),
    SMALL("Between 60 and 120 cm"),
    MEDIUM("Between 120 and 240 cm"),
    LARGE("Between 240 and 500 cm"),
    HUGE("Between 500 and 1000 cm"),
    GARGANTUAN("Larger than 1000 cm");

    private String description;

    Size(String description) {
        this.description = description;
    }

    /**
     * Absolute size difference.
     */
    public int diff(Size other) {
        return Math.abs(this.ordinalSize() - other.ordinalSize());
    }

    public int ordinalSize() {
        return ordinal();
    }
}
