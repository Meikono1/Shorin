package com.fuchsbau.shorin.Engine.Editor.Module.Actions;

public enum ActionCategory {
    CLASS, SKILL, GEAR, BASIC, EXPLORATION, DOWNTIME, ACTIVITY;

    public String displayName() {
        return switch (this) {
            case CLASS -> "Class";
            case SKILL -> "Skill";
            case GEAR -> "Gear";
            case BASIC -> "Basic";
            case EXPLORATION -> "Exploration";
            case DOWNTIME -> "Downtime";
            case ACTIVITY -> "Activity";
        };
    }
}