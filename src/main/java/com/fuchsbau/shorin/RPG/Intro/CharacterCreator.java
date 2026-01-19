package com.fuchsbau.shorin.RPG.Intro;

import com.fuchsbau.shorin.Engine.RPG.ScenarioDefinition;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.RPG.Saveble;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.logging.Logger;

import static javafx.scene.paint.Color.RED;


public class CharacterCreator implements Saveble {
    private final Logger logger = FileLogger.getLogger();
    private final CharacterCreatorBinder characterDraft = new CharacterCreatorBinder();

    private final ScenarioDefinition definition;
    private final static SceneBuilder scenebuilder = SceneBuilder.getSceneBuilder();

    // Controls
    private final ComboBox<String> raceDropdown = new ComboBox<>();

    private enum Section {BASE, BODY, HEAD, ARMS, LEGS, BREASTS, GENITALIA, TAIL, EXTRAS}

    private final ObjectProperty<Section> activeSection = new SimpleObjectProperty<>(Section.BASE);
    private final Button confirmBtn = scenebuilder.createMenuButton("Continue");


    // Paperdoll
    private final double BASE_W = 840;
    private final double BASE_H = 1040;
    private final Canvas paperdollCanvas = new Canvas();

    private final StackPane paperdollNode = new StackPane(paperdollCanvas);

    // Linke Seite
    private double lastDivider = 0.62;

    private final GridPane settingsGrid = new GridPane();
    private final SplitPane split = new SplitPane();


    // Stats
    public final IntegerProperty pointsLeft = new SimpleIntegerProperty(8);

    public static final class StatBlock {
        public final IntegerProperty pointsLeft = new SimpleIntegerProperty(12);

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
        public final IntegerProperty maxStr = new SimpleIntegerProperty(4);
        public final IntegerProperty maxDex = new SimpleIntegerProperty(4);
        public final IntegerProperty maxCon = new SimpleIntegerProperty(4);
        public final IntegerProperty maxInt = new SimpleIntegerProperty(4);
        public final IntegerProperty maxWis = new SimpleIntegerProperty(4);
        public final IntegerProperty maxCha = new SimpleIntegerProperty(4);
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

        HBox topBar = buildTopStepBar();
        root.setTop(topBar);

        // SplitPane (Links = settings, Rechts = stats + paperdoll model)
        split.setDividerPositions(lastDivider); // links 62%, rechts ~38% initial

        StackPane centerWrap = new StackPane(split);
        StackPane.setAlignment(split, Pos.CENTER_RIGHT);
        centerWrap.setBackground(Background.fill(RED));

        root.setCenter(centerWrap);

        // Links: Settings in 3 Spalten (wrap in ScrollPane)
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(10);
        settingsGrid.setPadding(new Insets(10));

        // 3 columns: Label / Input / extra
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(28);
        c1.setHgrow(Priority.NEVER);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(44);
        c2.setHgrow(Priority.ALWAYS);

        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(28);
        c3.setHgrow(Priority.NEVER);

        settingsGrid.getColumnConstraints().addAll(c1, c2, c3);
        buildBaseSection(settingsGrid);

        // Settings
        ScrollPane settingsScroll = new ScrollPane(settingsGrid);
        settingsScroll.setFitToWidth(true);
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

        // Placeholder overlay label
        Label paperdollTitle = new Label("Paperdoll");
        paperdollTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        StackPane.setAlignment(paperdollTitle, Pos.TOP_LEFT);

        paperdollWrap.getChildren().addAll(paperdollNode, paperdollTitle);
        VBox.setVgrow(paperdollWrap, Priority.ALWAYS);

        rightPane.getChildren().addAll(statsBox, paperdollWrap);

        if (settingsScroll instanceof Region r) r.setMinWidth(0);
        if (rightPane instanceof Region r) r.setMinWidth(0);


        setupPaperdollCanvasScaling(paperdollWrap);

        split.getItems().addAll(settingsScroll, rightPane);

        return new Scene(root);
    }

    // ---------- UI: StatsGrid 3x2, aber mit +/- und PointsLeft ----------
    private VBox buildStatsPane() {
        VBox box = new VBox(10);

        Label statsTitle = new Label("Stats");
        statsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; fx-margin-right: 10; fx.text-fill: e6e6ff;");

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
            if (i == 0 || i == 3) c.setHgrow(Priority.NEVER);          // Attr labels
            else c.setHgrow(Priority.NEVER);                            // Controls kompakt
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

    private void addStatCell(GridPane grid, int row, int col, String name,
                             IntegerProperty statValue, IntegerProperty minValue, IntegerProperty maxValue) {

        Label statName = new Label(name);

        Button minus = scenebuilder.makeButton("–");
        Label value = new Label();
        value.textProperty().bind(statValue.asString());
        Button plus = scenebuilder.makeButton("+");

        minus.setMinWidth(36);
        plus.setMinWidth(36);

        // Aktionen
        minus.setOnAction(e -> tryDecStat(statValue, minValue));
        plus.setOnAction(e -> tryIncStat(statValue, maxValue));

        // Disable-Logik
        minus.disableProperty().bind(statValue.lessThanOrEqualTo(minValue));
        plus.disableProperty().bind(
                stats.pointsLeft.lessThanOrEqualTo(0)
                        .or(statValue.greaterThanOrEqualTo(maxValue))
        );


        HBox controls = new HBox(6, minus, value, plus);
        controls.setAlignment(Pos.CENTER_LEFT);

        grid.add(statName, col, row);
        grid.add(controls, col + 1, row, 2, 1);
    }

    private void tryIncStat(IntegerProperty stat, IntegerProperty max) {
        if (stats.pointsLeft.get() <= 0 || stat.get() >= max.get()) return;
        stat.set(stat.get() + 1);
        stats.pointsLeft.set(stats.pointsLeft.get() - 1);
    }

    private void tryDecStat(IntegerProperty stat, IntegerProperty min) {
        if (stat.get() <= min.get()) return;
        stat.set(stat.get() - 1);
        stats.pointsLeft.set(stats.pointsLeft.get() + 1);
    }


    // ---------- Rassen-Malus anwenden (Beispiele) ----------
    private void applyRaceMalus(String race) {
        // reset mins
        stats.minStr.set(0);
        stats.minDex.set(0);
        stats.minCon.set(0);
        stats.minInt.set(0);
        stats.minWis.set(0);
        stats.minCha.set(0);

        // Beispiel: Elf -2 CON
        if ("Elf".equals(race)) {
            stats.minCon.set(-2);
        }
        // Beispiel: Orc -2 INT
        if ("Orc".equals(race)) {
            stats.minInt.set(-2);
        }

        // Optional: wenn aktuelle Werte unter neues Minimum fallen -> clamp
        clampToMins();
    }

    private void clampToMins() {
        clamp(stats.str, stats.minStr);
        clamp(stats.dex, stats.minDex);
        clamp(stats.con, stats.minCon);
        clamp(stats.intel, stats.minInt);
        clamp(stats.wis, stats.minWis);
        clamp(stats.cha, stats.minCha);
    }

    private static void clamp(IntegerProperty stat, IntegerProperty min) {
        if (stat.get() < min.get()) stat.set(min.get());
    }

    private HBox buildTopStepBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-hbox");

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

        raceDropdown.getItems().setAll("Human", "Elf", "Dwarf", "Orc");
        raceDropdown.getSelectionModel().selectFirst();

        topBar.getChildren().addAll(spacer, confirmBtn);
        return topBar;
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
            case ARMS:
                buildArmsSection(settingsGrid);
                break;
            case LEGS:
                buildLegsSection(settingsGrid);
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
        TextField nameTextField = new TextField();
        nameTextField.textProperty().bindBidirectional(characterDraft.name);
        addSettingRow(settingsGrid, 1, "Name", nameTextField, null);

        raceDropdown.valueProperty().bindBidirectional(characterDraft.race);
        addSettingRow(settingsGrid, 2, "Race", raceDropdown, null);

        Slider ageSlider = new Slider();
        ageSlider.valueProperty().bind(characterDraft.age);
        addSettingRow(settingsGrid, 3, "Age", ageSlider, null);

    }

    private void buildBodySection(GridPane settingsGrid) {
        Slider feminitySlider = new Slider();
        feminitySlider.valueProperty().bind(characterDraft.feminity);
        addSettingRow(settingsGrid, 1, "Feminity", feminitySlider, null);

        TextField bodySize = new TextField();
        bodySize.textProperty().bindBidirectional(characterDraft.bodySize);
        addSettingRow(settingsGrid, 2, "BodySize", bodySize, null);

        TextField muscle = new TextField();
        muscle.textProperty().bindBidirectional(characterDraft.muscleDefinition);
        addSettingRow(settingsGrid, 3, "Muscle", muscle, null);

        TextField bodyHeight = new TextField();
        bodyHeight.textProperty().bindBidirectional(characterDraft.bodyHeight);
        addSettingRow(settingsGrid, 4, "Height", bodyHeight, null);
        addSettingRow(settingsGrid, 5, "Pattern", new TextField(), null); //Base pattern falls vorhanden, ansonsten skincolour.
    }

    private void buildHeadSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Height", new Slider(120, 220, 180), new Label("cm"));// in papermodel abbilden
        addSettingRow(settingsGrid, 2, "Hair", new TextField(), null); // hair aus liste für papermodel
        addSettingRow(settingsGrid, 3, "Ears", new TextField(), null); // ohren aus liste für papermodel
        addSettingRow(settingsGrid, 4, "Lips/Muzzle", new TextField(), null); // unterschiedlliche Muzzles, bzw lippen und dazugehörige größen.
        addSettingRow(settingsGrid, 5, "Throat Capacity", new TextField(), null); // hat einfluss darauf wieviel breite genommen werden kann.
        addSettingRow(settingsGrid, 6, "Throat Depth", new TextField(), null); // wie lang der penis/ anderes sein kann befohr ein gag reflex ausgelöst wird. Später auch ersticken?
        addSettingRow(settingsGrid, 7, "Tongue Modifier", new TextField(), null); // Catzenzunge kann rau oder weich sein.
        addSettingRow(settingsGrid, 8, "Horns", new TextField(), null); // falls hörner vorhanden sind, anpassen für papermodel zusammen mit größen.
    }

    private void buildArmsSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Arms", new TextField(), new Label("Years")); //Papermodel arm selection, maybe more idc

    }

    private void buildLegsSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Legs", new TextField(), new Label("Years")); //Papermodel leegs selection, maybe more idc , same as arms
    }

    private void buildBreastsSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Breast", new TextField(), new Label("Years")); // breast model für paperdoll auch die anzahl an nippel
        addSettingRow(settingsGrid, 2, "Size", new TextField(), new Label("Years")); // Größe, wenn keine flachen brüste genommen wurden.
        addSettingRow(settingsGrid, 3, "Lactation", new TextField(), new Label("Years")); // True/false
        addSettingRow(settingsGrid, 4, "Lactation Capacity", new TextField(), new Label("Years")); // selbst erklärend
        addSettingRow(settingsGrid, 5, "Nipple", new TextField(), new Label("Years")); // manche rassen haben keine, True/false
        addSettingRow(settingsGrid, 6, "Nipple größe", new TextField(), new Label("Years")); // kann unterschidedlich sein pro rasse, komm ich später zurück

    }

    private void buildGenitaliaSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Penis Type", new TextField(), new Label("Years")); //Für papermodel, Kann auch Pseudopenis sein oder ähnliches. Ist als model hinterlegt
        addSettingRow(settingsGrid, 2, "Penis MODS", new TextField(), new Label("Years")); // Automatisch gefüllt mit typen für späteres spielen (knot und so)
        addSettingRow(settingsGrid, 3, "Penis Length", new TextField(), new Label("Years")); // länge ist multiplicator für den typen
        addSettingRow(settingsGrid, 4, "Penis Girth", new TextField(), new Label("Years")); // Automatisch berechnet mit spielraum
        addSettingRow(settingsGrid, 5, "Cum Storage", new TextField(), new Label("Years")); // Automatisch gefüllt mit spielraum / Balls kann man nicht einstellen! sind abhängig von Penis größe


        addSettingRow(settingsGrid, 6, "Vagina Type", new TextField(), new Label("Years")); //Für papermodel
        addSettingRow(settingsGrid, 7, "Vagina MODS", new TextField(), new Label("Years")); // Automatisch gefüllt mit typen für späteres spielen (Eier legen und so)
        addSettingRow(settingsGrid, 8, "Vagina depth", new TextField(), new Label("Years")); // Tiefe nur für spiel benötigt, wie tief bis zum Cervix, von Rasse/Größe abhängig mit Spielraum
        addSettingRow(settingsGrid, 9, "Vagina Capacity", new TextField(), null); // Wie weit sich der kanal breiten kann ohne probleme, 1.1 Modifier für unangenehm und so weiter
        addSettingRow(settingsGrid, 10, "Hymen", new TextField(), new Label("Years")); // true/false


        addSettingRow(settingsGrid, 11, "Ass size", new TextField(), new Label("Years")); // Größe der hinterteils, Kein einfluss auf papermodel
        addSettingRow(settingsGrid, 12, "Anus depth", new TextField(), new Label("Years")); // Tiefe nur für spiel benötigt, wie tief der Rectum ist
        addSettingRow(settingsGrid, 13, "Anus Capacity", new TextField(), null); // Wie weit sich der anus breiten kann ohne probleme, 1.1 Modifier für unangenehm und so weiter

    }

    private void buildTailSection(GridPane settingsGrid) {
        addSettingRow(settingsGrid, 1, "Tail Type", new TextField(), new Label("Years")); //Für papermodel
        addSettingRow(settingsGrid, 2, "Tail MODS", new TextField(), new Label("Years")); // Automatisch gefüllt mit typen für späteres spielen (Prehensile und sowas)
        addSettingRow(settingsGrid, 3, "Tail size", new TextField(), new Label("Years")); // länge ist multiplicator für den typen, in maßen
    }

    private void buildExtrasSection(GridPane settingsGrid) {
        //TODO muss noch überlegen wie ich mit Flügel und sowas umgehe.
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

    private static void addSettingRow(GridPane grid, int row, String labelText, Node col2, Node col3) {
        Label label = new Label(labelText);
        label.setMinWidth(Region.USE_PREF_SIZE);

        GridPane.setHgrow(col2, Priority.ALWAYS);
        if (col2 instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);

        grid.add(label, 0, row);
        grid.add(col2, 1, row);
        if (col3 != null) {
            grid.add(col3, 2, row);
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
