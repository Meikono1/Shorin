package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.Classes.ClassModule;
import com.fuchsbau.shorin.Engine.Editor.Module.Races.AncestryModule;
import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.System.Character.*;
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
import javafx.scene.text.Text;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class CharacterModule implements EditorModule {

    private static final Logger logger = FileLogger.getLogger();
    private static final String DIR = "GameConfig/User/Charakters/";

    // UI-Refs
    private TextField nameField, levelField;
    private Label classLabel;
    private Label ancestryLabel;
    private Label backgroundLabel;
    private VBox featPanel;
    private VBox tabContextPanel;
    private Button classSlotBtn;
    private Button ancestrySlotBtn;
    private Button backgroundSlotBtn;


    private final ObservableList<String> charNames = FXCollections.observableArrayList();

    private PlayerCharacter character = new PlayerCharacter();

    // Stats panel
    private final IntegerProperty acField = new SimpleIntegerProperty(0);
    private final IntegerProperty hpField = new SimpleIntegerProperty(0);
    private final IntegerProperty speedField = new SimpleIntegerProperty(0);
    private final IntegerProperty fortField = new SimpleIntegerProperty(0);
    private final IntegerProperty refField = new SimpleIntegerProperty(0);
    private final IntegerProperty willField = new SimpleIntegerProperty(0);
    private final IntegerProperty strMod = new SimpleIntegerProperty(0);
    private final IntegerProperty dexMod = new SimpleIntegerProperty(0);
    private final IntegerProperty conMod = new SimpleIntegerProperty(0);
    private final IntegerProperty intMod = new SimpleIntegerProperty(0);
    private final IntegerProperty wisMod = new SimpleIntegerProperty(0);
    private final IntegerProperty chaMod = new SimpleIntegerProperty(0);

    private AbilityBoostPanel abilityBoostPanel = null;

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
        nameField.textProperty().addListener((obs, ov, nv) -> {
            if (character != null) character.name = nv.trim();
        });

        levelField = new TextField("1");
        levelField.textProperty().addListener((observableValue, s, t1) -> {
            if (character != null) character.setLevel(t1);
            refreshOverview();
        });
        levelField.setPrefWidth(45);

        classLabel = new Label("–");
        ancestryLabel = new Label("–");
        backgroundLabel = new Label("–");

        HBox header = new HBox(10,
                makeLabel("Name"), nameField,
                makeLabel("Level"), levelField,
                makeLabel("Klasse"), classLabel,
                makeLabel("Ancestry"), ancestryLabel,
                makeLabel("Background"), backgroundLabel
        );
        header.setPadding(new Insets(8));
        return header;
    }

    private Node buildMainArea() {
        BorderPane area = new BorderPane();

        HBox statsRow = new HBox(0,
                buildStatsPanel(),
                new Separator(),
                buildCombatPanel(),
                new Separator(),
                buildSkillsPanel()
        );

        // Level-Bereich lebt jetzt IM Tab — nicht mehr separat
        Node tabArea = buildTabArea();

        VBox center = new VBox(0, statsRow, new Separator(), tabArea);
        area.setCenter(center);
        VBox.setVgrow(tabArea, Priority.ALWAYS);
        VBox.setVgrow(statsRow, Priority.SOMETIMES);

        center.heightProperty().addListener((obs, ov, nv) -> {
            double h = nv.doubleValue() * 0.25;
            statsRow.setMinHeight(h);
            statsRow.setMaxHeight(h);
        });

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

        addStatRow(grid, "AC", acField, 0);
        addStatRow(grid, "HP", hpField, 1);
        addStatRow(grid, "Speed", speedField, 2);

        grid.add(new Separator(), 0, 3, 2, 1);

        addStatRow(grid, "Fort", fortField, 4);
        addStatRow(grid, "Ref", refField, 5);
        addStatRow(grid, "Will", willField, 6);

        return grid;
    }

    // --- SKILLS ---
    private final Map<Skill, ObjectProperty<Proficiency>> skillProfs = new LinkedHashMap<>();
    private final Map<String, Label> skillBonusLabels = new LinkedHashMap<>();


    private Node buildSkillsPanel() {
        VBox box = new VBox(3);
        box.setPadding(new Insets(8));

        for (Skill skill : Skill.values()) {
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
            skillBonusLabels.put(skill.displayName(), bonusLabel);

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

        for (Skill skill : Skill.values()) {
            int value = character.getSkillValue(skill);

            Label lbl = skillBonusLabels.get(skill.displayName());

            lbl.setText((value >= 0 ? "+" : "") + value);
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
        tabContextPanel.setMaxWidth(Double.MAX_VALUE);
        tabContextPanel.setMinWidth(0);

        HBox.setHgrow(tabContextPanel, Priority.ALWAYS);
        featPanel = tabContextPanel;

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                makeCreationTab(),
                makeEquipTab("Waffen", "weapon"),
                makeEquipTab("Rüstung", "armor"),
                makeEquipTab("Ausrüstung", "gear"),
                makeEquipTab("Spells", "spells"),
                makeEquipTab("Pet/Companion", "pet")
        );

        tabs.getSelectionModel().selectedItemProperty().addListener(
                (obs, ov, nv) -> {
                    if (nv != null) updateTabContext(nv.getUserData());
                });

        tabs.getSelectionModel().select(0);

        ScrollPane contextScroll = new ScrollPane(tabContextPanel);
        contextScroll.setFitToWidth(true);
        contextScroll.setFitToHeight(true);
        contextScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        SplitPane split = new SplitPane(tabs, contextScroll);
        split.setDividerPositions(0.65);
        VBox.setVgrow(split, Priority.ALWAYS);
        return split;
    }

    private Tab makeCreationTab() {
        Tab tab = new Tab("Erstellung");
        tab.setUserData("erstellung");

        VBox levelScroll = new VBox(8);
        levelScroll.setPadding(new Insets(8));

        levelScroll.getChildren().add(buildLevelBlock(0, List.of(
                SlotType.CLASS,
                SlotType.BACKGROUND,
                SlotType.ANCESTRY,
                SlotType.ABILITY_BOOST
        )));

        for (int lvl = 1; lvl <= 20; lvl++) {
            levelScroll.getChildren().add(buildLevelBlock(lvl, List.of(
                    SlotType.CLASS_FEAT,
                    SlotType.SKILL_FEAT
            )));
        }

        ScrollPane scroll = new ScrollPane(levelScroll);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tab.setContent(scroll);

        return tab;
    }

    private Tab makeEquipTab(String title, String type) {
        Tab tab = new Tab(title);
        tab.setUserData(type);
        VBox content = new VBox(6);
        content.setPadding(new Insets(8));
        content.getChildren().add(makeSmallLabel("(leer)"));
        tab.setContent(content);
        return tab;
    }

    private void updateTabContext(Object userData) {
        if ("erstellung".equals(userData)) return;
        tabContextPanel.getChildren().clear();
        String name = userData != null ? userData.toString() : "";
        tabContextPanel.getChildren().add(makeLabel(name.substring(0, 1).toUpperCase()
                + name.substring(1) + " — verfügbar"));
        tabContextPanel.getChildren().add(makeSmallLabel("(noch nicht verbunden)"));
        logger.fine("TabContext: " + name);
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

            if (level == 0) {
                switch (slot) {
                    case CLASS -> classSlotBtn = slotBtn;
                    case ANCESTRY -> ancestrySlotBtn = slotBtn;
                    case BACKGROUND -> backgroundSlotBtn = slotBtn;
                }
            }

            block.getChildren().add(slotBtn);
        }

        return block;
    }

    private void showFeatPanel(SlotType slot) {
        tabContextPanel.getChildren().clear();
        tabContextPanel.getChildren().add(makeLabel(slotLabel(slot)));

        switch (slot) {
            case CLASS -> {
                List<ClassBuild> allClasses = ClassModule.loadAll();
                ObservableList<ClassBuild> classItems = FXCollections.observableArrayList(allClasses);
                FilteredList<ClassBuild> filtered = new FilteredList<>(classItems, c -> true);

                // Suche — nur über der Liste
                TextField search = new TextField();
                search.setPromptText("Klasse suchen...");
                search.textProperty().addListener((obs, ov, nv) ->
                        filtered.setPredicate(c -> nv == null || nv.isBlank()
                                || c.name.toLowerCase().contains(nv.toLowerCase())));

                // Liste — Breite: breitester Eintrag + 20px, nach Layout berechnet
                ListView<ClassBuild> list = new ListView<>(filtered);
                list.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(ClassBuild c, boolean empty) {
                        super.updateItem(c, empty);
                        setText(empty || c == null ? null : c.name);
                    }
                });

                double listWidth = calcListWidth(allClasses.stream().map(c -> c.name).toList());
                list.setPrefWidth(listWidth);
                list.setMinWidth(listWidth);
                list.setMaxWidth(listWidth);
                VBox.setVgrow(list, Priority.ALWAYS);

                // Liste + Suche als linke Spalte
                VBox leftCol = new VBox(4, search, list);
                leftCol.setPrefWidth(listWidth);
                leftCol.setMinWidth(listWidth);
                leftCol.setMaxWidth(listWidth);

                // Detail — ScrollPane nimmt den Rest
                VBox detail = new VBox(6);
                detail.setPadding(new Insets(8));
                ScrollPane detailScroll = new ScrollPane(detail);
                detailScroll.setFitToWidth(true);
                detailScroll.setFitToHeight(true);
                detailScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                detailScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                detail.getChildren().add(makeSmallLabel("Klasse auswählen..."));

                // Einfachklick → Detail
                list.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                    if (nv == null) return;
                    updateClassDetail(detail, nv);
                    logger.fine("Klasse Vorschau: " + nv.name);
                });

                // Doppelklick → übernehmen
                list.setOnMouseClicked(e -> {
                    if (e.getClickCount() != 2) return;
                    ClassBuild sel = list.getSelectionModel().getSelectedItem();
                    if (sel == null || character == null) return;
                    character.className = sel.name;
                    classLabel.setText(sel.name);
                    character.setClass(sel);
                    refreshOverview();
                    logger.info("Klasse übernommen: " + sel.name);
                });

                // vorselektieren
                if (character != null && !character.className.isBlank()) {
                    allClasses.stream()
                            .filter(c -> c.name.equals(character.className))
                            .findFirst()
                            .ifPresent(c -> {
                                list.getSelectionModel().select(c);
                                list.scrollTo(c);
                            });
                }

                // BorderPane: links fixe Liste, Mitte Detail
                BorderPane layout = new BorderPane();
                layout.setLeft(leftCol);
                layout.setCenter(detailScroll);
                BorderPane.setMargin(leftCol, new Insets(0, 6, 0, 0));

                // tabContextPanel auf volle Größe bringen
                tabContextPanel.getChildren().clear();
                tabContextPanel.setPrefWidth(Double.MAX_VALUE);
                tabContextPanel.setMaxWidth(Double.MAX_VALUE);
                VBox.setVgrow(layout, Priority.ALWAYS);
                tabContextPanel.getChildren().add(layout);

                // Label oben drüber — getrennt damit es nicht im Layout verschwindet
                logger.fine("Klassen-Panel: " + allClasses.size() + " geladen");
            }
            case ANCESTRY -> {
                List<Ancestrie> all = AncestryModule.loadAll();
                ObservableList<Ancestrie> items = FXCollections.observableArrayList(all);
                FilteredList<Ancestrie> filtered = new FilteredList<>(items, a -> true);

                TextField search = new TextField();
                search.setPromptText("Ancestry suchen...");
                search.textProperty().addListener((obs, ov, nv) ->
                        filtered.setPredicate(a -> nv == null || nv.isBlank()
                                || a.name.toLowerCase().contains(nv.toLowerCase())));

                ListView<Ancestrie> list = new ListView<>(filtered);
                list.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(Ancestrie a, boolean empty) {
                        super.updateItem(a, empty);
                        setText(empty || a == null ? null : a.name);
                    }
                });
                double listWidth = calcListWidth(all.stream().map(a -> a.name).toList());
                list.setPrefWidth(listWidth);
                list.setMinWidth(listWidth);
                list.setMaxWidth(listWidth);
                VBox.setVgrow(list, Priority.ALWAYS);

                VBox leftCol = new VBox(4, search, list);
                leftCol.setPrefWidth(listWidth);
                leftCol.setMinWidth(listWidth);
                leftCol.setMaxWidth(listWidth);

                VBox detail = new VBox(6);
                detail.setPadding(new Insets(8));
                ScrollPane detailScroll = new ScrollPane(detail);
                detailScroll.setFitToWidth(true);
                detailScroll.setFitToHeight(true);
                detailScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                detail.getChildren().add(makeSmallLabel("Ancestry auswählen..."));

                list.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                    if (nv == null) return;
                    updateAncestryDetail(detail, nv);
                    logger.fine("Ancestry Vorschau: " + nv.name);
                });

                list.setOnMouseClicked(e -> {
                    if (e.getClickCount() != 2) return;
                    Ancestrie sel = list.getSelectionModel().getSelectedItem();
                    if (sel == null || character == null) return;
                    character.setAncestrie(sel);
                    refreshOverview();
                    logger.info("Ancestry übernommen: " + sel.name);
                });

                if (character != null && !character.ancestry.isBlank()) {
                    all.stream().filter(a -> a.name.equals(character.ancestry)).findFirst()
                            .ifPresent(a -> {
                                list.getSelectionModel().select(a);
                                list.scrollTo(a);
                            });
                }

                BorderPane layout = new BorderPane();
                layout.setLeft(leftCol);
                layout.setCenter(detailScroll);
                BorderPane.setMargin(leftCol, new Insets(0, 6, 0, 0));

                tabContextPanel.getChildren().clear();
                tabContextPanel.setMaxWidth(Double.MAX_VALUE);
                VBox.setVgrow(layout, Priority.ALWAYS);
                tabContextPanel.getChildren().add(layout);
                logger.fine("Ancestry-Panel: " + all.size() + " geladen");
            }
            case BACKGROUND -> {
                List<PlayerBackground> all = BackgroundModule.loadAll();
                ObservableList<PlayerBackground> items = FXCollections.observableArrayList(all);
                FilteredList<PlayerBackground> filtered = new FilteredList<>(items, b -> true);

                TextField search = new TextField();
                search.setPromptText("Background suchen...");
                search.textProperty().addListener((obs, ov, nv) ->
                        filtered.setPredicate(b -> nv == null || nv.isBlank()
                                || b.name.toLowerCase().contains(nv.toLowerCase())));

                ListView<PlayerBackground> list = new ListView<>(filtered);
                list.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(PlayerBackground b, boolean empty) {
                        super.updateItem(b, empty);
                        setText(empty || b == null ? null : b.name);
                    }
                });
                double listWidth = calcListWidth(all.stream().map(b -> b.name).toList());
                list.setPrefWidth(listWidth);
                list.setMinWidth(listWidth);
                list.setMaxWidth(listWidth);
                VBox.setVgrow(list, Priority.ALWAYS);

                VBox leftCol = new VBox(4, search, list);
                leftCol.setPrefWidth(listWidth);
                leftCol.setMinWidth(listWidth);
                leftCol.setMaxWidth(listWidth);

                VBox detail = new VBox(6);
                detail.setPadding(new Insets(8));
                ScrollPane detailScroll = new ScrollPane(detail);
                detailScroll.setFitToWidth(true);
                detailScroll.setFitToHeight(true);
                detailScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                detail.getChildren().add(makeSmallLabel("Background auswählen..."));

                list.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                    if (nv == null) return;
                    updateBackgroundDetail(detail, nv);
                    logger.fine("Background Vorschau: " + nv.name);
                });

                list.setOnMouseClicked(e -> {
                    if (e.getClickCount() != 2) return;
                    PlayerBackground sel = list.getSelectionModel().getSelectedItem();
                    if (sel == null || character == null) return;
                    character.background = sel.name;
                    backgroundLabel.setText(sel.name);
                    refreshLevelBlock(0);
                    logger.info("Background übernommen: " + sel.name);
                });

                if (character != null && !character.background.isBlank()) {
                    all.stream().filter(b -> b.name.equals(character.background)).findFirst()
                            .ifPresent(b -> {
                                list.getSelectionModel().select(b);
                                list.scrollTo(b);
                            });
                }

                BorderPane layout = new BorderPane();
                layout.setLeft(leftCol);
                layout.setCenter(detailScroll);
                BorderPane.setMargin(leftCol, new Insets(0, 6, 0, 0));

                tabContextPanel.getChildren().clear();
                tabContextPanel.setMaxWidth(Double.MAX_VALUE);
                VBox.setVgrow(layout, Priority.ALWAYS);
                tabContextPanel.getChildren().add(layout);
                logger.fine("Background-Panel: " + all.size() + " geladen");
            }
            case ABILITY_BOOST -> {
                if (character.className.isBlank() || character.ancestry.isBlank() || character.background.isBlank()) {
                    tabContextPanel.getChildren().add(makeSmallLabel(
                            "⚠ Klasse, Ancestry und Background müssen zuerst gewählt werden."));
                    logger.fine("AbilityBoost: nicht alle Voraussetzungen erfüllt");
                    return;
                }

                Ancestrie anc = AncestryModule.loadAll().stream()
                        .filter(a -> a.name.equals(character.ancestry)).findFirst().orElse(null);
                PlayerBackground bg = BackgroundModule.loadAll().stream()
                        .filter(b -> b.name.equals(character.background)).findFirst().orElse(null);
                ClassBuild cls = ClassModule.loadAll().stream()
                        .filter(c -> c.name.equals(character.className)).findFirst().orElse(null);

                if (anc == null || bg == null || cls == null) {
                    tabContextPanel.getChildren().add(makeSmallLabel("Daten konnten nicht geladen werden."));
                    return;
                }

                abilityBoostPanel = new AbilityBoostPanel(anc, bg, cls, character, this::refreshOverview);
                VBox.setVgrow(abilityBoostPanel.getRoot(), Priority.ALWAYS);
                tabContextPanel.getChildren().add(abilityBoostPanel.getRoot());
                logger.fine("AbilityBoost-Panel gebaut");
            }

            default -> tabContextPanel.getChildren().add(makeSmallLabel("(noch nicht verbunden)"));
        }

        logger.fine("showFeatPanel: " + slot);
    }

    private void refreshOverview() {
        nameField.setText(character.name);
        levelField.setText(String.valueOf(character.level));

        classLabel.setText(character.className.isBlank() ? "–" : character.className);
        ancestryLabel.setText(character.ancestry.isBlank() ? "–" : character.ancestry);
        backgroundLabel.setText(character.background.isBlank() ? "–" : character.background);


        // Ich liebe JavaFX... 2 mal setzen, damit der Listener feuert und die sachen korrekt anzeigt.
        acField.set(Integer.MIN_VALUE);
        acField.set(character.ac);
        hpField.set(Integer.MIN_VALUE);
        hpField.set(character.hp);
        speedField.set(Integer.MIN_VALUE);
        speedField.set(character.speed);
        fortField.set(Integer.MIN_VALUE);
        fortField.set(character.fortitude);
        refField.set(Integer.MIN_VALUE);
        refField.set(character.reflex);
        willField.set(Integer.MIN_VALUE);
        willField.set(character.will);

        strMod.set(Integer.MIN_VALUE);
        strMod.set(character.str);
        dexMod.set(Integer.MIN_VALUE);
        dexMod.set(character.dex);
        conMod.set(Integer.MIN_VALUE);
        conMod.set(character.con);
        intMod.set(Integer.MIN_VALUE);
        intMod.set(character.intel);
        wisMod.set(Integer.MIN_VALUE);
        wisMod.set(character.wis);
        chaMod.set(Integer.MIN_VALUE);
        chaMod.set(character.cha);

        refreshLevelBlock(0);
    }

    private void updateClassDetail(VBox detail, ClassBuild c) {
        detail.getChildren().clear();

        Label name = makeLabel(c.name);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        detail.getChildren().add(name);
        detail.getChildren().add(new Separator());

        // HP + Key Abilities
        String abilities = c.keyAbilities.isEmpty() ? "–" :
                c.keyAbilities.stream().map(e -> e.abilityScore.name())
                        .reduce((a, b) -> a + ", " + b).orElse("–");
        detail.getChildren().add(makeSmallLabel("HP/Level: " + c.hpPerLevel));
        detail.getChildren().add(makeSmallLabel("Key Ability: " + abilities));

        // Saving Throws
        if (!c.savingThrows.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Saving Throws:"));
            c.savingThrows.forEach((save, exp) ->
                    detail.getChildren().add(makeSmallLabel("  " + save.getName() + ": " + exp.name())));
        }

        // Armor
        List<String> armorList = new ArrayList<>();
        c.armorProficiencies.forEach((cat, exp) -> {
            if (exp != Proficiency.UNTRAINED) armorList.add(cat.name() + " (" + exp.name() + ")");
        });
        if (!armorList.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Rüstung:"));
            armorList.forEach(s -> detail.getChildren().add(makeSmallLabel("  " + s)));
        }

        // Weapons
        List<String> weaponList = new ArrayList<>();
        c.weaponProficiencies.forEach((cat, exp) -> {
            if (exp != Proficiency.UNTRAINED) weaponList.add(cat.name() + " (" + exp.name() + ")");
        });
        if (!weaponList.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Waffen:"));
            weaponList.forEach(s -> detail.getChildren().add(makeSmallLabel("  " + s)));
        }

        // Beschreibung
        if (!c.description.isBlank()) {
            detail.getChildren().add(new Separator());
            Label desc = makeSmallLabel(c.description);
            desc.setWrapText(true);
            detail.getChildren().add(desc);
        }

        logger.fine("ClassDetail: " + c.name);
    }

    private void updateAncestryDetail(VBox detail, Ancestrie a) {
        detail.getChildren().clear();

        Label name = makeLabel(a.name);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        detail.getChildren().add(name);
        detail.getChildren().add(new Separator());

        detail.getChildren().add(makeSmallLabel("HP: " + a.health));
        detail.getChildren().add(makeSmallLabel("Speed: " + a.speedFt + " ft"));
        detail.getChildren().add(makeSmallLabel("Größe: " + a.size.name()));

        if (!a.abilityBoosts.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Ability Boosts:"));
            a.abilityBoosts.forEach(b -> detail.getChildren().add(
                    makeSmallLabel("  " + b.abilityScore.name() + ": " + (b.value > 0 ? "+" : "") + b.value)));
        }
        if (a.freeBoosts > 0)
            detail.getChildren().add(makeSmallLabel("Freie Boosts: " + a.freeBoosts));

        if (!a.traits.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Traits: " + String.join(", ", a.traits)));
        }
        if (!a.languages.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Sprachen:"));
            a.languages.forEach(l -> detail.getChildren().add(makeSmallLabel("  " + l.name)));
        }
        logger.fine("AncestryDetail: " + a.name);
    }

    private void updateBackgroundDetail(VBox detail, PlayerBackground b) {
        detail.getChildren().clear();

        Label name = makeLabel(b.name);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        detail.getChildren().add(name);
        detail.getChildren().add(new Separator());

        if (!b.description.isBlank()) {
            Label desc = makeSmallLabel(b.description);
            desc.setWrapText(true);
            detail.getChildren().add(desc);
        }

        if (!b.choiceBoosts.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel(
                    "Ability Boost"));
        }
        if (b.freeBoosts > 0)
            detail.getChildren().add(makeSmallLabel("Freie Boosts: " + b.freeBoosts));

        if (!b.skills.isEmpty()) {
            detail.getChildren().add(new Separator());
            detail.getChildren().add(makeSmallLabel("Skills: " + String.join(", ", b.skills)));
        }
        if (!b.lores.isEmpty())
            detail.getChildren().add(makeSmallLabel("Lores: " + String.join(", ", b.lores)));
        if (!b.feats.isEmpty())
            detail.getChildren().add(makeSmallLabel("Feats: " + String.join(", ", b.feats)));

        logger.fine("BackgroundDetail: " + b.name);
    }

    private void refreshLevelBlock(int level) {
        if (character == null) return;
        if (level != 0) return;

        if (classSlotBtn != null)
            classSlotBtn.setText("Klasse  —  "
                    + (character.className.isBlank() ? "Nicht gewählt" : character.className));
        if (ancestrySlotBtn != null)
            ancestrySlotBtn.setText("Ancestry  —  "
                    + (character.ancestry.isBlank() ? "Nicht gewählt" : character.ancestry));
        if (backgroundSlotBtn != null)
            backgroundSlotBtn.setText("Background  —  "
                    + (character.background.isBlank() ? "Nicht gewählt" : character.background));

        logger.fine("Level-0-Block aktualisiert: "
                + character.className + " | " + character.ancestry + " | " + character.background);
    }

    private double calcListWidth(List<String> names) {
        Text ruler = new Text();
        ruler.setStyle("-fx-font-size: 13px;");
        double max = 80; // Minimum
        for (String n : names) {
            ruler.setText(n);
            double w = ruler.getLayoutBounds().getWidth();
            if (w > max) max = w;
        }
        return max + 28;
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
    private Label makeLabel(Skill text) {
        Label l = new Label(text.displayName());
        l.setTextFill(Color.BLACK);
        return l;
    }

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
        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) loadCharacter(selected);
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNewCharacter(listView));

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveCharacter());

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            deleteCharacter(selected);
        });

        VBox box = new VBox(6, search, listView, newBtn, saveBtn, deleteBtn);
        box.setPadding(new Insets(8));
        return box;
    }

    private void loadCharNames() {
        charNames.clear();

        File dir = EditorIO.dataDir(DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Charakterverzeichnis erstellt: " + dir.getAbsolutePath());
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;
        for (File f : files) charNames.add(f.getName().replace(".json", ""));
        charNames.sort(String::compareToIgnoreCase);
        logger.info("Charaktere geladen: " + charNames.size());
    }

    private void loadCharacter(String name) {
        character = EditorIO.load(
                DIR + name + ".json",
                new TypeReference<>() {
                },
                new PlayerCharacter());
        character.name = name;
        character.refresh();

        refreshOverview();

        logger.info("Geladen: " + name + " | STR=" + character.str + " DEX=" + character.dex
                + " CON=" + character.con + " INT=" + character.intel
                + " WIS=" + character.wis + " CHA=" + character.cha);
    }

    private void createNewCharacter(ListView<String> listView) {
        character = new PlayerCharacter();
        character.name = "Neuer Charakter";
        nameField.setText(character.name);
        listView.getSelectionModel().select(character.name);
        logger.fine("Neuer Charakter erstellt");
    }

    private void saveCharacter() {
        if (character == null) return;

        String newName = nameField.getText().trim();
        if (newName.isBlank()) {
            logger.warning("Speichern: Name leer");
            return;
        }

        // Rename — alte Datei löschen
        if (!character.name.equals(newName)) {

            File old = EditorIO.dataDir(DIR + character.name + ".json");
            if (old.exists() && old.delete()) {
                charNames.remove(character.name);
                logger.info("Alte Datei gelöscht: " + character.name);
            }
        }

        character.name = newName;
        character.level = parseLevel();

        character.skills.clear();
        for (Skill skill : Skill.values()) {
            character.skills.put(skill, skillProfs.get(skill).getValue());
        }

        try {
            EditorIO.save(DIR + newName + ".json", character);
            logger.info("Gespeichert: " + newName + " | STR=" + character.str + " DEX=" + character.dex);
        } catch (Exception ex) {
            logger.severe("Speichern fehlgeschlagen: " + ex.getMessage());
            return;
        }

        if (!charNames.contains(newName)) {
            charNames.add(newName);
            charNames.sort(String::compareToIgnoreCase);
        }
    }

    private void deleteCharacter(String name) {

        File f = EditorIO.dataDir(DIR + name + ".json");
        if (f.exists() && f.delete()) {
            charNames.remove(name);
            character = new PlayerCharacter();
            nameField.clear();
            logger.info("Charakter gelöscht: " + name);
        } else {
            logger.warning("Löschen fehlgeschlagen: " + name);
        }
    }

    public static List<PlayerCharacter> loadCharakterfromDisk() {
        File dir = PathResolver.resolveWritable(DIR).toFile();
        if (!dir.exists()) return new ArrayList<>();

        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return new ArrayList<>();

        List<PlayerCharacter> result = new ArrayList<>();
        for (File f : files) {
            PlayerCharacter c = EditorIO.load(DIR + "/" + f.getName(), new TypeReference<>() {
            }, null);
            if (c != null) result.add(c);
            else logger.warning("Charakter konnte nicht geladen werden: " + f.getName());
        }

        logger.info("Charaktere geladen: " + result.size());
        return result;
    }

    public static List<String> loadCharacterNames() {
        File dir = PathResolver.resolveWritable(DIR).toFile();
        if (!dir.exists()) return new ArrayList<>();

        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return new ArrayList<>();

        return Arrays.stream(files)
                .map(f -> f.getName().replace(".json", ""))
                .sorted()
                .toList();
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
    }
}