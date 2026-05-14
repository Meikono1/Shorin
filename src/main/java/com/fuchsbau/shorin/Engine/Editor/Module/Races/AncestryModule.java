package com.fuchsbau.shorin.Engine.Editor.Module.Races;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import com.fuchsbau.shorin.Engine.Editor.Module.TraitModule;
import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Engine.RPG.Language;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScore;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScoreEntry;
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
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class AncestryModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final String DIR = "RPG/Ancesteries";

    private final ObservableList<Ancestrie> ancestries = FXCollections.observableArrayList();
    private Ancestrie selected = null;

    // UI-Refs
    private TextField nameField;
    private Spinner<Integer> healthSpinner;
    private Spinner<Integer> speedSpinner;
    private ComboBox<Size> sizeBox;
    private ListView<Ancestrie> sideListView;
    private ListView<AbilityScoreEntry> boostListView;
    private ListView<String> traitListView;
    private ListView<Language> languageListView;
    private Spinner<Integer> freeBoostSpinner;

    // Verfügbares
    private final ObservableList<String> availableTraits = FXCollections.observableArrayList();
    private final ObservableList<Language> availableLanguages = FXCollections.observableArrayList();


    @Override
    public String getTitle() {
        return "Ancestries";
    }

    @Override
    public Node buildContent() {
        return buildFormPanel();
    }

    // --- Formular ---
    private Node buildFormPanel() {

        // --- Basis ---
        nameField = new TextField();
        nameField.setPromptText("Ancestry-Name");

        healthSpinner = new Spinner<>(1, 30, 8);
        healthSpinner.setEditable(true);
        healthSpinner.setMaxWidth(Double.MAX_VALUE);

        speedSpinner = new Spinner<>(5, 60, 25, 5);
        speedSpinner.setEditable(true);
        speedSpinner.setMaxWidth(Double.MAX_VALUE);

        sizeBox = new ComboBox<>(FXCollections.observableArrayList(Size.values()));
        sizeBox.setValue(Size.MEDIUM);
        sizeBox.setMaxWidth(Double.MAX_VALUE);

        freeBoostSpinner = new Spinner<>(0, 5, 0);
        freeBoostSpinner.setEditable(true);
        freeBoostSpinner.setMaxWidth(Double.MAX_VALUE);

        VBox basisSection = buildSection("Basis",
                new Label("Name"), nameField,
                new Label("HP (Level 1)"), healthSpinner,
                new Label("Speed (ft)"), speedSpinner,
                new Label("Größe"), sizeBox,
                new Label("Freie Ability Boosts"), freeBoostSpinner
        );

        // --- Ability Boosts ---
        boostListView = new ListView<>();
        boostListView.setPrefHeight(80);
        boostListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AbilityScoreEntry b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) {
                    setText(null);
                    return;
                }
                setText((b.value > 0 ? "+" : "") + b.value + " " + b.abilityScore.name());
            }
        });
        boostListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            AbilityScoreEntry hit = boostListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.abilityBoosts.remove(hit);
            boostListView.getItems().setAll(selected.abilityBoosts);
            logger.fine("Boost entfernt: " + hit.abilityScore.name());
        });

        ComboBox<AbilityScore> boostScoreBox = new ComboBox<>(
                FXCollections.observableArrayList(AbilityScore.values()));
        boostScoreBox.setPromptText("Score wählen...");
        boostScoreBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Integer> boostValueBox = new ComboBox<>(
                FXCollections.observableArrayList(1, -1));
        boostValueBox.setPromptText("+1 / -1");
        boostValueBox.setMaxWidth(Double.MAX_VALUE);
        boostValueBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v > 0 ? "+1 Boost" : "-1 Flaw"));
            }
        });
        boostValueBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v > 0 ? "+1 Boost" : "-1 Flaw"));
            }
        });

        Button addBoostBtn = new Button("+ Boost hinzufügen");
        addBoostBtn.setMaxWidth(Double.MAX_VALUE);
        addBoostBtn.setOnAction(e -> {
            if (selected == null) return;
            AbilityScore score = boostScoreBox.getValue();
            Integer val = boostValueBox.getValue();
            if (score == null || val == null) return;
            selected.abilityBoosts.add(new AbilityScoreEntry(score, val));
            boostListView.getItems().setAll(selected.abilityBoosts);
            logger.fine("Boost hinzugefügt: " + val + " " + score);
        });

        HBox boostRow = new HBox(4, boostScoreBox, boostValueBox);
        HBox.setHgrow(boostScoreBox, Priority.ALWAYS);
        HBox.setHgrow(boostValueBox, Priority.ALWAYS);

        VBox boostSection = buildSection("Ability Boosts / Flaws",
                new Label("Zugewiesen (Doppelklick zum Entfernen)"),
                boostListView,
                new Separator(),
                new Label("Hinzufügen"),
                boostRow,
                addBoostBtn
        );

        // --- Traits ---
        traitListView = new ListView<>();
        traitListView.setPrefHeight(80);
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
        traitPickerList.setPrefHeight(100);
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
                traitSearch,
                traitPickerList
        );

        // --- Sprachen ---
        languageListView = new ListView<>();
        languageListView.setPrefHeight(80);
        languageListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Language lang, boolean empty) {
                super.updateItem(lang, empty);
                setText(empty || lang == null ? null : lang.name);
            }
        });
        languageListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            Language hit = languageListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.languages.remove(hit);
            languageListView.getItems().setAll(selected.languages);
            logger.fine("Sprache entfernt: " + hit);
        });

        ListView<Language> langPickerList = new ListView<>(availableLanguages);
        langPickerList.setPrefHeight(100);
        langPickerList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Language lang, boolean empty) {
                super.updateItem(lang, empty);
                setText(empty || lang == null ? null : lang.name);
            }
        });
        langPickerList.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            Language lang = langPickerList.getSelectionModel().getSelectedItem();
            if (lang == null) return;

            boolean alreadyAdded = selected.languages.stream()
                    .anyMatch(l -> l.name.equalsIgnoreCase(lang.name));
            if (alreadyAdded) return;
            selected.languages.add(lang);
            languageListView.getItems().setAll(selected.languages);
            logger.fine("Sprache hinzugefügt: " + lang.name);
        });

        VBox langSection = buildSection("Sprachen",
                new Label("Zugewiesen (Doppelklick zum Entfernen)"),
                languageListView,
                new Separator(),
                new Label("Verfügbar (Doppelklick zum Hinzufügen)"),
                langPickerList
        );

        // --- Speichern ---
        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSelected());

        // --- Layout ---
        VBox form = new VBox(8,
                basisSection,
                boostSection,
                traitSection,
                langSection,
                saveBtn
        );
        form.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // --- Formular befüllen ---
    private void loadIntoForm(Ancestrie a) {
        selected = a;
        nameField.setText(a.name);
        healthSpinner.getValueFactory().setValue(a.health);
        speedSpinner.getValueFactory().setValue(a.speedFt);
        sizeBox.setValue(a.size);
        boostListView.getItems().setAll(a.abilityBoosts);
        traitListView.getItems().setAll(a.traits);
        languageListView.getItems().setAll(a.languages);
        freeBoostSpinner.getValueFactory().setValue(a.freeBoosts);
        logger.fine("Ancestry geladen: " + a.name);
    }

    // --- Neu ---
    private void createNew(ListView<Ancestrie> listView) {
        Ancestrie a = new Ancestrie();
        a.name = "Neue Ancestry";
        ancestries.add(a);
        listView.getSelectionModel().select(a);
        loadIntoForm(a);
        logger.fine("Neue Ancestry erstellt");
    }

    // --- Speichern ---
    private void saveSelected() {
        if (selected == null) return;
        selected.name = nameField.getText().trim();
        selected.health = healthSpinner.getValue();
        selected.speedFt = speedSpinner.getValue();
        selected.size = sizeBox.getValue();
        selected.freeBoosts = freeBoostSpinner.getValue();

        saveToDisk();
        ancestries.set(ancestries.indexOf(selected), selected);
        logger.info("Ancestry gespeichert: " + selected.name);
    }

    // --- Löschen ---
    private void deleteSelected() {
        if (selected == null) return;
        File f = new File(
                PathResolver.resolveWritable("GameConfig/" + DIR).toFile(),
                selected.name + ".json");
        if (f.exists() && !f.delete()) {
            logger.warning("Datei konnte nicht gelöscht werden: " + f.getName());
        }
        ancestries.remove(selected);
        selected = null;
        nameField.clear();
        logger.info("Ancestry gelöscht");
    }

    // --- Disk ---
    private void saveToDisk() {
        if (selected == null) return;
        File dir = PathResolver.resolveWritable("GameConfig/" + DIR).toFile();
        dir.mkdirs();

        File f = new File(dir, selected.name + ".json");
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(f, selected);
            logger.info("Ancestry gespeichert: " + f.getAbsolutePath());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        ancestries.clear();
        File dir = PathResolver.resolveWritable("GameConfig/" + DIR).toFile();

        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Ancestry-Verzeichnis erstellt: " + dir.getAbsolutePath());
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            logger.info("Keine Ancestry-Dateien gefunden in: " + dir.getAbsolutePath());
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        for (File f : files) {
            try {
                Ancestrie a = mapper.readValue(f, Ancestrie.class);
                ancestries.add(a);
                logger.fine("Ancestry geladen: " + f.getName());
            } catch (Exception ex) {
                logger.warning("Ancestry konnte nicht geladen werden: "
                        + f.getName() + " — " + ex.getMessage());
            }
        }

        ancestries.sort(Comparator.comparing(a -> a.name.toLowerCase()));
        logger.info("Ancestries geladen: " + ancestries.size());
    }

    private void loadAvailableTraits() {
        List<Trait> loaded = TraitModule.loadAvailableTraits();
        availableTraits.clear();
        for (Trait t : loaded) availableTraits.add(t.getName());
        logger.fine("Traits geladen: " + availableTraits.size());
    }


    private void loadAvailableLanguages() {
        List<Language> loaded = EditorIO.load("Engine/languages.json",
                new TypeReference<>() {
                }, new ArrayList<>());
        availableLanguages.setAll(loaded);
        logger.fine("Verfügbare Sprachen geladen: " + availableLanguages.size());
    }

    // --- Lifecycle ---
    @Override
    public void onActivate() {
        loadFromDisk();
        loadAvailableTraits();
        loadAvailableLanguages();
        if (sideListView != null) sideListView.refresh();
    }

    @Override
    public void onDeactivate() {
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Suchen...");

        FilteredList<Ancestrie> filtered = new FilteredList<>(ancestries, a -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(a ->
                        nv == null || nv.isBlank() ||
                                a.name.toLowerCase().contains(nv.toLowerCase())));

        sideListView = new ListView<>(filtered);

        ListView<Ancestrie> listView = new ListView<>(filtered);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ancestrie a, boolean empty) {
                super.updateItem(a, empty);
                setText(empty || a == null ? null : a.name);
            }
        });
        listView.setOnMouseClicked(e -> {
            Ancestrie hit = listView.getSelectionModel().getSelectedItem();
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

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    public static List<String> loadAllNames() {
        return loadAll().stream().map(a -> a.name).toList();
    }

    public static List<Ancestrie> loadAll() {
        File dir = PathResolver.resolveWritable("GameConfig/" + DIR).toFile();
        if (!dir.exists()) return new ArrayList<>();

        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) return new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        List<Ancestrie> result = new ArrayList<>();
        for (File f : files) {
            try {
                result.add(mapper.readValue(f, Ancestrie.class));
            } catch (Exception ex) {
                logger.warning("Ancestry loadAll Fehler: " + f.getName() + " — " + ex.getMessage());
            }
        }
        result.sort(Comparator.comparing(a -> a.name.toLowerCase()));
        logger.fine("AncestryModule.loadAll: " + result.size());
        return result;
    }
}