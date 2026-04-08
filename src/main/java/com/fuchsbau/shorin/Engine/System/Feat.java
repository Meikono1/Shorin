package com.fuchsbau.shorin.Engine.System;

import java.util.ArrayList;
import java.util.List;

public class Feat {

    public String name = "";
    public String description = "";
    public int level = 1;
    public List<String> traits = new ArrayList<>(); // aus TraitModule

    public List<Prerequisite> prerequisites = new ArrayList<>();
    public List<Effect> effects = new ArrayList<>();

    public String frequency = "";
    public String requirements = "";
    public List<String> leadsTo = new ArrayList<>();
    public String grantedAction = ""; // Name der GameAction


    // Erweiterbar über type — aktuell: FEAT, SKILL
    public static class Prerequisite {
        public PrerequisiteType type;
        public String ref = "";       // Name des Feats oder Skills
        public String level = "";     // z.B. "master", "expert" bei Skills

        public Prerequisite() {}

        public Prerequisite(PrerequisiteType type, String ref, String level) {
            this.type = type;
            this.ref = ref;
            this.level = level;
        }
    }

    public enum PrerequisiteType {
        FEAT,
        SKILL
        // später: CLASS, ANCESTRY, ABILITY_SCORE, ...
    }

    // Erweiterbar über type — aktuell: SKILL_BONUS
    public static class Effect {
        public EffectType type;
        public String ref = "";       // z.B. Skill-Name
        public String value = "";     // z.B. "+2", "trained"

        public Effect() {}

        public Effect(EffectType type, String ref, String value) {
            this.type = type;
            this.ref = ref;
            this.value = value;
        }
    }

    public enum EffectType {
        SKILL_BONUS
        // später: ABILITY_BONUS, ACTION_GRANT, RESISTANCE, ...
    }
}