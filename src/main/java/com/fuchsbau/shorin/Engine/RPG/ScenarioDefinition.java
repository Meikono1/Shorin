package com.fuchsbau.shorin.Engine.RPG;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fuchsbau.shorin.Races.Base.Race;
import com.fuchsbau.shorin.Races.RaceLoader;

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

    public List<Race> parsedRaces() {
        if (races == null || races.isEmpty()) return new LinkedList<>();

        return RaceLoader.getCachedRaces(races)
                .stream().sorted(Comparator.comparing(Race::raceName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

}
