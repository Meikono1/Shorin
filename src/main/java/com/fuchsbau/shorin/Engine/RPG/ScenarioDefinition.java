package com.fuchsbau.shorin.Engine.RPG;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScenarioDefinition(
        String name,
        double x,
        double y,
        String icon,
        String sceneClass,
        int finishState,
        Set<String> races
) {

}
