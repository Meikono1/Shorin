package com.fuchsbau.shorin.Engine.System;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClassBuild {
    public String name = "";
    public String description = "";
    public int hpPerLevel = 1;
    public List<AbilityScores> keyAbilities = new ArrayList<>(AbilityScores.STR.ordinal());
    public Map<SavingThrows, Expertise> savingThrows = genDefaultSaves();
    public Map<ArmorCategory, Expertise> armorProficiencies = new EnumMap<>(ArmorCategory.class);
    public Map<WeaponCategory, Expertise> weaponProficiencies = new EnumMap<>(WeaponCategory.class);

    public ClassBuild() {
    }

    public ClassBuild(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    private EnumMap<SavingThrows, Expertise> genDefaultSaves() {
        EnumMap<SavingThrows, Expertise> map = new EnumMap<>(SavingThrows.class);
        for (SavingThrows savingThrows : SavingThrows.values()) {
            map.put(savingThrows, Expertise.T);
        }
        return map;
    }

    @Override
    public String toString() {
        return name;
    }
}