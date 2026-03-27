package com.fuchsbau.shorin.Engine.Editor.Module.Actions;

public enum ActionCategory {
    CLASS, SKILL, GEAR, BASIC, EXPLORATION, DOWNTIME, ACTIVITY,
    ACTION_1, ACTION_2, ACTION_3;

    public String displayName() {
        return switch (this) {
            case CLASS -> "Class";
            case SKILL -> "Skill";
            case GEAR -> "Gear";
            case BASIC -> "Basic";
            case EXPLORATION -> "Exploration";
            case DOWNTIME -> "Downtime";
            case ACTIVITY -> "Activity";
            case ACTION_1 -> "1 Action";
            case ACTION_2 -> "2 Actions";
            case ACTION_3 -> "3 Actions";
        };
    }
}