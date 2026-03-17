package com.fuchsbau.shorin.Engine.Editor;

import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class WorldMapEditorModule implements EditorModule {

    @Override
    public String getTitle() {
        return "World Editor";
    }

    @Override
    public Node buildContent() {
        // Platzhalter — hier kommt der Graph-Canvas rein
        return new Label("Weltkarte (Graph-Editor)");
    }

    // Kontextsensitive Toolbar unter den Editor-Tabs
    @Override
    public Node buildToolbar() {
        Button addNode = new Button("+ Knoten");
        Button addEdge = new Button("~ Verbindung");
        Button deleteBtn = new Button("✕ Löschen");
        Button selectBtn = new Button("⬡ Auswählen");

        // Später: ToggleGroup damit immer nur ein Tool aktiv ist
        return new HBox(4, selectBtn, addNode, addEdge, deleteBtn);
    }

    // Accordion-Panel links — scrollbar, aufklappbare Sektionen
    @Override
    public Node buildSidePanel() {
        Accordion accordion = new Accordion();

        // Sektion: Knoten
        VBox nodeSection = new VBox(4,
                new Button("Ort"),
                new Button("Dungeon"),
                new Button("Stadt"),
                new Button("Lager")
        );
        accordion.getPanes().add(new TitledPane("Knoten-Typen", nodeSection));

        // Sektion: Verbindungen
        VBox edgeSection = new VBox(4,
                new Button("Straße"),
                new Button("Pfad"),
                new Button("Fluss"),
                new Button("Gebirgspass")
        );
        accordion.getPanes().add(new TitledPane("Verbindungs-Typen", edgeSection));

        // Sektion: Zonen — später
        VBox zoneSection = new VBox(4,
                new Label("(noch nicht implementiert)")
        );
        accordion.getPanes().add(new TitledPane("Zonen", zoneSection));

        return accordion;
    }

    @Override
    public List<Menu> getMenus() {
        Menu mapMenu = new Menu("Karte");

        MenuItem newNode = new MenuItem("Knoten hinzufügen");
        MenuItem newEdge = new MenuItem("Verbindung ziehen");
        MenuItem deleteNode = new MenuItem("Knoten löschen");

        mapMenu.getItems().addAll(newNode, newEdge, new SeparatorMenuItem(), deleteNode);

        return List.of(mapMenu);
    }

    @Override
    public void onActivate() {
        // Canvas-Renderer starten
    }

    @Override
    public void onDeactivate() {
        // Canvas-Renderer stoppen
    }
}