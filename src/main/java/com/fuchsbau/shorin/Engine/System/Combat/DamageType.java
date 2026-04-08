package com.fuchsbau.shorin.Engine.System.Combat;

public enum DamageType {
    // Sammeltypen
    PHYSICAL(null),
    ENERGY(null),
    SPIRIT(null),

    // Physical
    BLUDGEONING(PHYSICAL),
    PIERCING(PHYSICAL),
    SLASHING(PHYSICAL),

    // Energy
    ACID(ENERGY),
    COLD(ENERGY),
    ELECTRICITY(ENERGY),
    FIRE(ENERGY),
    SONIC(ENERGY),
    VITALITY(ENERGY),
    VOID(ENERGY),
    FORCE(ENERGY),

    // Special
    MENTAL(null),
    POISON(null),
    BLEED(null),
    PRECISION(null);

    private final DamageType category;

    DamageType(DamageType category) {
        this.category = category;
    }

    // Gibt den Obertyp zurück — BLUDGEONING → PHYSICAL, FIRE → ENERGY, etc.
    public DamageType getCategory() {
        if (this.category == null) {
            return this;
        }
        return category;
    }

    // Prüft ob dieser Typ zu einer Kategorie gehört
    public boolean isOf(DamageType cat) {
        return this == cat || this.category == cat;
    }

    public String displayName() {
        return switch (this) {
            case PHYSICAL -> "Physical";
            case ENERGY -> "Energy";
            case BLUDGEONING -> "Bludgeoning";
            case PIERCING -> "Piercing";
            case SLASHING -> "Slashing";
            case BLEED -> "Bleed";
            case ACID -> "Acid";
            case COLD -> "Cold";
            case ELECTRICITY -> "Electricity";
            case FIRE -> "Fire";
            case SONIC -> "Sonic";
            case VITALITY -> "Vitality";
            case VOID -> "Void";
            case FORCE -> "Force";
            case SPIRIT -> "Spirit";
            case MENTAL -> "Mental";
            case POISON -> "Poison";
            case PRECISION -> "Precision";
        };
    }
}