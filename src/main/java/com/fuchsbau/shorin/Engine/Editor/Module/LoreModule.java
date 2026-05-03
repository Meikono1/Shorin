package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.RPG.Lore;
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

public class LoreModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    static final String FILE = "Engine/lores.json";

    private final ObservableList<Lore> lores = FXCollections.observableArrayList();
    private Lore selected = null;

    private TextField nameField;
    private TextArea  descField;

    @Override
    public String getTitle() { return "Lores"; }

    @Override
    public Node buildContent() {
        nameField = new TextField();
        nameField.setPromptText("Lore-Name");

        descField = new TextArea();
        descField.setPromptText("Beschreibung...");
        descField.setWrapText(true);
        descField.setPrefHeight(120);

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSelected());

        VBox section = buildSection("Lore",
                new Label("Name"), nameField,
                new Label("Beschreibung"), descField
        );

        VBox form = new VBox(8, section, saveBtn);
        form.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Suchen...");

        FilteredList<Lore> filtered = new FilteredList<>(lores, l -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(l ->
                        nv == null || nv.isBlank() ||
                                l.name.toLowerCase().contains(nv.toLowerCase())));

        ListView<Lore> listView = new ListView<>(filtered);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Lore l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null : l.name);
            }
        });
        listView.setOnMouseClicked(e -> {
            Lore hit = listView.getSelectionModel().getSelectedItem();
            if (hit != null) loadIntoForm(hit);
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> {
            Lore l = new Lore("Neues Lore", "");
            lores.add(l);
            listView.getSelectionModel().select(l);
            loadIntoForm(l);
            logger.fine("Neues Lore erstellt");
        });

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        VBox box = new VBox(6, search, listView, newBtn, deleteBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    private void loadIntoForm(Lore l) {
        selected = l;
        nameField.setText(l.name);
        descField.setText(l.description);
        logger.fine("Lore geladen: " + l.name);
    }

    private void saveSelected() {
        if (selected == null) return;
        selected.name        = nameField.getText().trim();
        selected.description = descField.getText().trim();
        lores.set(lores.indexOf(selected), selected);
        saveToDisk();
        logger.info("Lore gespeichert: " + selected.name);
    }

    private void deleteSelected() {
        if (selected == null) return;
        lores.remove(selected);
        selected = null;
        nameField.clear();
        descField.clear();
        saveToDisk();
        logger.info("Lore gelöscht");
    }

    private void saveToDisk() {
        List<Lore> sorted = new ArrayList<>(lores);
        sorted.sort(Comparator.comparing(l -> l.name.toLowerCase()));
        try {
            EditorIO.save(FILE, sorted);
            logger.info("Lores gespeichert: " + sorted.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        List<Lore> loaded = EditorIO.load(FILE, new TypeReference<>() {}, new ArrayList<>());
        lores.setAll(loaded);
        logger.info("Lores geladen: " + lores.size());
    }

    public static List<String> loadAllNames() {
        List<Lore> loaded = EditorIO.load(FILE, new TypeReference<>() {}, new ArrayList<>());
        return loaded.stream().map(l -> l.name).sorted().toList();
    }

    @Override
    public void onActivate() {
        loadFromDisk();
        if (!lores.isEmpty()) loadIntoForm(lores.getFirst());
    }

    @Override
    public void onDeactivate() {  }

    @Override
    public Node buildToolbar() { return null; }

    @Override
    public List<Menu> getMenus() { return List.of(); }
}