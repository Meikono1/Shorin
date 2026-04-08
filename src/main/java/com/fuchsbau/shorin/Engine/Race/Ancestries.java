package com.fuchsbau.shorin.Engine.Race;

import com.fuchsbau.shorin.Engine.RPG.Language;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScores;

import java.util.ArrayList;
import java.util.List;

public class Ancestries {

    // --- CombatStats ---
    public String name = "";
    public int health = 8;
    public Size size = Size.MEDIUM;
    public int speedFt = 25;

    // --- AbilityBoosts: feste Boosts die die Ancestry gibt (+1 oder -1) ---
    public List<AbilityBoost> abilityBoosts = new ArrayList<>();
    public int freeBoosts = 0;

    // --- Sonstiges ---
    public List<Language> languages = new ArrayList<>();
    public List<String> traits = new ArrayList<>(); // Trait-Namen, aus TraitModule


    // Innere Klasse für einen Ability Boost/Flaw
    public static class AbilityBoost {
        public AbilityScores score;
        public int value; // +1 oder -1

        public AbilityBoost() {
        }

        public AbilityBoost(AbilityScores score, int value) {
            this.score = score;
            this.value = value;
        }
    }
}