package com.fuchsbau.shorin.Engine.Encounter.Widget;

import com.fuchsbau.shorin.Engine.Encounter.EncounterState;
import javafx.scene.Node;

public interface EncounterWidget {
    String getId();

    Node build(EncounterState state);
}