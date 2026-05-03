package com.fuchsbau.shorin.Engine.Editor.Module.Classes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScores;
import com.fuchsbau.shorin.Engine.System.Character.ClassBuild;
import com.fuchsbau.shorin.Engine.System.Character.WeaponCategory;
import com.fuchsbau.shorin.Engine.System.Combat.SavingThrows;
import com.fuchsbau.shorin.Engine.System.Misc.Expertise;
import com.fuchsbau.shorin.Engine.System.Combat.ArmorCategory;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.logging.Logger;

public class ClassModule implements EditorModule {
    private static final Logger logger = FileLogger.getLogger();
    private static final String FILE = "Engine/classes.json";

    private TextField classNameField = new TextField();
    private TextArea descriptionArea = new TextArea();

    private final ObservableList<ClassBuild> classes = FXCollections.observableArrayList();
    private ClassBuild selectedClass = null;
    private ListView<ClassBuild> classListView;
    private final Map<SavingThrows, ComboBox<Expertise>> saveBoxes = new EnumMap<>(SavingThrows.class);
    private final Map<ArmorCategory, ComboBox<Expertise>> armorBoxes = new EnumMap<>(ArmorCategory.class);
    private final Map<WeaponCategory, ComboBox<Expertise>> weaponBoxes = new EnumMap<>(WeaponCategory.class);

    private Spinner<Integer> hpPerLevelSpinner = new Spinner<>(1, 99, 1);

    private final Map<AbilityScores, CheckBox> abilityButtons = new EnumMap<>(AbilityScores.class);

    @Override
    public String getTitle() {
        return "Classes";
    }

    @Override
    public Node buildContent() {
        VBox sectionsCol = new VBox(12);
        sectionsCol.setPadding(new Insets(8));
        sectionsCol.setFillWidth(true);

        sectionsCol.getChildren().addAll(
                buildNameSection(),
                buildDescriptionSection(),
                buildKeyAbilitySection(),
                buildProficienciesSection()
        );

        ScrollPane scroll = new ScrollPane(sectionsCol);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveToDisk());

        BorderPane root = new BorderPane();
        root.setCenter(scroll);
        root.setBottom(saveBtn);
        return root;
    }

    private Node buildNameSection() {
        classNameField = new TextField();
        classNameField.setPromptText("Klassenname...");
        classNameField.setMaxWidth(Double.MAX_VALUE);
        classNameField.textProperty().addListener((obs, ov, nv) -> {
            if (selectedClass != null) {
                selectedClass.name = nv.trim();
                logger.fine("Klassenname gesetzt: " + nv);
            }
        });

        return buildSection("Name", getClassNameField());
    }

    private TextField getClassNameField() {
        if (classNameField == null) {
            classNameField = new TextField();
            classNameField.setPromptText("Klassenname...");
            classNameField.setMaxWidth(Double.MAX_VALUE);
            classNameField.textProperty().addListener((obs, ov, nv) -> {
                if (selectedClass != null) {
                    selectedClass.name = nv.trim();
                    logger.fine("Klassenname gesetzt: " + nv);
                }
            });
        }
        return classNameField;
    }

    private Node buildDescriptionSection() {

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Klassenbeschreibung...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(120);
        descriptionArea.setMaxWidth(Double.MAX_VALUE);
        descriptionArea.textProperty().addListener((obs, ov, nv) -> {
            if (selectedClass != null) {
                selectedClass.description = nv;
                logger.fine("Beschreibung gesetzt: " + selectedClass.getName());
            }
        });

        return buildSection("Description", descriptionArea);
    }

    // --- Key Ability ---
    private Node buildKeyAbilitySection() {
        GridPane options = new GridPane();
        options.setHgap(12);
        options.setVgap(4);

        AbilityScores[] abilities = AbilityScores.values();
        for (int i = 0; i < abilities.length; i++) {
            CheckBox cb = new CheckBox(abilities[i].toString());
            abilityButtons.put(abilities[i], cb);
            int finalI = i;
            cb.setOnAction(e -> {
                if (selectedClass == null) return;
                if (cb.isSelected()) {
                    selectedClass.keyAbilities.add(abilities[finalI]);
                    logger.fine("Key Ability hinzugefügt: " + abilities[finalI].fullName());
                } else {
                    selectedClass.keyAbilities.remove(abilities[finalI]);
                    logger.fine("Key Ability entfernt: " + abilities[finalI].fullName());
                }
            });
            options.add(cb, i % 3, i / 3);
        }
        Label hpLabel = new Label("HP per Level");
        hpPerLevelSpinner = new Spinner<>(1, 99, 1);
        hpPerLevelSpinner.setEditable(true);
        hpPerLevelSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedClass != null) {
                selectedClass.hpPerLevel = nv;
                logger.fine("HP per Level gesetzt: " + nv);
            }
        });

        HBox hpRow = new HBox(8, hpLabel, hpPerLevelSpinner);

        return buildSection("Key Ability", options, new Separator(), hpRow);
    }

    // --- Proficincies ---
    private Node buildProficienciesSection() {
        // Saving Throws
        GridPane saveGrid = new GridPane();
        saveGrid.setHgap(12);
        saveGrid.setVgap(4);

        SavingThrows[] saves = SavingThrows.values();
        for (int i = 0; i < saves.length; i++) {
            SavingThrows save = saves[i];
            Label label = new Label(save.getName() + " (" + save.getScore().name() + ")");
            ComboBox<Expertise> box = makeExpertiseBox(Expertise.T);
            box.setOnAction(e -> {
                if (selectedClass != null) {
                    selectedClass.savingThrows.put(save, box.getValue());
                    logger.fine("Save gesetzt: " + save.getName() + " → " + box.getValue().fullName());
                }
            });
            saveBoxes.put(save, box);
            saveGrid.add(label, 0, i);
            saveGrid.add(box, 1, i);
        }

        // Armor
        GridPane armorGrid = new GridPane();
        armorGrid.setHgap(12);
        armorGrid.setVgap(4);

        int row = 0;
        for (ArmorCategory cat : ArmorCategory.values()) {
            ComboBox<Expertise> box = makeExpertiseBox(Expertise.U);
            box.setOnAction(e -> {
                if (selectedClass != null) {
                    selectedClass.armorProficiencies.put(cat, box.getValue());
                    logger.fine("Armor gesetzt: " + cat.name() + " → " + box.getValue().fullName());
                }
            });
            armorBoxes.put(cat, box);
            armorGrid.add(new Label(cat.name()), 0, row);
            armorGrid.add(box, 1, row++);
        }

        // Weapons
        GridPane weaponGrid = new GridPane();
        weaponGrid.setHgap(12);
        weaponGrid.setVgap(4);

        row = 0;
        for (WeaponCategory cat : WeaponCategory.values()) {
            ComboBox<Expertise> box = makeExpertiseBox(Expertise.U);
            box.setOnAction(e -> {
                if (selectedClass != null) {
                    selectedClass.weaponProficiencies.put(cat, box.getValue());
                    logger.fine("Weapon gesetzt: " + cat.name() + " → " + box.getValue().fullName());
                }
            });
            weaponBoxes.put(cat, box);
            weaponGrid.add(new Label(cat.name()), 0, row);
            weaponGrid.add(box, 1, row++);
        }

        VBox saveCol   = new VBox(4, new Label("Saving Throws"), new Separator(), saveGrid);
        VBox armorCol  = new VBox(4, new Label("Armor"),         new Separator(), armorGrid);
        VBox weaponCol = new VBox(4, new Label("Weapons"),       new Separator(), weaponGrid);
        HBox.setHgrow(saveCol,   Priority.ALWAYS);
        HBox.setHgrow(armorCol,  Priority.ALWAYS);
        HBox.setHgrow(weaponCol, Priority.ALWAYS);

        HBox columns = new HBox(24, saveCol, new Separator(), armorCol, new Separator(), weaponCol);

        return buildSection("Proficiencies at level 1", columns);
    }

    private ComboBox<Expertise> makeExpertiseBox(Expertise min) {
        ComboBox<Expertise> box = new ComboBox<>();
        // Nur Werte ab Minimum
        for (Expertise e : Expertise.values()) {
            if (e.ordinal() >= min.ordinal()) box.getItems().add(e);
        }
        box.setValue(min);
        return box;
    }


    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew());

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox btnRow = new HBox(4, newBtn, deleteBtn);

        classListView = new ListView<>(classes);
        classListView.setOnMouseClicked(e -> {
            ClassBuild gc = classListView.getSelectionModel().getSelectedItem();
            if (gc != null) loadClass(gc);
        });
        VBox.setVgrow(classListView, Priority.ALWAYS);

        VBox panel = new VBox(6, btnRow, classListView);
        panel.setPadding(new Insets(8));
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxHeight(Double.MAX_VALUE);

        return panel;
    }

    private void refreshList() {
        ClassBuild current = selectedClass;
        List<ClassBuild> sorted = new ArrayList<>(classes);
        sorted.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        classes.setAll(sorted);
        // Selektion wiederherstellen
        if (current != null) classListView.getSelectionModel().select(current);
        logger.fine("Klassenliste aktualisiert: " + classes.size());
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    // --- Neue Klasse anlegen ---
    private void createNew() {
        ClassBuild gc = new ClassBuild("Neue Klasse");
        classes.add(gc);
        classListView.getSelectionModel().select(gc);
        loadClass(gc);
        logger.info("Neue Klasse angelegt");
    }

    // --- Löschen ---
    private void deleteSelected() {
        if (selectedClass == null) return;
        classes.remove(selectedClass);
        selectedClass = null;
        saveToDisk();
        logger.info("Klasse gelöscht");
    }

    // --- Klasse in Formular laden ---
    private void loadClass(ClassBuild gc) {
        selectedClass = gc;
        getClassNameField().setText(gc.name);
        descriptionArea.setText(gc.description);

        abilityButtons.forEach((ability, cb) ->
                cb.setSelected(gc.keyAbilities.contains(ability)));

        saveBoxes.forEach((save, box) -> {
            Expertise val = gc.savingThrows.getOrDefault(save, Expertise.T);
            box.setValue(val);
        });

        hpPerLevelSpinner.getValueFactory().setValue(gc.hpPerLevel);

        armorBoxes.forEach((cat, box) ->
                box.setValue(gc.armorProficiencies.getOrDefault(cat, Expertise.U)));
        weaponBoxes.forEach((cat, box) ->
                box.setValue(gc.weaponProficiencies.getOrDefault(cat, Expertise.U)));

        armorBoxes.forEach((cat, box) -> gc.armorProficiencies.put(cat, box.getValue()));
        weaponBoxes.forEach((cat, box) -> gc.weaponProficiencies.put(cat, box.getValue()));


        logger.fine("Klasse geladen: " + gc.getName());
    }

    // --- Disk ---
    private void saveToDisk() {
        try {
            EditorIO.save(FILE, new ArrayList<>(classes));
            refreshList();
            logger.info("Klassen gespeichert: " + classes.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        List<ClassBuild> loaded = EditorIO.load(FILE,
                new TypeReference<>() {
                }, new ArrayList<>());
        classes.setAll(loaded);
        if (loaded.isEmpty()) {
            ClassBuild neu = new ClassBuild("Neue Klasse");
            loaded.add(neu);
            logger.info("Keine Klassen gefunden — leere Klasse erstellt");
        }

        classes.setAll(loaded);
        loadClass(classes.getFirst());
        logger.info("Klassen geladen: " + classes.size());
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

    public static List<String> loadAllNames() {
        List<ClassBuild> loaded = EditorIO.load(FILE, new TypeReference<>() {}, new ArrayList<>());
        return loaded.stream().map(c -> c.name).sorted().toList();
    }
    public static List<ClassBuild> loadAll() {
        return EditorIO.load(FILE, new TypeReference<>() {}, new ArrayList<>());
    }
}
