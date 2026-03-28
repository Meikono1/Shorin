package com.fuchsbau.shorin.Engine.System;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NpcBuild {

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
    public int reflex = 0;
    public int will = 0;

    // --- Perception ---
    public int perception = 0;
    public List<SenseEntry> senses = new ArrayList<>();

    // --- Skills ---
    public Map<Skills, Integer> skills = new EnumMap<>(Skills.class);

    // --- Angriffe ---
    public List<NpcAttack> attacks = new ArrayList<>();

    // --- Aktionen ---
    public List<String> actionIds = new ArrayList<>(); // Referenz auf GameAction IDs

    // --- Loot ---
    public List<String> lootIds = new ArrayList<>();

    // -- Recall KNowledge --
    public List<RecallKnowledge> recallKnowledge = new ArrayList<>();

    // ────────────────────────────────────────────────────────────

    public NpcBuild() {
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    @Override
    public String toString() {
        return name;
    }

    // --- Angriff ---
    public static class NpcAttack {
        public String name = "";
        public ActionCost cost = ActionCost.ONE;
        public int bonus = 0;
        public String damage = "";
        public DamageType damageType = DamageType.PIERCING;
        public List<String> traits = new ArrayList<>();

        public NpcAttack() {
        }
    }
}