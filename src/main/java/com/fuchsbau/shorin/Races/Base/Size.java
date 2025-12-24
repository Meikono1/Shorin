package com.fuchsbau.shorin.Races.Base;

public enum Size {
    TINY("Smaller than 60 cm"),
    SMALL("Between 60 and 120 cm"),
    Medium("Between 120 and 240 cm"),
    Large("Between 240 and 500 cm"),
    HUGE("Between 500 and 1000 cm"),
    GARGANTUAN("Larger than 1000 cm");

    private String description;

    private Size(String description) {
        this.description = description;
    }
}
