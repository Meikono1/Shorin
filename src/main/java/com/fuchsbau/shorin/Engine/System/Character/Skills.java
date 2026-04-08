package com.fuchsbau.shorin.Engine.System.Character;

public enum Skills {
    ACROBATICS(AbilityScores.DEX),
    ARCANA(AbilityScores.INT),
    ATHLETICS(AbilityScores.STR),
    CRAFTING(AbilityScores.INT),
    DECEPTION(AbilityScores.CHA),
    DIPLOMACY(AbilityScores.CHA),
    INTIMIDATION(AbilityScores.CHA),
    LORE(AbilityScores.INT),
    MEDICINE(AbilityScores.WIS),
    NATURE(AbilityScores.WIS),
    OCCULTISM(AbilityScores.INT),
    PERFORMANCE(AbilityScores.CHA),
    PERCEPTION(AbilityScores.WIS),
    RELIGION(AbilityScores.WIS),
    SOCIETY(AbilityScores.INT),
    STEALTH(AbilityScores.DEX),
    SURVIVAL(AbilityScores.WIS),
    THIEVERY(AbilityScores.DEX);

    private final AbilityScores stat;

    Skills(AbilityScores stat) {
        this.stat = stat;
    }

    public AbilityScores getStat() {
        return stat;
    }

    public String displayName() {
        String name = this.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}