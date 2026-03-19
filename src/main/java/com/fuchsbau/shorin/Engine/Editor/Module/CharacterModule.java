package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fuchsbau.shorin.Engine.System.PlayerCharacter;
import com.fuchsbau.shorin.Engine.System.Proficiency;
import com.fuchsbau.shorin.Engine.System.SlotType;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.logging.Logger;

public class CharacterModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();

    private PlayerCharacter character = new PlayerCharacter();

    // UI-Refs
    private TextField nameField, levelField, classField, ancestryField;
    private Label activeLevelLabel;
    private VBox featPanel;       // rechts unten — dynamisch
    private VBox tabContextPanel; // rechts mitte — dynamisch

    // ────────────────────────────────────────────────────────────
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
        classField = new TextField();
        classField.setPromptText("Klasse");
        ancestryField = new TextField();
        ancestryField.setPromptText("Ancestry");

        HBox header = new HBox(10,
                makeLabel("Name"), nameField,
                makeLabel("Level"), levelField,
                makeLabel("Klasse"), classField,
                makeLabel("Ancestry"), ancestryField
        );
        header.setPadding(new Insets(8));
        header.setStyle("-fx-background-color: rgba(20,20,30,0.95);");
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

        String[] stats = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < stats.length; i++) {
            grid.add(makeLabel(stats[i]), 0, i);
            TextField f = new TextField("10");
            f.setPrefWidth(50);
            grid.add(f, 1, i);
        }
        return grid;
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
    private Node buildSkillsPanel() {
        VBox box = new VBox(3);
        box.setPadding(new Insets(8));

        // PF2e Standardskills
        String[] skills = {
                "Acrobatics", "Arcana", "Athletics", "Crafting", "Deception",
                "Diplomacy", "Intimidation", "Lore", "Medicine", "Nature",
                "Occultism", "Performance", "Religion", "Society", "Stealth",
                "Survival", "Thievery"
        };

        for (String skill : skills) {
            ComboBox<Proficiency> prof = new ComboBox<>();
            prof.getItems().addAll(Proficiency.values());
            prof.setValue(Proficiency.UNTRAINED);
            prof.setPrefWidth(35);

            // Proficiency als Kurzzeichen anzeigen
            prof.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Proficiency p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.shortLabel());
                }
            });
            prof.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Proficiency p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.shortLabel());
                }
            });

            TextField bonus = new TextField("+0");
            bonus.setPrefWidth(40);
            bonus.setEditable(false);

            HBox row = new HBox(4, makeSmallLabel(skill), prof, bonus);
            HBox.setHgrow(makeSmallLabel(skill), Priority.ALWAYS);
            box.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        return scroll;
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
        l.setTextFill(Color.LIGHTGRAY);
        return l;
    }

    private Label makeSmallLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.GRAY);
        l.setStyle("-fx-font-size: 11px;");
        return l;
    }

    // --- Lifecycle ---
    @Override
    public void onActivate() { /* laden */ }

    @Override
    public void onDeactivate() { /* speichern */ }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        return null;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }
}