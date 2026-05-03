package com.fuchsbau.shorin.Engine.Race;

import com.fuchsbau.shorin.Engine.RPG.Language;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScore;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScoreEntry;

import java.util.ArrayList;
import java.util.List;

public class Ancestrie {

    // --- CombatStats ---
    public String name = "";
    public int health = 8;
    public Size size = Size.MEDIUM;
    public int speedFt = 25;

    // --- AbilityBoosts: feste Boosts die die Ancestry gibt (+1 oder -1) ---
    public List<AbilityScoreEntry> abilityBoosts = new ArrayList<>();
    public int freeBoosts = 0;

    public List<Language> languages = new ArrayList<>();
    public List<String> traits = new ArrayList<>();
}