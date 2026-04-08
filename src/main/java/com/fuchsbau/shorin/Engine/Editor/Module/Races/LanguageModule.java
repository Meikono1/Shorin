package com.fuchsbau.shorin.Engine.Editor.Module.Races;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import com.fuchsbau.shorin.Engine.RPG.Language;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class LanguageModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final String FILE = "Engine/languages.json";

    private final ObservableList<Language> languages = FXCollections.observableArrayList();
    private Language selected = null;

    // UI-Refs
    private TextField nameField;
    private TextArea descField;
    private TextField templateField;

    @Override
    public String getTitle() {
        return "Sprachen";
    }

    @Override
    public Node buildContent() {
        return buildFormPanel();
    }

    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Suchen...");

        FilteredList<Language> filtered = new FilteredList<>(languages, l -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(l ->
                        nv == null || nv.isBlank() ||
                                l.name.toLowerCase().contains(nv.toLowerCase())));

        ListView<Language> listView = new ListView<>(filtered);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Language l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null : l.name);
            }
        });
        listView.setOnMouseClicked(e -> {
            Language hit = listView.getSelectionModel().getSelectedItem();
            if (hit != null) loadIntoForm(hit);
        });

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew(listView));

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        VBox box = new VBox(6, search, listView, newBtn, deleteBtn);
        box.setPadding(new Insets(8));
        VBox.setVgrow(listView, Priority.ALWAYS);
        return box;
    }

    private Node buildFormPanel() {
        nameField = new TextField();
        nameField.setPromptText("Sprach-Name");

        descField = new TextArea();
        descField.setPromptText("Beschreibung der Sprache...");
        descField.setWrapText(true);
        descField.setPrefHeight(120);

        templateField = new TextField();
        templateField.setPromptText("Decryption-Template");

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSelected());

        VBox basisSection = buildSection("Sprache",
                new Label("Name"), nameField,
                new Label("Beschreibung"), descField,
                new Label("Decryption Template"), templateField
        );

        VBox form = new VBox(8, basisSection, saveBtn);
        form.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private void loadIntoForm(Language l) {
        selected = l;
        nameField.setText(l.name);
        descField.setText(l.description);
        templateField.setText(l.decryptionTemplate);
        logger.fine("Sprache geladen: " + l.name);
    }

    private void createNew(ListView<Language> listView) {
        Language l = new Language("Neue Sprache", "");
        languages.add(l);
        listView.getSelectionModel().select(l);
        loadIntoForm(l);
        logger.fine("Neue Sprache erstellt");
    }

    private void saveSelected() {
        if (selected == null) return;

        String newName = nameField.getText().trim();

        // Duplikat entfernen
        for (int i = languages.size() - 1; i >= 0; i--) {
            Language l = languages.get(i);
            if (l != selected && l.name.equalsIgnoreCase(newName)) {
                languages.remove(i);
                logger.info("Duplikat überschrieben: " + newName);
                break;
            }
        }

        selected.name = newName;
        selected.description = descField.getText().trim();
        selected.decryptionTemplate = templateField.getText().trim();
        saveToDisk();
        languages.set(languages.indexOf(selected), selected);
        logger.info("Sprache gespeichert: " + selected.name);
    }

    private void deleteSelected() {
        if (selected == null) return;
        languages.remove(selected);
        selected = null;
        nameField.clear();
        descField.clear();
        templateField.clear();
        saveToDisk();
        logger.info("Sprache gelöscht");
    }

    private void saveToDisk() {
        List<Language> sorted = new ArrayList<>(languages);
        sorted.sort(Comparator.comparing(l -> l.name.toLowerCase()));
        try {
            EditorIO.save(FILE, sorted);
            logger.info("Sprachen gespeichert: " + sorted.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        List<Language> loaded = EditorIO.load(FILE,
                new TypeReference<>() {}, new ArrayList<>());
        languages.setAll(loaded);
        logger.info("Sprachen geladen: " + languages.size());
    }

    @Override
    public void onActivate() {
        loadFromDisk();
    }

    @Override
    public void onDeactivate() {
        saveToDisk();
    }

    @Override
    public Node buildToolbar() { return null; }

    @Override
    public List<Menu> getMenus() { return List.of(); }
}