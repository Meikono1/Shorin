package com.fuchsbau.shorin.RPG.Intro;

import com.fuchsbau.shorin.Engine.RPG.DetailWindow;
import com.fuchsbau.shorin.Engine.RPG.ScenarioDefinition;
import com.fuchsbau.shorin.Engine.RPG.StartClassLoader;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Races.Base.Attributes;
import com.fuchsbau.shorin.Races.Base.Race;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static javafx.scene.paint.Color.RED;


public class CharacterCreator implements Saveble {
    private final Logger logger = FileLogger.getLogger();
    private final CharacterCreatorBinder characterDraft = new CharacterCreatorBinder();
    private byte level = 0;
    private Label size;
    private Label speed;
    private Label money;
    private Race selectedRace;

    private final ScenarioDefinition definition;
    private final static SceneBuilder scenebuilder = SceneBuilder.getSceneBuilder();

    // Controls
    private enum Section {BASE, BODY, HEAD, BREASTS, GENITALIA, TAIL, EXTRAS}


    // Paperdoll
    private final double BASE_W = 840;
    private final double BASE_H = 1040;
    private final Canvas paperdollCanvas = new Canvas();

    private final StackPane paperdollNode = new StackPane(paperdollCanvas);

    // Linke Seite
    private double lastDivider = 0.62;

    private final GridPane settingsGrid = new GridPane();
    private final SplitPane split = new SplitPane();

    private Attributes lastRaceMod = new Attributes(0, 0, 0, 0, 0, 0);

    // Stats
    public static final class StatBlock {
        public final IntegerProperty pointsLeft = new SimpleIntegerProperty(8);

        public final IntegerProperty str = new SimpleIntegerProperty(0);
        public final IntegerProperty dex = new SimpleIntegerProperty(0);
        public final IntegerProperty con = new SimpleIntegerProperty(0);
        public final IntegerProperty intel = new SimpleIntegerProperty(0);
        public final IntegerProperty wis = new SimpleIntegerProperty(0);
        public final IntegerProperty cha = new SimpleIntegerProperty(0);

        // min pro Stat (durch Rassen-Malus). Standard 0.
        public final IntegerProperty minStr = new SimpleIntegerProperty(0);
        public final IntegerProperty minDex = new SimpleIntegerProperty(0);
        public final IntegerProperty minCon = new SimpleIntegerProperty(0);
        public final IntegerProperty minInt = new SimpleIntegerProperty(0);
        public final IntegerProperty minWis = new SimpleIntegerProperty(0);
        public final IntegerProperty minCha = new SimpleIntegerProperty(0);

        // max pro Stat Standard 0.
        public final IntegerProperty maxStr = new SimpleIntegerProperty(3);
        public final IntegerProperty maxDex = new SimpleIntegerProperty(3);
        public final IntegerProperty maxCon = new SimpleIntegerProperty(3);
        public final IntegerProperty maxInt = new SimpleIntegerProperty(3);
        public final IntegerProperty maxWis = new SimpleIntegerProperty(3);
        public final IntegerProperty maxCha = new SimpleIntegerProperty(3);
    }

    private final StatBlock stats = new StatBlock();

    public CharacterCreator(ScenarioDefinition definition) {
        this.definition = definition;
    }

    public Scene getScene() {
        Scene scene = makeScene();
        applyInitialState();
        String cssUrl = CSSLoader.resolveUserOrBackupCSS();
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl);
        } else {
            logger.warning("CSS not found: css/main.css");
        }
        return scene;
    }

    public Scene makeScene() {
        BorderPane root = new BorderPane();

        // SplitPane (Links = settings, Rechts = stats + paperdoll model)
        split.setDividerPositions(lastDivider); // links 62%, rechts ~38% initial

        StackPane centerWrap = new StackPane(split);
        StackPane.setAlignment(split, Pos.CENTER_RIGHT);
        centerWrap.setBackground(Background.fill(RED));

        root.setCenter(centerWrap);

        // Links: Settings in 4 Spalten (wrap in ScrollPane)
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(10);
        settingsGrid.setPadding(new Insets(10));
        settingsGrid.setMinWidth(0);
        settingsGrid.setMaxWidth(Double.MAX_VALUE);

        // 4 columns: Label / Alert / Input / Extra
        ColumnConstraints c1 = new ColumnConstraints(); // Label
        c1.setPercentWidth(15);
        c1.setHgrow(Priority.NEVER);
        c1.setMinWidth(Region.USE_PREF_SIZE);

        ColumnConstraints cAlert = new ColumnConstraints(); // Alert-Icon
        cAlert.setPercentWidth(2);
        cAlert.setHgrow(Priority.NEVER);

        ColumnConstraints c2 = new ColumnConstraints(); // Input
        c2.setPercentWidth(80);
        c2.setMinWidth(1);
        c2.setFillWidth(true);
        c2.setHgrow(Priority.ALWAYS);

        ColumnConstraints c3 = new ColumnConstraints(); // Extra
        c3.setPercentWidth(15);
        c3.setHgrow(Priority.NEVER);

        settingsGrid.getColumnConstraints().setAll(c1, cAlert, c2, c3);
        buildBaseSection(settingsGrid);

        // Settings
        ScrollPane settingsScroll = new ScrollPane(settingsGrid);
        settingsScroll.setFitToWidth(true);
        settingsScroll.setMinWidth(0);
        settingsScroll.setStyle("-fx-background-color: transparent;");

        // RIGHT: VBox = stats (top) + paperdoll (bottom)
        VBox rightPane = new VBox(12);
        rightPane.setPadding(new Insets(10));

        // Stats area
        VBox statsBox = new VBox(8);
        statsBox.getStyleClass().add("stats-box");


        // Spalten: Name | Wert
        VBox statsGrid = buildStatsPane();

        statsBox.getChildren().addAll(statsGrid);


        // Paperdoll canvas area (StackPane for overlays)
        StackPane paperdollWrap = new StackPane();
        paperdollWrap.setPadding(new Insets(10));
        paperdollWrap.setStyle("-fx-background-color: rgba(30,30,30,0.18); -fx-background-radius: 12;");


        paperdollWrap.getChildren().addAll(paperdollNode);
        VBox.setVgrow(paperdollWrap, Priority.ALWAYS);

        rightPane.getChildren().addAll(statsBox, paperdollWrap);
        rightPane.setMinWidth(0);


        setupPaperdollCanvasScaling(paperdollWrap);

        split.getItems().addAll(settingsScroll, rightPane);


        // Top
        HBox topBar = buildTopStepBar();
        HBox statsBar = buildStatsBar();

        VBox topContainer = new VBox();

        topContainer.getStyleClass().add("top-hbox");
        topContainer.getChildren().addAll(topBar, statsBar);

        root.setTop(topContainer);

        return new Scene(root);
    }

    // ---------- UI: StatsGrid 3x2, aber mit +/- und PointsLeft ----------
    private VBox buildStatsPane() {
        VBox box = new VBox(10);

        Label statsTitle = new Label("Stats");
        statsTitle.setStyle("-fx-font-size: 14px; " + "-fx-font-weight: bold; " + "-fx-padding: 0 10 0 0; " + "-fx-text-fill: #e6e6ff;");

        // Header: "Punkte" + Restpunkte
        Label pointsLabel = new Label("Remaining points:");
        Label pointsLeftValue = new Label();


        pointsLeftValue.textProperty().bind(stats.pointsLeft.asString());

        HBox pointsRow = new HBox(8, pointsLabel, pointsLeftValue);
        pointsRow.setAlignment(Pos.CENTER_LEFT);

        HBox topRow = new HBox(50, statsTitle, pointsRow);


        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);

        // 6 Spalten: (Attr | [ -  value  + ]) x2
        for (int i = 0; i < 6; i++) {
            ColumnConstraints c = new ColumnConstraints();
            if (i == 0 || i == 3) c.setHgrow(Priority.NEVER);
            else c.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(c);
        }

        // Row 0
        addStatCell(grid, 0, 0, "STR", stats.str, stats.minStr, stats.maxStr);
        addStatCell(grid, 0, 3, "DEX", stats.dex, stats.minDex, stats.maxDex);

        // Row 1
        addStatCell(grid, 1, 0, "CON", stats.con, stats.minCon, stats.maxCon);
        addStatCell(grid, 1, 3, "INT", stats.intel, stats.minInt, stats.maxInt);

        // Row 2
        addStatCell(grid, 2, 0, "WIS", stats.wis, stats.minWis, stats.maxWis);
        addStatCell(grid, 2, 3, "CHA", stats.cha, stats.minCha, stats.maxCha);

        box.getChildren().addAll(topRow, grid);
        return box;
    }

    private void addStatCell(GridPane grid, int row, int col, String name, IntegerProperty statValue, IntegerProperty minValue, IntegerProperty maxValue) {

        Label statName = new Label(name);

        Label info = new Label("?");
        info.setFocusTraversable(false);
        info.setMinSize(16, 16);
        info.setAlignment(Pos.CENTER);
        info.setCursor(Cursor.HAND);
        info.setOnMouseClicked(mouseEvent -> {
            setSection(Section.EXTRAS);
            animateContentSwap(settingsGrid);
        });

        Tooltip tip = new Tooltip(getStatHelpText(name)); // oder Map/Enum
        tip.setShowDelay(Duration.millis(250));
        tip.setWrapText(true);
        tip.setMaxWidth(280);
        Tooltip.install(info, tip);

        HBox statHeader = new HBox(6, statName, info);
        statHeader.setAlignment(Pos.CENTER_LEFT);

        Button minus = scenebuilder.makeButton("–");
        Label value = new Label();
        value.textProperty().bind(statValue.asString());
        Button plus = scenebuilder.makeButton("+");

        minus.setMinWidth(36);
        plus.setMinWidth(36);

        minus.setOnAction(e -> tryDecStat(statValue, minValue));
        plus.setOnAction(e -> tryIncStat(statValue, maxValue));

        minus.disableProperty().bind(statValue.lessThanOrEqualTo(minValue));
        plus.disableProperty().bind(stats.pointsLeft.lessThanOrEqualTo(0).or(statValue.greaterThanOrEqualTo(maxValue)));

        HBox controls = new HBox(6, minus, value, plus);
        controls.setAlignment(Pos.CENTER_LEFT);

        grid.add(statHeader, col, row);
        grid.add(controls, col + 1, row, 2, 1);
    }

    private String getStatHelpText(String name) {
        return switch (name) {
            case "STR" -> "Strength: Hitchance with Melee weapons, Dmg in Meelee and Carrying capacity";
            case "DEX" -> "Dexterity: Hitchance with ranged weapons, Armor and dodging";
            case "CON" -> "Constitution: Total Health, health regeneration and resisting harm";
            case "INT" -> "Intelligence: Skill points, learning speed and languages";
            case "WIS" -> "Wisdom: Perception, resistance vs Spells";
            case "CHA" -> "Charisma: Charming, Intimidating, influencing";
            default -> "how? (CCJSHT)";
        };
    }


    private void tryIncStat(IntegerProperty stat, IntegerProperty max) {
        if (stats.pointsLeft.get() <= 0 || stat.get() >= max.get()) return;
        stat.set(stat.get() + 1);
        stats.pointsLeft.set(stats.pointsLeft.get() - 1);
        characterDraft.health.set(selectedRace.getMaxHealth() + (level * stats.con.get()));
    }

    private void tryDecStat(IntegerProperty stat, IntegerProperty min) {
        if (stat.get() <= min.get()) return;
        stat.set(stat.get() - 1);
        stats.pointsLeft.set(stats.pointsLeft.get() + 1);
        characterDraft.health.set(selectedRace.getMaxHealth() + (level * stats.con.get()));
    }

    // ---------- Rassen-Malus anwenden ----------
    private void applyRaceMalus(Race race) {
        int baseMax = 3;

        // alten Mod zurücknehmen (damit Wechsel nicht akkumuliert)
        add(stats.str, -lastRaceMod.strength());
        add(stats.dex, -lastRaceMod.dexterity());
        add(stats.con, -lastRaceMod.constitution());
        add(stats.intel, -lastRaceMod.intellect());
        add(stats.wis, -lastRaceMod.wisdom());
        add(stats.cha, -lastRaceMod.charisma());

        Attributes mod = (race != null && race.getAttributes() != null) ? race.getAttributes() : new Attributes(0, 0, 0, 0, 0, 0);

        // neuen Mod anwenden
        add(stats.str, mod.strength());
        add(stats.dex, mod.dexterity());
        add(stats.con, mod.constitution());
        add(stats.intel, mod.intellect());
        add(stats.wis, mod.wisdom());
        add(stats.cha, mod.charisma());

        // min/max setzen: Bereich wird um mod verschoben
        setRange(stats.minStr, stats.maxStr, baseMax, mod.strength());
        setRange(stats.minDex, stats.maxDex, baseMax, mod.dexterity());
        setRange(stats.minCon, stats.maxCon, baseMax, mod.constitution());
        setRange(stats.minInt, stats.maxInt, baseMax, mod.intellect());
        setRange(stats.minWis, stats.maxWis, baseMax, mod.wisdom());
        setRange(stats.minCha, stats.maxCha, baseMax, mod.charisma());

        lastRaceMod = mod;

        clampToMinMax();
    }

    private static void setRange(IntegerProperty min, IntegerProperty max, int baseMax, int mod) {
        min.set(mod);
        max.set(baseMax + mod);
    }

    private static void add(IntegerProperty prop, int delta) {
        prop.set(prop.get() + delta);
    }

    private void clampToMinMax() {
        clamp(stats.str, stats.minStr.get(), stats.maxStr.get());
        clamp(stats.dex, stats.minDex.get(), stats.maxDex.get());
        clamp(stats.con, stats.minCon.get(), stats.maxCon.get());
        clamp(stats.intel, stats.minInt.get(), stats.maxInt.get());
        clamp(stats.wis, stats.minWis.get(), stats.maxWis.get());
        clamp(stats.cha, stats.minCha.get(), stats.maxCha.get());
    }

    private static void clamp(IntegerProperty v, int min, int max) {
        int x = v.get();
        if (x < min) v.set(min);
        else if (x > max) v.set(max);
    }

    private HBox buildTopStepBar() {

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        //topBar.getStyleClass().add("top-hbox");

        // Step Buttons aus Sections

        for (Section section : Section.values()) {
            char[] label = section.name().toCharArray();
            for (int i = 1; i < label.length; i++) {
                label[i] = Character.toLowerCase(label[i]);
            }
            ToggleButton toggleButton = scenebuilder.makeMenuToggleButton(new String(label));
            toggleButton.setOnMouseClicked(mouseEvent -> {
                setSection(section);
                animateContentSwap(settingsGrid);
            });
            topBar.getChildren().add(toggleButton);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button confirmBtn = scenebuilder.createMenuButton("Continue");
        confirmBtn.setOnMouseClicked(mouseEvent -> {
            Main.getStage().setScene(StartClassLoader.getEntry(definition.sceneClass()).getScene(-1));
        });

        topBar.getChildren().addAll(spacer, confirmBtn);
        return topBar;
    }

    private HBox buildStatsBar() {
        HBox stats = new HBox(20);
        stats.setPadding(new Insets(5, 10, 10, 10));
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getStyleClass().add("stats-hbox");


        Label name = SceneBuilder.createTextLabel("Name:");
        Label nameLabel = SceneBuilder.createTextLabel("");
        nameLabel.textProperty().bind(characterDraft.name);

        characterDraft.health.set(selectedRace.getMaxHealth() + (level * this.stats.con.get()));
        Label hp = SceneBuilder.createTextLabel("");
        hp.textProperty().bind(characterDraft.health.asString("HP: %d"));

        size = SceneBuilder.createTextLabel("Size: " + selectedRace.getSize().name());
        speed = SceneBuilder.createTextLabel("Speed: " + selectedRace.getSpeed() + "ft");

        money = SceneBuilder.createTextLabel("Gold: " + characterDraft.gold.get() + ", Silver: " + characterDraft.silver.get() + ", Copper: " + characterDraft.copper.get());

        stats.getChildren().addAll(name, nameLabel, hp, size, speed, money);
        return stats;
    }

    private void setSection(Section section) {
        if (!split.getDividers().isEmpty()) {
            lastDivider = split.getDividers().getFirst().getPosition();
        }
        settingsGrid.getChildren().clear();

        switch (section) {
            case BASE:
                buildBaseSection(settingsGrid);
                break;
            case BODY:
                buildBodySection(settingsGrid);
                break;
            case HEAD:
                buildHeadSection(settingsGrid);
                break;
            case BREASTS:
                buildBreastsSection(settingsGrid);
                break;
            case GENITALIA:
                buildGenitaliaSection(settingsGrid);
                break;
            case TAIL:
                buildTailSection(settingsGrid);
                break;
            case EXTRAS:
                buildExtrasSection(settingsGrid);
                break;
            default:
                logger.severe("Section existiert nicht : " + section);
        }

        Platform.runLater(() -> split.setDividerPositions(lastDivider));
    }

    private void buildBaseSection(GridPane settingsGrid) {
        ComboBox<String> raceDropdown = SceneBuilder.makeDropdown();
        ComboBox<String> sexDropdown = SceneBuilder.makeDropdown();
        Map<String, Race> byDisplay = new HashMap<>();

        Slider ageSlider = new Slider();
        ageSlider.setSnapToTicks(true);
        ageSlider.setMajorTickUnit(1);
        ageSlider.setMinorTickCount(0);
        ageSlider.setBlockIncrement(1);
        ageSlider.setValue(characterDraft.age.getValue());

        ageSlider.valueProperty().addListener((obs, ov, nv) -> {
            int v = nv.intValue();
            if (characterDraft.age.get() != v) characterDraft.age.set(v);
        });
        characterDraft.age.addListener((obs, ov, nv) -> {
            double v = nv.doubleValue();
            if (ageSlider.getValue() != v) ageSlider.setValue(v);
        });

        Consumer<Race> syncAgeRange = (race) -> {
            if (race == null) return;

            int min = race.getLifeStage().ageAdult();
            int max = race.getLifeStage().ageOld();

            ageSlider.setMin(min);
            ageSlider.setMax(max);

            int current = characterDraft.age.get();
            int clamped = Math.max(min, Math.min(max, current));
            if (clamped != current) characterDraft.age.set(clamped);
        };

        Label ageValue = new Label();
        ageValue.textProperty().bind(characterDraft.age.asString());

        for (Race entry : definition.parsedRaces()) {
            String dn = entry.displayName();
            raceDropdown.getItems().add(dn);
            byDisplay.put(dn, entry);
        }

        if (selectedRace == null) {
            selectedRace = definition.parsedRaces().getFirst();
        }

        raceDropdown.getSelectionModel().select(selectedRace.displayName());
        characterDraft.race.set(selectedRace.displayName());

        applyRaceMalus(selectedRace);

        raceDropdown.valueProperty().addListener((obs, oldV, newV) -> {
            Race e = byDisplay.get(newV);
            selectedRace = e;
            applyRaceMalus(e);
            characterDraft.health.set(e.getMaxHealth() + (level * stats.con.get()));
            size.setText("Size: " + selectedRace.getSize().name());
            speed.setText("Speed: " + selectedRace.getSpeed() + "ft");

            syncAgeRange.accept(e);
            characterDraft.age.set(e.getLifeStage().ageAdult());
        });

        sexDropdown.getItems().add("Male");
        sexDropdown.getItems().add("Female");
        sexDropdown.getSelectionModel().select("Male");


        DetailWindow detailWindow = new DetailWindow(Main.getStage().getScene().getWindow());

        Label info = new Label("?");
        info.getStyleClass().add("info-icon");
        info.setCursor(Cursor.HAND);
        info.setOnMouseClicked(e -> detailWindow.showRace(selectedRace));

        TextField nameTextField = new TextField();
        nameTextField.textProperty().bindBidirectional(characterDraft.name);
        addSettingRow(settingsGrid, 1, "Name", null, nameTextField, null);

        raceDropdown.valueProperty().bindBidirectional(characterDraft.race);
        addSettingRow(settingsGrid, 2, "Race", info, raceDropdown, null);
        addSettingRow(settingsGrid, 3, "Gender", null, sexDropdown, null);
        addSettingRow(settingsGrid, 4, "Age", null, ageSlider, ageValue);
        if (selectedRace != null) syncAgeRange.accept(selectedRace);
    }

    private void buildBodySection(GridPane settingsGrid) {
        Slider feminitySlider = new Slider();
        feminitySlider.valueProperty().bind(characterDraft.feminity);
        addSettingRow(settingsGrid, 1, "Feminity", null, feminitySlider, null);

        TextField bodySize = new TextField();
        bodySize.textProperty().bindBidirectional(characterDraft.bodySize);
        addSettingRow(settingsGrid, 2, "BodySize", null, bodySize, null);

        TextField muscle = new TextField();
        muscle.textProperty().bindBidirectional(characterDraft.muscleDefinition);
        addSettingRow(settingsGrid, 3, "Muscle", null, muscle, null);

        TextField bodyHeight = new TextField();
        bodyHeight.textProperty().bindBidirectional(characterDraft.bodyHeight);
        addSettingRow(settingsGrid, 4, "Height", null, bodyHeight, null);
        addSettingRow(settingsGrid, 5, "Pattern", null, new TextField(), null); //Base pattern falls vorhanden, ansonsten skincolour.

        addSettingRow(settingsGrid, 6, "Arms", null, new TextField(), new Label("Years")); //Papermodel arm selection, maybe more idc
        addSettingRow(settingsGrid, 7, "Legs", null, new TextField(), new Label("Years")); //Papermodel leegs selection, maybe more idc , same as arms

    }

    private void buildHeadSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Height", null, new Slider(120, 220, 180), new Label("cm"));// in papermodel abbilden
        addSettingRow(settingsGrid, 2, "Hair", null, new TextField(), null); // hair aus liste für papermodel
        addSettingRow(settingsGrid, 3, "Ears", null, new TextField(), null); // ohren aus liste für papermodel
        addSettingRow(settingsGrid, 4, "Lips/Muzzle", null, new TextField(), null); // unterschiedlliche Muzzles, bzw lippen und dazugehörige größen.
        addSettingRow(settingsGrid, 5, "Throat Capacity", null, new TextField(), null); // hat einfluss darauf wieviel breite genommen werden kann.
        addSettingRow(settingsGrid, 6, "Throat Depth", null, new TextField(), null); // wie lang der penis/ anderes sein kann befohr ein gag reflex ausgelöst wird. Später auch ersticken?
        addSettingRow(settingsGrid, 7, "Tongue Modifier", null, new TextField(), null); // Catzenzunge kann rau oder weich sein.
        addSettingRow(settingsGrid, 8, "Horns", null, new TextField(), null); // falls hörner vorhanden sind, anpassen für papermodel zusammen mit größen.
    }

    private void buildBreastsSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Breast", null, new TextField(), new Label("Years")); // breast model für paperdoll auch die anzahl an nippel
        addSettingRow(settingsGrid, 2, "Size", null, new TextField(), new Label("Years")); // Größe, wenn keine flachen brüste genommen wurden.
        addSettingRow(settingsGrid, 3, "Lactation", null, new TextField(), new Label("Years")); // True/false
        addSettingRow(settingsGrid, 4, "Lactation Capacity", null, new TextField(), new Label("Years")); // selbst erklärend
        addSettingRow(settingsGrid, 5, "Nipple", null, new TextField(), new Label("Years")); // manche rassen haben keine, True/false
        addSettingRow(settingsGrid, 6, "Nipple größe", null, new TextField(), new Label("Years")); // kann unterschidedlich sein pro rasse, komm ich später zurück

    }

    private void buildGenitaliaSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Penis Type", null, new TextField(), new Label("Years")); //Für papermodel, Kann auch Pseudopenis sein oder ähnliches. Ist als model hinterlegt
        addSettingRow(settingsGrid, 2, "Penis MODS", null, new TextField(), new Label("Years")); // Automatisch gefüllt mit typen für späteres spielen (knot und so)
        addSettingRow(settingsGrid, 3, "Penis Length", null, new TextField(), new Label("Years")); // länge ist multiplicator für den typen
        addSettingRow(settingsGrid, 4, "Penis Girth", null, new TextField(), new Label("Years")); // Automatisch berechnet mit spielraum
        addSettingRow(settingsGrid, 5, "Cum Storage", null, new TextField(), new Label("Years")); // Automatisch gefüllt mit spielraum / Balls kann man nicht einstellen! sind abhängig von Penis größe


        addSettingRow(settingsGrid, 6, "Vagina Type", null, new TextField(), new Label("Years")); //Für papermodel
        addSettingRow(settingsGrid, 7, "Vagina MODS", null, new TextField(), new Label("Years")); // Automatisch gefüllt mit typen für späteres spielen (Eier legen und so)
        addSettingRow(settingsGrid, 8, "Vagina depth", null, new TextField(), new Label("Years")); // Tiefe nur für spiel benötigt, wie tief bis zum Cervix, von Rasse/Größe abhängig mit Spielraum
        addSettingRow(settingsGrid, 9, "Vagina Capacity", null, new TextField(), null); // Wie weit sich der kanal breiten kann ohne probleme, 1.1 Modifier für unangenehm und so weiter
        addSettingRow(settingsGrid, 10, "Hymen", null, new TextField(), new Label("Years")); // true/false


        addSettingRow(settingsGrid, 11, "Ass size", null, new TextField(), new Label("Years")); // Größe der hinterteils, Kein einfluss auf papermodel
        addSettingRow(settingsGrid, 12, "Anus depth", null, new TextField(), new Label("Years")); // Tiefe nur für spiel benötigt, wie tief der Rectum ist
        addSettingRow(settingsGrid, 13, "Anus Capacity", null, new TextField(), null); // Wie weit sich der anus breiten kann ohne probleme, 1.1 Modifier für unangenehm und so weiter

    }

    private void buildTailSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Tail Type", null, new TextField(), new Label("Years")); //Für papermodel
        addSettingRow(settingsGrid, 2, "Tail MODS", null, new TextField(), new Label("Years")); // Automatisch gefüllt mit typen für späteres spielen (Prehensile und sowas)
        addSettingRow(settingsGrid, 3, "Tail size", null, new TextField(), new Label("Years")); // länge ist multiplicator für den typen, in maßen
    }

    private void buildExtrasSection(GridPane settingsGrid) {
        //TODO Flügel erstmal gestrichen, Extras sind details über Stats zu charakter info
    }

    private void wirePaperdollRedraw() {
        // initial
        drawPaperdollPlaceholder();
    }

    private void setupPaperdollCanvasScaling(StackPane paperdollWrap) {
        paperdollCanvas.setWidth(BASE_W);
        paperdollCanvas.setHeight(BASE_H);

        var scale = Bindings.createDoubleBinding(() -> {
            double availW = paperdollWrap.getWidth() - paperdollWrap.getPadding().getLeft() - paperdollWrap.getPadding().getRight();
            double availH = paperdollWrap.getHeight() - paperdollWrap.getPadding().getTop() - paperdollWrap.getPadding().getBottom();
            if (availW <= 0 || availH <= 0) return 1.0;
            return Math.min(availW / BASE_W, availH / BASE_H);
        }, paperdollWrap.widthProperty(), paperdollWrap.heightProperty(), paperdollWrap.paddingProperty());

        paperdollCanvas.scaleXProperty().bind(scale);
        paperdollCanvas.scaleYProperty().bind(scale);

        paperdollCanvas.layoutXProperty().bind(paperdollWrap.widthProperty().subtract(paperdollCanvas.getWidth()).divide(2));
        paperdollCanvas.layoutYProperty().bind(paperdollWrap.heightProperty().subtract(paperdollCanvas.getHeight()).divide(2));

        paperdollCanvas.setManaged(false);
        StackPane.setAlignment(paperdollCanvas, Pos.CENTER);

        drawPaperdollPlaceholder();
    }

    private static void addSettingRow(GridPane grid, int row, String labelText, Node alertNode, Node col2, Node col3) {
        Label label = new Label(labelText);
        label.setMinWidth(Region.USE_PREF_SIZE);

        GridPane.setHgrow(col2, Priority.ALWAYS);
        if (col2 instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            r.setMinWidth(0);
            r.setPrefWidth(0);
        }

        grid.add(label, 0, row);

        if (alertNode != null) {
            grid.add(alertNode, 1, row);
            GridPane.setValignment(alertNode, VPos.CENTER);
        }

        grid.add(col2, 2, row);

        if (col3 != null) {
            grid.add(col3, 3, row);
        }
    }

    private void drawPaperdollPlaceholder() {
        GraphicsContext g = paperdollCanvas.getGraphicsContext2D();
        double w = paperdollCanvas.getWidth();
        double h = paperdollCanvas.getHeight();

        g.setFill(Color.rgb(0, 0, 0, 0));
        g.clearRect(0, 0, w, h);

        // subtle grid
        g.setStroke(Color.rgb(255, 255, 255, 0.08));
        for (int x = 0; x <= w; x += 40) g.strokeLine(x, 0, x, h);
        for (int y = 0; y <= h; y += 40) g.strokeLine(0, y, w, y);

        // simple mannequin
        g.setStroke(Color.rgb(255, 255, 255, 0.35));
        g.setLineWidth(3);

        double cx = w / 2.0;
        double headR = 80;

        g.strokeOval(cx - headR, 140 - headR, headR * 2, headR * 2);           // head
        g.strokeLine(cx, 220, cx, 640);                                       // torso
        g.strokeLine(cx, 340, cx - 240, 260);                                 // left arm
        g.strokeLine(cx, 340, cx + 240, 260);                                 // right arm
        g.strokeLine(cx, 640, cx - 160, 1000);                                  // left leg
        g.strokeLine(cx, 640, cx + 160, 1000);                                  // right leg

        g.setFill(Color.rgb(255, 255, 255, 0.65));
        g.fillText("Paperdoll (Canvas Platzhalter)", 14, 18);
    }

    private void applyInitialState() {
        wirePaperdollRedraw();
    }

    private void animateContentSwap(Node content) {
        content.setTranslateX(40);
        content.setOpacity(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(180), content);
        tt.setFromX(40);
        tt.setToX(0);

        FadeTransition ft = new FadeTransition(Duration.millis(180), content);
        ft.setFromValue(0);
        ft.setToValue(1);

        new ParallelTransition(tt, ft).play();
    }

    @Override
    public Scene getScene(int stage) {
        return getScene();
    }

    @Override
    public void reset() {

    }
}
