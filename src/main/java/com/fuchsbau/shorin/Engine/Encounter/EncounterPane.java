package com.fuchsbau.shorin.Engine.Encounter;

import com.fuchsbau.shorin.Engine.Encounter.Widget.EncounterWidget;
import com.fuchsbau.shorin.Engine.Encounter.Widget.InitiativeTrackerWidget;
import com.fuchsbau.shorin.Engine.Map.Core.MapRenderer;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EncounterPane {
    private final Logger logger = FileLogger.getLogger();

    private InitiativeTrackerWidget trackerWidget;
    public final BooleanProperty inCombat = new SimpleBooleanProperty(false);

    private final AnchorPane root = new AnchorPane();
    private final EncounterState state = new EncounterState();
    private final List<EncounterWidget> widgets = new ArrayList<>();

    public EncounterPane(MapRenderer mapRenderer) {
        // Map
        Node mapNode = mapRenderer.buildBattleMapPane(null);
        AnchorPane.setTopAnchor(mapNode, 0.0);
        AnchorPane.setBottomAnchor(mapNode, 0.0);
        AnchorPane.setLeftAnchor(mapNode, 0.0);
        AnchorPane.setRightAnchor(mapNode, 0.0);
        root.getChildren().add(mapNode);
        logger.fine("MapNode eingehängt");

        // Initiative-Leiste — oben, volle Breite, an inCombat gebunden
        trackerWidget = new InitiativeTrackerWidget();
        trackerWidget.visible.bind(inCombat);
        Node trackerNode = trackerWidget.build(state);
        AnchorPane.setTopAnchor(trackerNode, 0.0);
        AnchorPane.setLeftAnchor(trackerNode, 0.0);
        AnchorPane.setRightAnchor(trackerNode, 0.0);
        root.getChildren().add(trackerNode);
        logger.info("InitiativeTracker eingehängt — gebunden an inCombat");

        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setMinSize(0, 0);
    }

    public void addWidget(EncounterWidget widget, WidgetAnchor anchor) {
        widgets.add(widget);
        Node node = widget.build(state);

        switch (anchor) {
            case TOP_LEFT -> {
                AnchorPane.setTopAnchor(node, 8.0);
                AnchorPane.setLeftAnchor(node, 8.0);
            }
            case TOP_RIGHT -> {
                AnchorPane.setTopAnchor(node, 8.0);
                AnchorPane.setRightAnchor(node, 8.0);
            }
            case BOTTOM_LEFT -> {
                AnchorPane.setBottomAnchor(node, 8.0);
                AnchorPane.setLeftAnchor(node, 8.0);
            }
            case BOTTOM_RIGHT -> {
                AnchorPane.setBottomAnchor(node, 8.0);
                AnchorPane.setRightAnchor(node, 8.0);
            }
            case BOTTOM_CENTER -> {
                AnchorPane.setBottomAnchor(node, 8.0);
                AnchorPane.setLeftAnchor(node, 0.0);
                AnchorPane.setRightAnchor(node, 0.0);
            }
            case LEFT_CENTER -> {
                AnchorPane.setTopAnchor(node, 0.0);
                AnchorPane.setBottomAnchor(node, 0.0);
                AnchorPane.setLeftAnchor(node, 8.0);
            }
            case RIGHT_CENTER -> {
                AnchorPane.setTopAnchor(node, 0.0);
                AnchorPane.setBottomAnchor(node, 0.0);
                AnchorPane.setRightAnchor(node, 8.0);
            }
            case FLOAT -> {
            } // manuell positioniert
        }

        root.getChildren().add(node);
        logger.fine("Widget registriert: " + widget.getId() + " @ " + anchor);
    }

    public AnchorPane getRoot() {
        return root;
    }

    public EncounterState getState() {
        return state;
    }

    public BooleanProperty inCombatProperty() {
        return inCombat;
    }
}