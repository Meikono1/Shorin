package com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces;

import javafx.scene.Node;

/**
 * Jede View baut sich einmal auf (build) und kann Daten neu binden (refresh).
 * Kein zweites build() — nur refresh() wenn sich Daten ändern.
 *
 * Implementierungen: LeftPanelView, CenterPanelView, RightPanelView, EncounterView
 */
public interface Renderable {

    // Einmalig — gibt den fertigen Node zurück
    Node build();

    // Nur Daten neu binden, kein Layout-Rebuild
    void refresh();
}