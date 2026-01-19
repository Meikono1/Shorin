package com.fuchsbau.shorin.Engine.RPG;

public record ScenarioDefinition(
        String name,
        double x,
        double y,
        String icon,
        String sceneClass,
        int finishState
) {
}
