package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.System.Character.PlayerCharacter;
import com.fuchsbau.shorin.Engine.System.Misc.Proficiency;
import com.fuchsbau.shorin.Engine.System.SlotType;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CharacterModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();


    // UI-Refs
    private TextField nameField, levelField;
    private ComboBox<String> classDropdown, ancestryDropdown;
    private Label activeLevelLabel;
    private VBox featPanel;
    private VBox tabContextPanel;

    private final ObservableList<String> charNames = FXCollections.observableArrayList();

    private PlayerCharacter character = new PlayerCharacter();

    // Stats panel
    private final IntegerProperty strMod = new SimpleIntegerProperty(0);
    private final IntegerProperty dexMod = new SimpleIntegerProperty(0);
    private final IntegerProperty conMod = new SimpleIntegerProperty(0);
    private final IntegerProperty intMod = new SimpleIntegerProperty(0);
    private final IntegerProperty wisMod = new SimpleIntegerProperty(0);
    private final IntegerProperty chaMod = new SimpleIntegerProperty(0);


    @Override
    public String getTitle() {
        return "Charakter";
    }

    @Override
    public Node buildContent() {
        BorderPane root = new BorderPane();

        // --- Bereiche ---
        root.setTop(buildHeader());

        // Stats + Combat + Skills über die gesamte Breite
        root.setCenter(buildMainArea());

        return root;
    }

    // --- HEADER ---
    private Node buildHeader() {
        nameField = new TextField();
        nameField.setPromptText("Name");
        levelField = new TextField("1");
        levelField.setPrefWidth(45);

        classDropdown = new ComboBox<>();
        classDropdown.setPromptText("Klasse");
        classDropdown.getItems().addAll("Fighter", "Rogue", "Wizard", "Cleric"); // später aus ClassModule
        classDropdown.setPrefWidth(120);

        ancestryDropdown = new ComboBox<>();
        ancestryDropdown.setPromptText("Ancestry");
        ancestryDropdown.getItems().addAll("Human", "Elf", "Dwarf", "Gnome"); // später aus AncestryModule
        ancestryDropdown.setPrefWidth(120);

        HBox header = new HBox(10,
                makeLabel("Name"), nameField,
                makeLabel("Level"), levelField,
                makeLabel("Klasse"), classDropdown,
                makeLabel("Ancestry"), ancestryDropdown
        );
        header.setPadding(new Insets(8));
        return header;
    }

    // --- HAUPTBEREICH ---
    private Node buildMainArea() {
        BorderPane area = new BorderPane();

        // Obere Hälfte: Stats | Combat | Skills
        HBox statsRow = new HBox(0,
                buildStatsPanel(),
                new Separator(),
                buildCombatPanel(),
                new Separator(),
                buildSkillsPanel()
        );
        HBox.setHgrow(buildSkillsPanel(), Priority.ALWAYS);

        // Mitte: Tabs + Kontext
        Node tabArea = buildTabArea();

        // Untere Hälfte: Level-Scroll + Feat-Panel
        Node levelArea = buildLevelArea();

        VBox center = new VBox(0, statsRow, new Separator(), tabArea, new Separator(), levelArea);
        VBox.setVgrow(tabArea, Priority.SOMETIMES);
        VBox.setVgrow(levelArea, Priority.ALWAYS);

        area.setCenter(center);
        return area;
    }

    // --- STATS ---
    private Node buildStatsPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(4);
        grid.setPadding(new Insets(8));

        // Stat + Property → Label zeigt berechneten Modifier
        addStatRow(grid, "STR", strMod, 0);
        addStatRow(grid, "DEX", dexMod, 1);
        addStatRow(grid, "CON", conMod, 2);
        addStatRow(grid, "INT", intMod, 3);
        addStatRow(grid, "WIS", wisMod, 4);
        addStatRow(grid, "CHA", chaMod, 5);

        return grid;
    }

    private void addStatRow(GridPane grid, String name, IntegerProperty mod, int row) {
        grid.add(makeLabel(name), 0, row);

        // Modifier-Label
        Label modLabel = new Label("+0");
        modLabel.setPrefWidth(35);
        modLabel.setTextFill(Color.BLACK);
        modLabel.setStyle("-fx-font-weight: bold;");
        mod.addListener((obs, ov, nv) -> {
            modLabel.setText((nv.intValue() >= 0 ? "+" : "") + nv.intValue());
            recalculateSkills();
        });

        grid.add(modLabel, 1, row);
    }

    // --- COMBAT ---
    private Node buildCombatPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(4);
        grid.setPadding(new Insets(8));

        addCombatRow(grid, "AC", 0);
        addCombatRow(grid, "HP", 1);
        addCombatRow(grid, "Speed", 2);

        grid.add(new Separator(), 0, 3, 2, 1);

        addCombatRow(grid, "Fort", 4);
        addCombatRow(grid, "Ref", 5);
        addCombatRow(grid, "Will", 6);

        return grid;
    }

    private void addCombatRow(GridPane grid, String label, int row) {
        grid.add(makeLabel(label), 0, row);
        TextField f = new TextField("0");
        f.setPrefWidth(50);
        grid.add(f, 1, row);
    }

    // --- SKILLS ---
    private final Map<String, ObjectProperty<Proficiency>> skillProfs = new LinkedHashMap<>();
    private final Map<String, Label> skillBonusLabels = new LinkedHashMap<>();

    private static final String[] SKILLS = {
            "Acrobatics", "Arcana", "Athletics", "Crafting", "Deception",
            "Diplomacy", "Intimidation", "Lore", "Medicine", "Nature",
            "Occultism", "Performance", "Religion", "Society", "Stealth",
            "Survival", "Thievery"
    };

    // Welcher Stat gehört zu welchem Skill
    private static IntegerProperty skillStat(String skill,
                                             IntegerProperty str, IntegerProperty dex, IntegerProperty con,
                                             IntegerProperty intel, IntegerProperty wis, IntegerProperty cha) {
        return switch (skill) {
            case "Athletics" -> str;
            case "Acrobatics", "Stealth", "Thievery" -> dex;
            case "Arcana", "Crafting", "Lore", "Occultism", "Society" -> intel;
            case "Medicine", "Nature", "Perception", "Religion", "Survival" -> wis;
            case "Deception", "Diplomacy", "Intimidation", "Performance" -> cha;
            default -> wis;
        };
    }

    private Node buildSkillsPanel() {
        VBox box = new VBox(3);
        box.setPadding(new Insets(8));

        for (String skill : SKILLS) {
            ObjectProperty<Proficiency> prof = new SimpleObjectProperty<>(Proficiency.UNTRAINED);
            skillProfs.put(skill, prof);

            // Feste Breiten für alle Spalten
            Label nameLabel = makeLabel(skill);
            nameLabel.setPrefWidth(100);
            nameLabel.setMinWidth(100);
            nameLabel.setMaxWidth(100);

            ComboBox<Proficiency> profBox = new ComboBox<>();
            profBox.getItems().addAll(Proficiency.values());
            profBox.setValue(Proficiency.UNTRAINED);
            profBox.setPrefWidth(95);
            profBox.setMinWidth(95);

            // Volltext anzeigen statt "..."
            profBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Proficiency p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.name());
                }
            });
            profBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Proficiency p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.name());
                }
            });
            profBox.valueProperty().addListener((obs, ov, nv) -> {
                prof.set(nv);
                recalculateSkills();
            });

            // Bonus-Label — read-only, berechnet
            Label bonusLabel = new Label("+0");
            bonusLabel.setPrefWidth(35);
            bonusLabel.setMinWidth(35);
            bonusLabel.setTextFill(Color.BLACK);
            skillBonusLabels.put(skill, bonusLabel);

            HBox row = new HBox(4, nameLabel, profBox, bonusLabel);
            box.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        return scroll;
    }

    // Alle Skill-Boni neu berechnen — wird bei Stat- oder Proficiency-Änderung aufgerufen
    private void recalculateSkills() {
        int level = parseLevel();
        logger.fine("Skills neu berechnen — Level " + level);

        for (String skill : SKILLS) {
            Proficiency prof = skillProfs.get(skill).get();
            IntegerProperty stat = skillStat(skill, strMod, dexMod, conMod, intMod, wisMod, chaMod);

            // PF2e Formel: Stat-Mod + Level (wenn trained+) + Proficiency-Bonus
            int profBonus = switch (prof) {
                case UNTRAINED -> 0;                  // kein Level-Bonus
                case TRAINED -> level + 2;
                case EXPERT -> level + 4;
                case MASTER -> level + 6;
                case LEGENDARY -> level + 8;
            };

            int total = stat.get() + profBonus;
            Label lbl = skillBonusLabels.get(skill);
            lbl.setText((total >= 0 ? "+" : "") + total);
        }
    }

    private int parseLevel() {
        try {
            return Math.max(1, Integer.parseInt(levelField.getText().trim()));
        } catch (Exception e) {
            return 1;
        }
    }

    // --- TABS (Waffen/Rüstung/etc) ---
    private Node buildTabArea() {
        tabContextPanel = new VBox(6);
        tabContextPanel.setPadding(new Insets(8));
        tabContextPanel.setPrefWidth(220);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                makeEquipTab("Waffen", "weapon"),
                makeEquipTab("Rüstung", "armor"),
                makeEquipTab("Ausrüstung", "gear"),
                makeEquipTab("Spells", "spells"),
                makeEquipTab("Pet/Companion", "pet")
        );

        tabs.getSelectionModel().selectedItemProperty().addListener(
                (obs, ov, nv) -> updateTabContext(nv.getText()));

        SplitPane split = new SplitPane(tabs, new ScrollPane(tabContextPanel));
        split.setDividerPositions(0.70);
        split.setPrefHeight(220);
        return split;
    }

    private Tab makeEquipTab(String title, String type) {
        Tab tab = new Tab(title);
        VBox content = new VBox(6);
        content.setPadding(new Insets(8));
        content.getChildren().add(makeSmallLabel("(leer)"));
        tab.setContent(content);
        return tab;
    }

    private void updateTabContext(String tabName) {
        tabContextPanel.getChildren().clear();
        tabContextPanel.getChildren().add(makeLabel(tabName + " — verfügbar"));
        // Später: gefilterte Item-Liste aus ItemModule laden
    }

    // --- LEVEL BEREICH ---
    private Node buildLevelArea() {
        featPanel = new VBox(6);
        featPanel.setPadding(new Insets(8));
        featPanel.getChildren().add(makeSmallLabel("Slot auswählen..."));

        VBox levelScroll = new VBox(8);
        levelScroll.setPadding(new Insets(8));

        // Level 0 — Klasse, Background, Ancestry
        levelScroll.getChildren().add(buildLevelBlock(0, List.of(
                SlotType.CLASS,
                SlotType.BACKGROUND,
                SlotType.ANCESTRY
        )));

        // Level 1-20 — Platzhalter, später aus ClassModule befüllt
        for (int lvl = 1; lvl <= 20; lvl++) {
            levelScroll.getChildren().add(buildLevelBlock(lvl, List.of(
                    SlotType.CLASS_FEAT,
                    SlotType.SKILL_FEAT
            )));
        }

        ScrollPane scroll = new ScrollPane(levelScroll);
        scroll.setFitToWidth(true);

        SplitPane split = new SplitPane(scroll, new ScrollPane(featPanel));
        split.setDividerPositions(0.55);
        SplitPane.setResizableWithParent(scroll, true);
        return split;
    }

    private Node buildLevelBlock(int level, List<SlotType> slots) {
        VBox block = new VBox(4);
        block.setPadding(new Insets(6));
        block.setStyle("-fx-border-color: #333350; -fx-border-width: 1; -fx-border-radius: 4;");

        Label lvlLabel = new Label(level == 0 ? "Charakter-Erstellung" : "Level " + level);
        lvlLabel.setTextFill(Color.ORANGE);
        lvlLabel.setStyle("-fx-font-weight: bold;");
        block.getChildren().add(lvlLabel);

        for (SlotType slot : slots) {
            Button slotBtn = new Button(slotLabel(slot) + "  —  Nicht gewählt");
            slotBtn.setMaxWidth(Double.MAX_VALUE);
            slotBtn.setOnAction(e -> showFeatPanel(slot));
            block.getChildren().add(slotBtn);
        }

        return block;
    }

    private void showFeatPanel(SlotType slot) {
        featPanel.getChildren().clear();
        featPanel.getChildren().add(makeLabel(slotLabel(slot)));
        featPanel.getChildren().add(makeSmallLabel("Verfügbare Optionen:"));
        // Später: aus FeatModule / TraitModule gefiltert laden
        featPanel.getChildren().add(makeSmallLabel("(noch nicht verbunden)"));
    }

    private String slotLabel(SlotType slot) {
        return switch (slot) {
            case CLASS_FEAT -> "Class Feat";
            case SKILL_FEAT -> "Skill Feat";
            case ANCESTRY_FEAT -> "Ancestry Feat";
            case GENERAL_FEAT -> "General Feat";
            case SKILL_INCREASE -> "Skill Increase";
            case ABILITY_BOOST -> "Ability Boost";
            case CLASS_FEATURE -> "Class Feature";
            case BACKGROUND -> "Background";
            case ANCESTRY -> "Ancestry";
            case CLASS -> "Klasse";
        };
    }

    // --- Helpers ---
    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.BLACK);
        return l;
    }

    private Label makeSmallLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.BLACK);
        l.setStyle("-fx-font-size: 11px;");
        return l;
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        TextField search = new TextField();
        search.setPromptText("Charakter suchen...");

        FilteredList<String> filtered = new FilteredList<>(charNames, s -> true);
        search.textProperty().addListener((obs, ov, nv) ->
                filtered.setPredicate(s ->
                        nv == null || nv.isBlank() ||
                                s.toLowerCase().contains(nv.toLowerCase())));

        ListView<String> listView = new ListView<>(filtered);
        listView.setPrefHeight(200);
        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) loadCharacter(selected);
        });

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNewCharacter(listView));

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            deleteCharacter(selected);
        });

        VBox box = new VBox(6, search, listView, newBtn, deleteBtn);
        box.setPadding(new Insets(8));
        VBox.setVgrow(listView, Priority.ALWAYS);
        return box;
    }

    private void loadCharNames() {
        charNames.clear();
        File dir = PathResolver.resolveWritable("User/Charakters").toFile();
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Charakterverzeichnis erstellt: " + dir.getAbsolutePath());
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;
        for (File f : files) {
            // Dateiname ohne .json
            charNames.add(f.getName().replace(".json", ""));
        }
        charNames.sort(String::compareToIgnoreCase);
        logger.info("Charaktere geladen: " + charNames.size());
    }

    private void loadCharacter(String name) {
        character = EditorIO.load(
                "User/Charakters/" + name + ".json",
                new TypeReference<>() {
                },
                new PlayerCharacter());
        character.name = name;
        // Formular befüllen
        nameField.setText(character.name);
        logger.info("Charakter geladen: " + name);
    }

    private void createNewCharacter(ListView<String> listView) {
        character = new PlayerCharacter();
        character.name = "Neuer Charakter";
        nameField.setText(character.name);
        saveCharacter();
        charNames.add(character.name);
        charNames.sort(String::compareToIgnoreCase);
        listView.getSelectionModel().select(character.name);
        logger.fine("Neuer Charakter erstellt");
    }

    private void saveCharacter() {
        if (character == null || character.name == null || character.name.isBlank()) return;
        try {
            EditorIO.save("User/Charakters/" + character.name + ".json", character);
        } catch (Exception e) {
            logger.severe("SaveCharacter Failed im CharacterModule");
            logger.severe(e.getMessage());
        }
        logger.info("Charakter gespeichert: " + character.name);
    }

    private void deleteCharacter(String name) {
        File f = PathResolver.resolveWritable("User/Charakters/" + name + ".json").toFile();
        if (f.exists() && f.delete()) {
            charNames.remove(name);
            character = new PlayerCharacter();
            nameField.clear();
            logger.info("Charakter gelöscht: " + name);
        } else {
            logger.warning("Charakter konnte nicht gelöscht werden: " + name);
        }
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    // --- Lifecycle ---
    @Override
    public void onActivate() {
        loadCharNames();
    }

    @Override
    public void onDeactivate() {
        saveCharacter();
    }
}