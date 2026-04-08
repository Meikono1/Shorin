package com.fuchsbau.shorin.Engine.System.Character;

import com.fuchsbau.shorin.Engine.System.Misc.Proficiency;
import com.fuchsbau.shorin.Engine.System.SlotEntry;

import java.util.ArrayList;
import java.util.List;

public class PlayerCharacter {

    // --- Header ---
    public String name = "";
    public int level = 1;
    public String className = "";
    public String ancestry = "";
    public String background = "";

    // --- Stats ---
    public int str = 10, dex = 10, con = 10;
    public int intel = 10, wis = 10, cha = 10;

    // --- Combat ---
    public int ac = 10;
    public int hp = 0;
    public int speed = 25;

    // --- Saves ---
    public int fortitude = 0;
    public int reflex = 0;
    public int will = 0;

    // --- Skills ---
    public List<SkillEntry> skills = new ArrayList<>();

    // --- Ausrüstung ---
    public List<String> weapons = new ArrayList<>();
    public List<String> armor = new ArrayList<>();
    public List<String> gear = new ArrayList<>();
    public List<String> spells = new ArrayList<>();

    // --- Pet/Companion ---
    public String petName = "";
    public String petType = "";

    // --- Level-Progression ---
    // Key = Level (0-20), Value = Liste gewählter Feats/Features
    public List<LevelEntry> levelEntries = new ArrayList<>();


    public static class SkillEntry {
        public String name = "";
        public Proficiency rank = Proficiency.UNTRAINED;
        public int bonus = 0;

        public SkillEntry() {
        }

        public SkillEntry(String name) {
            this.name = name;
        }
    }

    public static class LevelEntry {
        public int level = 0;
        public List<SlotEntry> slots = new ArrayList<>();

        public LevelEntry() {
        }

        public LevelEntry(int level) {
            this.level = level;
        }
    }
}