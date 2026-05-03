package com.fuchsbau.shorin.Engine.System.Character;

public enum Skill {
    ACROBATICS(AbilityScore.DEX),
    ARCANA(AbilityScore.INT),
    ATHLETICS(AbilityScore.STR),
    CRAFTING(AbilityScore.INT),
    DECEPTION(AbilityScore.CHA),
    DIPLOMACY(AbilityScore.CHA),
    INTIMIDATION(AbilityScore.CHA),
    LORE(AbilityScore.INT),
    MEDICINE(AbilityScore.WIS),
    NATURE(AbilityScore.WIS),
    OCCULTISM(AbilityScore.INT),
    PERFORMANCE(AbilityScore.CHA),
    PERCEPTION(AbilityScore.WIS),
    RELIGION(AbilityScore.WIS),
    SOCIETY(AbilityScore.INT),
    STEALTH(AbilityScore.DEX),
    SURVIVAL(AbilityScore.WIS),
    THIEVERY(AbilityScore.DEX);

    private final AbilityScore stat;

    Skill(AbilityScore stat) {
        this.stat = stat;
    }

    public AbilityScore getStat() {
        return stat;
    }

    public String displayName() {
        String name = this.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}