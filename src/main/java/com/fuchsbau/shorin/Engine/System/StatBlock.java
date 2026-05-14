package com.fuchsbau.shorin.Engine.System;

import com.fuchsbau.shorin.Engine.System.Character.SenseEntry;
import com.fuchsbau.shorin.Engine.System.Character.Skill;
import com.fuchsbau.shorin.Engine.System.Combat.DamageModifier;
import com.fuchsbau.shorin.Engine.System.Combat.DamageType;
import com.fuchsbau.shorin.Engine.System.Misc.Proficiency;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StatBlock {
    // --- Basis ---
    public String name = "";
    public int level = 0;
    public String size = "Medium";
    public String tokenPath = "";
    public List<String> traits = new ArrayList<>();

    // --- Stats (direkte Modifier) ---
    public int str = 0, dex = 0, con = 0;
    public int intel = 0, wis = 0, cha = 0;

    // --- Combat ---
    public int ac = 10;
    public int hp = 0;
    public int speed = 25;
    public List<DamageType> immunities = new ArrayList<>();
    public List<DamageModifier> resistances = new ArrayList<>();
    public List<DamageModifier> weaknesses = new ArrayList<>();

    // --- Saves ---
    public int fortitude = 0;
    public Proficiency fortProficency = Proficiency.UNTRAINED;
    public int reflex = 0;
    public Proficiency reflexProficency = Proficiency.UNTRAINED;
    public int will = 0;
    public Proficiency willProficency = Proficiency.UNTRAINED;

    // --- Perception ---
    public int perception = 0;
    public List<SenseEntry> senses = new ArrayList<>();

    // --- Skills ---
    public Map<Skill, Integer> skillIncreases = new EnumMap<>(Skill.class);
    public Map<Skill, Proficiency> skills = new EnumMap<>(Skill.class);

    // --- Angriffe ---
    public List<NonPlayerCharacter.NpcAttack> attacks = new ArrayList<>();

    // --- Aktionen ---
    public List<String> actionIds = new ArrayList<>();

    // --- Ausrüstung ---
    public List<String> weapons = new ArrayList<>();
    public List<String> armor = new ArrayList<>();
    public List<String> gear = new ArrayList<>();
    public List<String> spells = new ArrayList<>();
}
