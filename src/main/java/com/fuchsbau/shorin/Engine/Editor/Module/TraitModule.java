package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.System.Misc.Trait;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class TraitModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final String FILE = "Engine/traits.json";

    // Daten
    private final ObservableList<Trait> traits = FXCollections.observableArrayList();
    private Trait selected = null;

    // UI-Refs für Formular
    private TextField nameField;
    private TextArea descField;

    @Override
    public String getTitle() {
        return "Traits";
    }

    @Override
    public Node buildContent() {
        return buildFormPanel();
    }

    // --- Formular rechts ---
    private Node buildFormPanel() {
        Label nameLabel = new Label("Name");
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameField = new TextField();
        nameField.setPromptText("Trait-Name");

        Label descLabel = new Label("Beschreibung");
        descLabel.setTextFill(Color.LIGHTGRAY);
        descField = new TextArea();
        descField.setPromptText("Beschreibung des Traits...");
        descField.setWrapText(true);
        VBox.setVgrow(descField, Priority.ALWAYS);

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSelected());

        Button deleteBtn = new Button("Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        VBox form = new VBox(6,
                nameLabel, nameField,
                descLabel, descField,
                new Separator(),
                saveBtn, deleteBtn
        );
        form.setPadding(new Insets(8));
        return form;
    }

    // --- Formular befüllen ---
    private void loadIntoForm(Trait t) {
        selected = t;
        nameField.setText(t.getName());
        descField.setText(t.getDescription());
    }

    // --- Neuen Trait anlegen ---
    private void createNew(ListView<Trait> listView) {
        Trait t = new Trait("Neuer Trait", "");
        traits.add(t);
        listView.getSelectionModel().select(t);
        loadIntoForm(t);
    }

    // --- Änderungen aus Formular in Objekt schreiben + speichern ---
    private void saveSelected() {
        if (selected == null) return;

        String newName = nameField.getText().trim();

        // Duplikat suchen und entfernen
        for (int i = traits.size() - 1; i >= 0; i--) {
            Trait t = traits.get(i);
            if (t != selected && t.getName().equalsIgnoreCase(newName)) {
                traits.remove(i);
                logger.info("Duplikat überschrieben: " + newName);
                break;
            }
        }

        selected.setName(newName);
        selected.setDescription(descField.getText().trim());
        saveToDisk();

        traits.set(traits.indexOf(selected), selected);
    }

    // --- Löschen ---
    private void deleteSelected() {
        if (selected == null) return;
        traits.remove(selected);
        selected = null;
        nameField.clear();
        descField.clear();
        saveToDisk();
    }

    // --- Disk ---
    private void saveToDisk() {
        try {
            List<Trait> sorted = traits.stream()
                    .sorted(Comparator.comparing(t -> t.getName().toLowerCase()))
                    .toList();
            EditorIO.save(FILE, sorted);
            logger.info("Traits gespeichert: " + traits.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        List<Trait> loaded = EditorIO.load(FILE,
                new TypeReference<>() {
                }, new ArrayList<>());
        traits.setAll(loaded);
        logger.info("Traits geladen: " + traits.size());
    }

    // --- Lifecycle ---
    @Override
    public void onActivate() {
        loadFromDisk();
    }

    @Override
    public void onDeactivate() {
        saveToDisk();
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Suchen...");

        FilteredList<Trait> filtered = new FilteredList<>(traits, t -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(t ->
                        nv == null || nv.isBlank() ||
                                t.getName().toLowerCase().contains(nv.toLowerCase())));

        ListView<Trait> listView = new ListView<>(filtered);
        listView.setOnMouseClicked(e -> {
            Trait t = listView.getSelectionModel().getSelectedItem();
            if (t != null) loadIntoForm(t);
        });

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew(listView));

        VBox box = new VBox(6, search, listView, newBtn);
        box.setPadding(new Insets(8));
        VBox.setVgrow(listView, Priority.ALWAYS);
        return box;
    }

    public static List<Trait> loadAvailableTraits() {
        return EditorIO.load(
                "Engine/traits.json",
                new TypeReference<>() {
                },
                new ArrayList<>());
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }
}