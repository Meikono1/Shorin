package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.Classes.FeatModule;
import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScore;
import com.fuchsbau.shorin.Engine.System.Character.PlayerBackground;
import com.fuchsbau.shorin.Engine.System.Character.Skill;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.logging.Logger;

public class BackgroundModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final String DIR = "Engine/backgrounds.json";

    private final ObservableList<PlayerBackground> backgrounds = FXCollections.observableArrayList();
    private PlayerBackground selected = null;

    // UI-Refs
    private ListView<PlayerBackground> sideListView;
    private TextField nameField;
    private TextArea descField;
    private Spinner<Integer> freeBoostSpinner;

    // Choice-Boosts: Checkboxen pro AbilityScore
    private final Map<AbilityScore, CheckBox> choiceBoxes = new EnumMap<>(AbilityScore.class);

    // Listen
    private ListView<String> skillListView;
    private ListView<String> loreListView;
    private ListView<String> featListView;

    // Available Fields
    private final ObservableList<String> availableLores = FXCollections.observableArrayList();
    private final ObservableList<String> availableFeats = FXCollections.observableArrayList();

    @Override
    public String getTitle() {
        return "Backgrounds";
    }

    @Override
    public Node buildContent() {
        nameField = new TextField();
        nameField.setPromptText("Background-Name...");
        nameField.textProperty().addListener((obs, ov, nv) -> {
            if (selected != null) {
                selected.name = nv.trim();
                logger.fine("Name: " + nv);
            }
        });

        descField = new TextArea();
        descField.setPromptText("Beschreibung...");
        descField.setWrapText(true);
        descField.setPrefHeight(90);
        descField.textProperty().addListener((obs, ov, nv) -> {
            if (selected != null) selected.description = nv;
        });

        VBox basisSection = buildSection("Basis",
                new Label("Name"), nameField,
                new Label("Beschreibung"), descField
        );

        // --- Choice Boosts ---
        GridPane choiceGrid = new GridPane();
        choiceGrid.setHgap(10);
        choiceGrid.setVgap(4);
        AbilityScore[] scores = AbilityScore.values();
        for (int i = 0; i < scores.length; i++) {
            AbilityScore s = scores[i];
            CheckBox cb = new CheckBox(s.name());
            choiceBoxes.put(s, cb);
            cb.setOnAction(e -> {
                if (selected == null) return;
                if (cb.isSelected()) {
                    if (!selected.choiceBoosts.contains(s.name())) selected.choiceBoosts.add(s);
                } else selected.choiceBoosts.remove(s.name());
                logger.fine("ChoiceBoost: " + selected.choiceBoosts);
            });
            choiceGrid.add(cb, i % 3, i / 3);
        }

        freeBoostSpinner = new Spinner<>(0, 4, 1);
        freeBoostSpinner.setEditable(true);
        freeBoostSpinner.setMaxWidth(80);
        freeBoostSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selected != null) {
                selected.freeBoosts = nv;
                logger.fine("FreeBoosts: " + nv);
            }
        });

        VBox boostSection = buildSection("Ability Boosts",
                new Label("Wählbare Boosts (mind. einer muss gewählt werden):"),
                choiceGrid,
                new Label("Zusätzliche freie Boosts:"),
                freeBoostSpinner
        );

        // --- Skills ---
        skillListView = new ListView<>();
        skillListView.setPrefHeight(80);
        skillListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String hit = skillListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.skills.remove(hit);
            skillListView.getItems().setAll(selected.skills);
            logger.fine("Skill entfernt: " + hit);
        });

        ComboBox<String> skillPicker = new ComboBox<>();
        skillPicker.setMaxWidth(Double.MAX_VALUE);
        skillPicker.getItems().setAll(
                java.util.Arrays.stream(Skill.values()).map(Skill::displayName).toList()
        );
        skillPicker.setPromptText("Skill hinzufügen...");
        skillPicker.setOnAction(e -> {
            String v = skillPicker.getValue();
            if (v == null || selected == null || selected.skills.contains(v)) return;
            selected.skills.add(v);
            skillListView.getItems().setAll(selected.skills);
            skillPicker.setValue(null);
            logger.fine("Skill hinzugefügt: " + v);
        });

        VBox skillSection = buildSection("Skills",
                new Label("Zugewiesen (Doppelklick zum Entfernen):"),
                skillListView,
                skillPicker
        );

        // --- Lores ---
        loreListView = new ListView<>();
        loreListView.setPrefHeight(60);
        loreListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String hit = loreListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.lores.remove(hit);
            loreListView.getItems().setAll(selected.lores);
            logger.fine("Lore entfernt: " + hit);
        });

        TextField loreSearch = new TextField();
        loreSearch.setPromptText("Lore suchen...");
        FilteredList<String> filteredLores = new FilteredList<>(availableLores, l -> true);
        loreSearch.textProperty().addListener((obs, ov, nv) ->
                filteredLores.setPredicate(l -> nv == null || nv.isBlank()
                        || l.toLowerCase().contains(nv.toLowerCase())));

        ListView<String> lorePickerList = new ListView<>(filteredLores);
        lorePickerList.setPrefHeight(80);
        lorePickerList.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String v = lorePickerList.getSelectionModel().getSelectedItem();
            if (v == null || selected.lores.contains(v)) return;
            selected.lores.add(v);
            loreListView.getItems().setAll(selected.lores);
            logger.fine("Lore hinzugefügt: " + v);
        });

        VBox loreSection = buildSection("Lores",
                new Label("Zugewiesen (Doppelklick zum Entfernen):"), loreListView,
                new Separator(),
                new Label("Verfügbar (Doppelklick zum Hinzufügen):"), loreSearch, lorePickerList
        );

        // --- Feats ---
        featListView = new ListView<>();
        featListView.setPrefHeight(60);
        featListView.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String hit = featListView.getSelectionModel().getSelectedItem();
            if (hit == null) return;
            selected.feats.remove(hit);
            featListView.getItems().setAll(selected.feats);
            logger.fine("Feat entfernt: " + hit);
        });

        TextField featSearch = new TextField();
        featSearch.setPromptText("Feat suchen...");
        FilteredList<String> filteredFeats = new FilteredList<>(availableFeats, f -> true);
        featSearch.textProperty().addListener((obs, ov, nv) ->
                filteredFeats.setPredicate(f -> nv == null || nv.isBlank()
                        || f.toLowerCase().contains(nv.toLowerCase())));

        ListView<String> featPickerList = new ListView<>(filteredFeats);
        featPickerList.setPrefHeight(80);
        featPickerList.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || selected == null) return;
            String v = featPickerList.getSelectionModel().getSelectedItem();
            if (v == null || selected.feats.contains(v)) return;
            selected.feats.add(v);
            featListView.getItems().setAll(selected.feats);
            logger.fine("Feat hinzugefügt: " + v);
        });

        VBox featSection = buildSection("Skill Feats",
                new Label("Zugewiesen (Doppelklick zum Entfernen):"), featListView,
                new Separator(),
                new Label("Verfügbar (Doppelklick zum Hinzufügen):"), featSearch, featPickerList
        );

        // --- Save/Delete ---
        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSelected());

        VBox content = new VBox(10,
                basisSection, boostSection, skillSection, loreSection, featSection, saveBtn
        );
        content.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Suchen...");

        FilteredList<PlayerBackground> filtered = new FilteredList<>(backgrounds, b -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(b ->
                        nv == null || nv.isBlank() ||
                                b.name.toLowerCase().contains(nv.toLowerCase())));

        sideListView = new ListView<>(filtered);
        sideListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PlayerBackground b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null : b.name);
            }
        });
        sideListView.setOnMouseClicked(e -> {
            PlayerBackground hit = sideListView.getSelectionModel().getSelectedItem();
            if (hit != null) loadIntoForm(hit);
        });
        VBox.setVgrow(sideListView, Priority.ALWAYS);

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew());

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        VBox box = new VBox(6, search, sideListView, newBtn, deleteBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    // --- Form befüllen ---
    private void loadIntoForm(PlayerBackground b) {
        selected = b;
        nameField.setText(b.name);
        descField.setText(b.description);
        freeBoostSpinner.getValueFactory().setValue(b.freeBoosts);

        choiceBoxes.forEach((score, cb) ->
                cb.setSelected(b.choiceBoosts.contains(score)));

        skillListView.getItems().setAll(b.skills);
        loreListView.getItems().setAll(b.lores);
        featListView.getItems().setAll(b.feats);


        logger.fine("Background geladen: " + b.name);
    }

    private void createNew() {
        PlayerBackground b = new PlayerBackground();
        b.name = "Neuer Background";
        backgrounds.add(b);
        sideListView.getSelectionModel().select(b);
        loadIntoForm(b);
        logger.fine("Neuer Background erstellt");
    }

    private void saveSelected() {
        if (selected == null) return;
        selected.name = nameField.getText().trim();
        backgrounds.set(backgrounds.indexOf(selected), selected);
        saveToDisk();
        logger.info("Background gespeichert: " + selected.name);
    }

    private void deleteSelected() {
        if (selected == null) return;
        backgrounds.remove(selected);
        selected = null;
        nameField.clear();
        descField.clear();
        saveToDisk();
        logger.info("Background gelöscht");
    }

    private void saveToDisk() {
        try {
            EditorIO.save(DIR, new ArrayList<>(backgrounds));
            logger.info("Backgrounds gespeichert: " + backgrounds.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        List<PlayerBackground> loaded = EditorIO.load(DIR, new TypeReference<>() {
        }, new ArrayList<>());
        backgrounds.setAll(loaded);
        backgrounds.sort(Comparator.comparing(b -> b.name.toLowerCase()));
        logger.info("Backgrounds geladen: " + backgrounds.size());
    }

    // statische Hilfsmethode — wird später von CharacterModule genutzt
    public static List<PlayerBackground> loadAll() {
        return EditorIO.load(DIR, new TypeReference<>() {
        }, new ArrayList<>());
    }

    @Override
    public void onActivate() {
        loadFromDisk();

        availableLores.setAll(LoreModule.loadAllNames());
        logger.fine("Lores verfügbar: " + availableLores.size());

        availableFeats.setAll(FeatModule.loadAllNames());
        logger.fine("Feats verfügbar: " + availableFeats.size());

        if (!backgrounds.isEmpty()) loadIntoForm(backgrounds.getFirst());
    }

    @Override
    public void onDeactivate() {
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    public static List<String> loadAllNames() {
        List<Ancestrie> loaded = EditorIO.load(
                DIR, new TypeReference<>() {
                }, new ArrayList<>());
        return loaded.stream().map(a -> a.name).sorted().toList();
    }
}