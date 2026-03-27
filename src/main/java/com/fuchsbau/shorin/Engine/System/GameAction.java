package com.fuchsbau.shorin.Engine.System;

import com.fuchsbau.shorin.Engine.Editor.Module.Actions.ActionCategory;

import java.util.ArrayList;
import java.util.List;

public class GameAction {
    public String name;
    public String description = "";
    public ActionCategory category = ActionCategory.BASIC;
    public ActionCost cost = ActionCost.ONE;
    public String trigger = "";
    public String requirements = "";
    public List<String> traits = new ArrayList<>();

    // Degree of Success
    public String criticalSuccess = "";
    public String success = "";
    public String failure = "";
    public String criticalFailure = "";

    // Effekt
    public EffectType effectType = EffectType.NONE;

    // DC
    public boolean hasDC = false;
    public DCType dcType = null;
    public int fixedDC = 0;
    public AbilityScores dcStat = null;

    public boolean secretRoll = false;

    public GameAction() {}

    public GameAction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}