package com.fuchsbau.shorin.Engine.System;

import com.fuchsbau.shorin.Engine.System.Combat.ActionCost;
import com.fuchsbau.shorin.Engine.System.Combat.DamageType;
import com.fuchsbau.shorin.Engine.System.Misc.RecallKnowledge;

import java.util.ArrayList;
import java.util.List;

public class NonPlayerCharacter extends StatBlock {

    // --- Loot ---
    public List<String> lootIds = new ArrayList<>();

    // -- Recall Knowledge --
    public List<RecallKnowledge> recallKnowledge = new ArrayList<>();

    public NonPlayerCharacter() {
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