package com.fuchsbau.shorin.Engine.System.Character;

import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.System.Combat.SavingThrows;
import com.fuchsbau.shorin.Engine.System.Misc.Proficiency;
import com.fuchsbau.shorin.Engine.System.StatBlock;

import java.util.ArrayList;
import java.util.List;

public class PlayerCharacter extends StatBlock {

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

    public void setLevel(String t1) {
        try {
            this.level = Integer.parseInt(t1);
            if (level < 0) level = 0;
            if (level > 20) level = 20;
            refresh();
        } catch (Exception ignored) {
        }
    }
}