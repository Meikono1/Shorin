package com.fuchsbau.shorin.Engine.Editor.Module.Classes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import com.fuchsbau.shorin.Engine.System.Character.Skills;
import com.fuchsbau.shorin.Engine.System.Feat;
import com.fuchsbau.shorin.Engine.System.Misc.Trait;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class FeatModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final String DIR = "GameConfig/RPG/Feats";

    private final ObservableList<Feat> feats = FXCollections.observableArrayList();
    private Feat selected = null;

    // Verfügbare Traits + Feats + Skills für Prerequisites
    private final ObservableList<String> availableTraits = FXCollections.observableArrayList();
    private final ObservableList<String> availableFeats = FXCollections.observableArrayList();

    // UI-Refs
    private ListView<Feat> sideListView;
    private TextField nameField;
    private Spinner<Integer> levelSpinner;
    private TextArea descField;
    private ListView<String> traitListView;
    private ListView<Feat.Prerequisite> prereqListView;
    private ListView<Feat.Effect> effectListView;

    // Action
    private TextField frequencyField;
    private TextField requirementsField;
    private ListView<String> leadsToListView;
    private ComboBox<String> actionBindBox;
    private final ObservableList<String> availableActions = FXCollections.observableArrayList();

    @Override
    public String getTitle() {
        return "Feats";
    }

    // Sidebar
    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Suchen...");

        FilteredList<Feat> filtered = new FilteredList<>(feats, f -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(f ->
                        nv == null || nv.isBlank() ||
                                f.name.toLowerCase().contains(nv.toLowerCase())));

        sideListView = new ListView<>(filtered);
        sideListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Feat f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.name + " (" + f.level + ")");
            }
        });
        sideListView.setOnMouseClicked(e -> {
            Feat hit = sideListView.getSelectionModel().getSelectedItem();
            if (hit != null) loadIntoForm(hit);
        });

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew());

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        VBox box = new VBox(6, search, sideListView, newBtn, deleteBtn);
        box.setPadding(new Insets(8));
        VBox.setVgrow(sideListView, Priority.ALWAYS);
        return box;
    }

    // Content
    @Override
    public Node buildContent() {
        // --- Basis ---
        nameField = new TextField();
        nameField.setPromptText("Feat-Name");

        levelSpinner = new Spinner<>(1, 20, 1);
        levelSpinner.setEditable(true);
        levelSpinner.setMaxWidth(Double.MAX_VALUE);

        descField = new TextArea();
        descField.setPromptText("Beschreibung...");
        descField.setWrapText(true);
        descField.setPrefHeight(100);

        frequencyField = new TextField();
        frequencyField.setPromptText("z.B. once per round");
        frequencyField.setMaxWidth(Double.MAX_VALUE);

        requirementsField = new TextField();
        requirementsField.setPromptText("z.B. You have an opponent grabbed");
        requirementsField.setMaxWidth(Double.MAX_VALUE);

        VBox basisSection = buildSection("Basis",
                new Label("Name"), nameField,
                new Label("Level"), levelSpinner,
                new Label("Beschreibung"), descField,
                new Label("Frequency"), frequencyField,
                new Label("Requirements"), requirementsField
        );

        // --- Traits ---
        traitListView = new ListView<>();
        traitListView.setPrefHeight(70);
        traitListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String hit = traitListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.traits.remove(hit);
            traitListView.getItems().setAll(selected.traits);
            logger.fine("Trait entfernt: " + hit);
        });

        TextField traitSearch = new TextField();
        traitSearch.setPromptText("Trait suchen...");
        FilteredList<String> filteredTraits = new FilteredList<>(availableTraits, t -> true);
        traitSearch.textProperty().addListener((obs, ov, nv) ->
                filteredTraits.setPredicate(t ->
                        nv == null || nv.isBlank() ||
                                t.toLowerCase().contains(nv.toLowerCase())));

        ListView<String> traitPickerList = new ListView<>(filteredTraits);
        traitPickerList.setPrefHeight(90);
        traitPickerList.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String t = traitPickerList.getSelectionModel().getSelectedItem();
            if (t == null || selected.traits.contains(t)) return;
            selected.traits.add(t);
            traitListView.getItems().setAll(selected.traits);
            logger.fine("Trait hinzugefügt: " + t);
        });

        VBox traitSection = buildSection("Traits",
                new Label("Zugewiesen (Doppelklick zum Entfernen)"),
                traitListView,
                new Separator(),
                new Label("Verfügbar (Doppelklick zum Hinzufügen)"),
                traitSearch, traitPickerList
        );

        // --- Prerequisites ---
        prereqListView = new ListView<>();
        prereqListView.setPrefHeight(80);
        prereqListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Feat.Prerequisite p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    return;
                }
                String lvl = p.level.isBlank() ? "" : " (" + p.level + ")";
                setText(p.type.name() + ": " + p.ref + lvl);
            }
        });
        prereqListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            Feat.Prerequisite hit = prereqListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.prerequisites.remove(hit);
            prereqListView.getItems().setAll(selected.prerequisites);
            logger.fine("Prerequisite entfernt: " + hit.ref);
        });

        // Prerequisite hinzufügen
        ComboBox<Feat.PrerequisiteType> prereqTypeBox = new ComboBox<>(
                FXCollections.observableArrayList(Feat.PrerequisiteType.values()));
        prereqTypeBox.setPromptText("Typ");
        prereqTypeBox.setMaxWidth(Double.MAX_VALUE);

        TextField prereqSearch = new TextField();
        prereqSearch.setPromptText("Feat/Skill suchen...");

        // Dynamische Liste je nach Typ
        ObservableList<String> prereqOptions = FXCollections.observableArrayList();
        FilteredList<String> filteredPrereq = new FilteredList<>(prereqOptions, s -> true);
        prereqSearch.textProperty().addListener((obs, ov, nv) ->
                filteredPrereq.setPredicate(s ->
                        nv == null || nv.isBlank() ||
                                s.toLowerCase().contains(nv.toLowerCase())));

        prereqTypeBox.valueProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            prereqOptions.clear();
            switch (nv) {
                case FEAT -> prereqOptions.setAll(availableFeats);
                case SKILL -> prereqOptions.setAll(
                        Arrays.stream(Skills.values())
                                .map(Enum::name).toList());
            }
            logger.fine("Prerequisite-Typ gewählt: " + nv);
        });

        ListView<String> prereqPickerList = new ListView<>(filteredPrereq);
        prereqPickerList.setPrefHeight(90);

        // Skill-Level nur bei SKILL sichtbar
        ComboBox<String> skillLevelBox = new ComboBox<>(
                FXCollections.observableArrayList("trained", "expert", "master", "legendary"));
        skillLevelBox.setPromptText("Mindest-Proficiency");
        skillLevelBox.setMaxWidth(Double.MAX_VALUE);
        prereqTypeBox.valueProperty().addListener((obs, ov, nv) ->
                skillLevelBox.setVisible(nv == Feat.PrerequisiteType.SKILL));
        skillLevelBox.setVisible(false);

        Button addPrereqBtn = new Button("+ Prerequisite hinzufügen");
        addPrereqBtn.setMaxWidth(Double.MAX_VALUE);
        addPrereqBtn.setOnAction(e -> {
            if (selected == null) return;
            Feat.PrerequisiteType type = prereqTypeBox.getValue();
            String ref = prereqPickerList.getSelectionModel().getSelectedItem();
            if (type == null || ref == null) return;
            String lvl = type == Feat.PrerequisiteType.SKILL
                    ? (skillLevelBox.getValue() != null ? skillLevelBox.getValue() : "")
                    : "";
            selected.prerequisites.add(new Feat.Prerequisite(type, ref, lvl));
            prereqListView.getItems().setAll(selected.prerequisites);
            logger.fine("Prerequisite hinzugefügt: " + type + " " + ref);
        });

        VBox prereqSection = buildSection("Prerequisites",
                new Label("Zugewiesen (Doppelklick zum Entfernen)"),
                prereqListView,
                new Separator(),
                new Label("Typ"), prereqTypeBox,
                prereqSearch, prereqPickerList,
                skillLevelBox,
                addPrereqBtn
        );

        // --- Effects ---
        effectListView = new ListView<>();
        effectListView.setPrefHeight(80);
        effectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Feat.Effect ef, boolean empty) {
                super.updateItem(ef, empty);
                if (empty || ef == null) {
                    setText(null);
                    return;
                }
                setText(ef.type.name() + ": " + ef.ref + " " + ef.value);
            }
        });
        effectListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            Feat.Effect hit = effectListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.effects.remove(hit);
            effectListView.getItems().setAll(selected.effects);
            logger.fine("Effect entfernt: " + hit.ref);
        });

        ComboBox<Feat.EffectType> effectTypeBox = new ComboBox<>(
                FXCollections.observableArrayList(Feat.EffectType.values()));
        effectTypeBox.setPromptText("Effect-Typ");
        effectTypeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> effectRefBox = new ComboBox<>(
                FXCollections.observableArrayList(
                        Arrays.stream(Skills.values())
                                .map(Enum::name).toList()));
        effectRefBox.setPromptText("Skill");
        effectRefBox.setMaxWidth(Double.MAX_VALUE);

        TextField effectValueField = new TextField();
        effectValueField.setPromptText("Wert (z.B. +2, trained)");
        effectValueField.setMaxWidth(Double.MAX_VALUE);

        Button addEffectBtn = new Button("+ Effect hinzufügen");
        addEffectBtn.setMaxWidth(Double.MAX_VALUE);
        addEffectBtn.setOnAction(e -> {
            if (selected == null) return;
            Feat.EffectType type = effectTypeBox.getValue();
            String ref = effectRefBox.getValue();
            String val = effectValueField.getText().trim();
            if (type == null || ref == null || val.isBlank()) return;
            selected.effects.add(new Feat.Effect(type, ref, val));
            effectListView.getItems().setAll(selected.effects);
            logger.fine("Effect hinzugefügt: " + type + " " + ref + " " + val);
        });

        VBox effectSection = buildSection("Effects",
                new Label("Zugewiesen (Doppelklick zum Entfernen)"),
                effectListView,
                new Separator(),
                new Label("Typ"), effectTypeBox,
                new Label("Ref"), effectRefBox,
                new Label("Wert"), effectValueField,
                addEffectBtn
        );

        // --- Action Bind ---
        actionBindBox = new ComboBox<>(availableActions);
        actionBindBox.setPromptText("Keine Action gebunden...");
        actionBindBox.setMaxWidth(Double.MAX_VALUE);

        Button clearActionBtn = new Button("✕ Binding entfernen");
        clearActionBtn.setMaxWidth(Double.MAX_VALUE);
        clearActionBtn.setOnAction(e -> {
            if (selected == null) return;
            selected.grantedAction = "";
            actionBindBox.setValue(null);
            logger.fine("Action Binding entfernt");
        });

        actionBindBox.valueProperty().addListener((obs, ov, nv) -> {
            if (selected == null) return;
            selected.grantedAction = nv != null ? nv : "";
            logger.fine("Action gebunden: " + selected.grantedAction);
        });

        VBox actionSection = buildSection("Granted Action",
                new Label("Action (Doppelklick = dieser Feat IST diese Action)"),
                actionBindBox,
                clearActionBtn
        );

        // --- Leads To ---
        leadsToListView = new ListView<>();
        leadsToListView.setPrefHeight(70);
        leadsToListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String hit = leadsToListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.leadsTo.remove(hit);
            leadsToListView.getItems().setAll(selected.leadsTo);
            logger.fine("LeadsTo entfernt: " + hit);
        });

        TextField leadsToSearch = new TextField();
        leadsToSearch.setPromptText("Feat suchen...");
        FilteredList<String> filteredLeadsTo = new FilteredList<>(availableFeats, s -> true);
        leadsToSearch.textProperty().addListener((obs, ov, nv) ->
                filteredLeadsTo.setPredicate(s ->
                        nv == null || nv.isBlank() ||
                                s.toLowerCase().contains(nv.toLowerCase())));

        ListView<String> leadsToPickerList = new ListView<>(filteredLeadsTo);
        leadsToPickerList.setPrefHeight(80);
        leadsToPickerList.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String f = leadsToPickerList.getSelectionModel().getSelectedItem();
            if (f == null || selected.leadsTo.contains(f)) return;
            selected.leadsTo.add(f);
            leadsToListView.getItems().setAll(selected.leadsTo);
            logger.fine("LeadsTo hinzugefügt: " + f);
        });

        VBox leadsToSection = buildSection("Leads To",
                new Label("Folge-Feats (Doppelklick zum Entfernen)"),
                leadsToListView,
                new Separator(),
                new Label("Verfügbar (Doppelklick zum Hinzufügen)"),
                leadsToSearch, leadsToPickerList
        );

        // --- Speichern ---
        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSelected());

        VBox form = new VBox(8,
                basisSection,
                traitSection,
                prereqSection,
                effectSection,
                actionSection,
                leadsToSection,
                saveBtn
        );
        form.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // Form
    private void loadIntoForm(Feat f) {
        selected = f;
        nameField.setText(f.name);
        levelSpinner.getValueFactory().setValue(f.level);
        descField.setText(f.description);
        traitListView.getItems().setAll(f.traits);
        prereqListView.getItems().setAll(f.prerequisites);
        effectListView.getItems().setAll(f.effects);
        frequencyField.setText(f.frequency);
        requirementsField.setText(f.requirements);
        leadsToListView.getItems().setAll(f.leadsTo);
        actionBindBox.setValue(f.grantedAction.isBlank() ? null : f.grantedAction);

        logger.fine("Feat geladen: " + f.name);
    }

    private void createNew() {
        Feat f = new Feat();
        f.name = "Neuer Feat";
        feats.add(f);
        sideListView.getSelectionModel().select(f);
        loadIntoForm(f);
        logger.fine("Neuer Feat erstellt");
    }

    private void saveSelected() {
        if (selected == null) return;
        selected.name = nameField.getText().trim();
        selected.level = levelSpinner.getValue();
        selected.description = descField.getText().trim();
        selected.frequency = frequencyField.getText().trim();
        selected.requirements = requirementsField.getText().trim();
        saveToDisk();
        feats.set(feats.indexOf(selected), selected);
        logger.info("Feat gespeichert: " + selected.name);
    }

    private void deleteSelected() {
        if (selected == null) return;
        File f = PathResolver.resolveWritable(DIR + "/" + selected.name + ".json").toFile();
        if (f.exists() && !f.delete())
            logger.warning("Datei konnte nicht gelöscht werden: " + f.getName());
        feats.remove(selected);
        selected = null;
        nameField.clear();
        descField.clear();
        logger.info("Feat gelöscht");
    }

    // Disk
    private void saveToDisk() {
        if (selected == null) return;
        try {
            EditorIO.save(DIR + "/" + selected.name + ".json", selected);
            logger.info("Feat gespeichert: " + selected.name);
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        feats.clear();
        File dir = PathResolver.resolveWritable(DIR).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return;
        for (File f : files) {
            try {
                Feat feat = EditorIO.load(DIR + "/" + f.getName(),
                        new TypeReference<>() {
                        }, null);
                if (feat != null) feats.add(feat);
            } catch (Exception ex) {
                logger.warning("Feat konnte nicht geladen werden: " + f.getName());
            }
        }
        feats.sort(Comparator.comparing(f -> f.name.toLowerCase()));
        availableFeats.setAll(feats.stream().map(f -> f.name).toList());
        logger.info("Feats geladen: " + feats.size());
    }

    private void loadAvailableTraits() {
        List<Trait> loaded = EditorIO.load("Engine/traits.json",
                new TypeReference<>() {
                }, new ArrayList<>());
        availableTraits.setAll(loaded.stream().map(Trait::getName).toList());
        logger.fine("Traits geladen: " + availableTraits.size());
    }

    private void loadAvailableActions() {
        List<com.fuchsbau.shorin.Engine.System.Combat.GameAction> loaded =
                EditorIO.load("Engine/actions.json",
                        new TypeReference<>() {}, new ArrayList<>());
        availableActions.setAll(loaded.stream()
                .map(a -> a.name)
                .toList());
        logger.fine("Actions geladen: " + availableActions.size());
    }

    // Lifecycle
    @Override
    public void onActivate() {
        loadFromDisk();
        loadAvailableTraits();
        loadAvailableActions();
        if (sideListView != null) sideListView.refresh();
        logger.info("FeatModule aktiviert — " + feats.size() + " Feats");
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
    public List<Menu> getMenus() {
        return List.of();
    }
}