package com.fuchsbau.shorin.Engine.System.Character;

import com.fuchsbau.shorin.Engine.System.Combat.ArmorCategory;
import com.fuchsbau.shorin.Engine.System.Combat.SavingThrows;
import com.fuchsbau.shorin.Engine.System.Misc.Proficiency;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClassBuild {
    public String name = "";
    public String description = "";
    public int hpPerLevel = 1;
    public List<AbilityScoreEntry> keyAbilities = new ArrayList<>();
    public Map<SavingThrows, Proficiency> savingThrows = genDefaultSaves();
    public Map<ArmorCategory, Proficiency> armorProficiencies = new EnumMap<>(ArmorCategory.class);
    public Map<WeaponCategory, Proficiency> weaponProficiencies = new EnumMap<>(WeaponCategory.class);

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

    private EnumMap<SavingThrows, Proficiency> genDefaultSaves() {
        EnumMap<SavingThrows, Proficiency> map = new EnumMap<>(SavingThrows.class);
        for (SavingThrows savingThrows : SavingThrows.values()) {
            map.put(savingThrows, Proficiency.TRAINED);
        }
        return map;
    }

    @Override
    public String toString() {
        return name;
    }
}