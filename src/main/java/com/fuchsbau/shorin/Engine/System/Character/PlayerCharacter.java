package com.fuchsbau.shorin.Engine.System.Character;

import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.System.Combat.SavingThrows;
import com.fuchsbau.shorin.Engine.System.Misc.Proficiency;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerCharacter {

    // --- Header ---
    public String name = "";
    public int level = 1;
    public String className = "";
    public int classHP = -1;
    public String ancestry = "";
    public int ancestryHP = -20;
    public String background = "";

    // Ability score Boosts
    public List<AbilityScoreEntry> ancestryBoostChoices = new ArrayList<>();
    public List<AbilityScoreEntry> backgroundBoostChoice = new ArrayList<>();
    public AbilityScoreEntry classBoostChoice;
    public List<AbilityScoreEntry> freeBoostChoices = new ArrayList<>();

    // --- Stats ---
    public int str = 0, dex = 0, con = 0;
    public int intel = 0, wis = 0, cha = 0;

    // --- Combat ---
    public int ac = 10;
    public int hp = 0;
    public int speed = 25;

    // --- Saves ---
    public int fortitude = 0;
    public Proficiency fortProficency = Proficiency.UNTRAINED;
    public int reflex = 0;
    public Proficiency reflexProficency = Proficiency.UNTRAINED;
    public int will = 0;
    public Proficiency willProficency = Proficiency.UNTRAINED;

    // --- Skills ---
    public Map<Skill, Proficiency> skills = new EnumMap<>(Skill.class);

    // --- Ausrüstung ---
    public List<String> weapons = new ArrayList<>();
    public List<String> armor = new ArrayList<>();
    public List<String> gear = new ArrayList<>();
    public List<String> spells = new ArrayList<>();

    // --- Pet/Companion ---
    public String petName = "";
    public String petType = "";

    public PlayerCharacter() {
        for (Skill s : Skill.values()) {
            skills.put(s, Proficiency.UNTRAINED);
        }
    }

    public void setClass(ClassBuild sel) {
        className = sel.name;
        classHP = sel.hpPerLevel;
        classBoostChoice = sel.keyAbilities.getFirst();
        this.fortProficency = sel.savingThrows.getOrDefault(SavingThrows.FORTITUDE, Proficiency.UNTRAINED);
        this.willProficency = sel.savingThrows.getOrDefault(SavingThrows.WILL, Proficiency.UNTRAINED);
        this.reflexProficency = sel.savingThrows.getOrDefault(SavingThrows.REFLEX, Proficiency.UNTRAINED);

        applyAbilityscores();
    }

    public void setAncestrie(Ancestrie sel) {
        ancestry = sel.name;
        ancestryHP = sel.health;
        ancestryBoostChoices = sel.abilityBoosts;

        applyAbilityscores();
    }

    private void applyAbilityscores() {
        resetScores();

        for (AbilityScoreEntry score : ancestryBoostChoices) {
            applyScore(score);
        }
        for (AbilityScoreEntry score : backgroundBoostChoice) {
            applyScore(score);
        }
        for (AbilityScoreEntry score : freeBoostChoices) {
            applyScore(score);
        }
        if (classBoostChoice != null) {
            applyScore(classBoostChoice);
        }

        calculateStats();
    }

    private void calculateStats() {
        this.hp = ancestryHP
                + classHP * level
                + con * level;

        this.ac = 10
                + dex; //@TODO Armor einbauen

        this.fortitude = con + fortProficency.getProficiencyValue(level);
        this.reflex = dex + reflexProficency.getProficiencyValue(level);
        this.will = wis + willProficency.getProficiencyValue(level);
    }

    public int getSkillValue(Skill stat) {
        return skills.get(stat).getProficiencyValue(level);
    }

    public void refresh() {
        applyAbilityscores();
    }

    private void resetScores() {
        str = 0;
        dex = 0;
        con = 0;
        intel = 0;
        wis = 0;
        cha = 0;
    }

    private void applyScore(AbilityScoreEntry score) {
        switch (score.abilityScore) {
            case CHA -> cha += score.value;
            case CON -> con += score.value;
            case DEX -> dex += score.value;
            case STR -> str += score.value;
            case INT -> intel += score.value;
            case WIS -> wis += score.value;
        }
    }
}