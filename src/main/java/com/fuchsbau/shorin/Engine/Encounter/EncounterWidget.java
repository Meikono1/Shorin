package com.fuchsbau.shorin.Engine.Encounter;

import javafx.scene.Node;

public interface EncounterWidget {
    String getId();

    Node build(EncounterState state);
}