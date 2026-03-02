package com.fuchsbau.shorin.Engine.RPG;

import com.fuchsbau.shorin.Engine.Map.Core.GameMap;
import com.fuchsbau.shorin.Engine.Map.Core.LightingSystem;
import com.fuchsbau.shorin.Engine.Map.Core.MapRenderer;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.AktionBar.ActionMenu;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.Places.Place;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.logging.Logger;

public class PlayerScreen implements Saveble {

    private static final double LEFT_WIDTH = 350;
    private static final double RIGHT_WIDTH = 350;

    private final SceneBuilder sb = SceneBuilder.getSceneBuilder();
    private final Logger logger = FileLogger.getLogger();

    private Scene scene;

    // Panels
    public VBox leftPanel;
    public VBox centerContent;
    public TextFlow storyFlow;
    public VBox actionMenu;
    public VBox rightPanel;

    // Aufbau
    private void build() {
        logger.info("Baue PlayerScreen");

        BorderPane root = new BorderPane();
        root.setBackground(GameOptions.hintergrund);

        root.setLeft(buildLeft());
        root.setCenter(buildCenter());
        root.setRight(buildRight());

        // ESC → Options
        scene = new Scene(root, GameOptions.width, GameOptions.height);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> Main.getStage().setScene(
                        Game.getInstance().optionen.getScene(1));
                // WASD → später für Map-Navigation
                case W -> onMove(0, -1);
                case S -> onMove(0, 1);
                case A -> onMove(-1, 0);
                case D -> onMove(1, 0);
            }
        });

        String css = CSSLoader.resolveUserOrBackupCSS();
        if (css != null) scene.getStylesheets().add(css);
    }

    // LEFT PANEL  –  Charakter / Stats / Tabs
    // LEFT PANEL
    private VBox buildLeft() {
        leftPanel = new VBox(6);
        leftPanel.setPrefWidth(LEFT_WIDTH);
        leftPanel.setMaxWidth(LEFT_WIDTH);
        leftPanel.setPadding(new Insets(8));
        leftPanel.setBackground(GameOptions.rowHintergrundTrans40);

        // CHAR SWITCHER
        HBox charSwitcher = new HBox(8);
        charSwitcher.setAlignment(Pos.CENTER);
        charSwitcher.setPadding(new Insets(4));
        charSwitcher.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 40, 70), new CornerRadii(4), Insets.EMPTY)));

        Button prevChar = new Button("◀");
        prevChar.getStyleClass().add("menu-button");

        Label charName = sb.makeWhiteLabel(Game.getInstance().spieler.getText().getText());
        charName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        charName.setMaxWidth(Double.MAX_VALUE);
        charName.setAlignment(Pos.CENTER);
        HBox.setHgrow(charName, Priority.ALWAYS);

        Button nextChar = new Button("▶");
        nextChar.getStyleClass().add("menu-button");

        charSwitcher.getChildren().addAll(prevChar, charName, nextChar);

        // QUICK STATS  –  HP / AC / Shield / Gold / Heropoints
        VBox quickStats = new VBox(4);
        quickStats.setPadding(new Insets(6));
        quickStats.setBackground(new Background(new BackgroundFill(
                Color.rgb(30, 50, 40), new CornerRadii(4), Insets.EMPTY)));

        Label statsHeader = sb.makeWhiteLabel("── Status ──");
        statsHeader.setMaxWidth(Double.MAX_VALUE);
        statsHeader.setAlignment(Pos.CENTER);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(8);
        statsGrid.setVgap(3);

        statsGrid.add(statLabel("HP"), 0, 0);
        statsGrid.add(statValue("__ / __"), 1, 0);
        statsGrid.add(statLabel("AC"), 0, 1);
        statsGrid.add(statValue("__"), 1, 1);
        statsGrid.add(statLabel("Shield"), 0, 2);
        statsGrid.add(statValue("__"), 1, 2);
        statsGrid.add(statLabel("Gold"), 0, 3);
        statsGrid.add(statValue("__"), 1, 3);
        statsGrid.add(statLabel("Hero"), 0, 4);
        statsGrid.add(statValue("__ / 3"), 1, 4);

        // CONDITIONS
        Label condHeader = sb.makeWhiteLabel("Conditions:");
        condHeader.setStyle("-fx-font-size: 11px;");
        Label condValue = new Label("keine");
        condValue.setTextFill(Color.GRAY);
        condValue.setStyle("-fx-font-size: 11px;");
        condValue.setWrapText(true);

        quickStats.getChildren().addAll(statsHeader, statsGrid, new Separator(), condHeader, condValue);

        // 4-WEGE KREUZ  –  Inv / Char / Spells / Feats
        GridPane cross = new GridPane();
        cross.setHgap(4);
        cross.setVgap(4);
        cross.setPadding(new Insets(4));
        cross.setAlignment(Pos.CENTER);

        Button invBtn = crossBtn("🎒 Inv", Color.rgb(60, 40, 20));
        Button charBtn = crossBtn("👤 Char", Color.rgb(20, 40, 60));
        Button spellBtn = crossBtn("✨ Spells", Color.rgb(40, 20, 60));
        Button featBtn = crossBtn("⭐ Feats", Color.rgb(20, 55, 35));

        // TODO: je einen neuen Screen öffnen

        cross.add(charBtn, 0, 0);
        cross.add(spellBtn, 1, 0);
        cross.add(invBtn, 0, 1);
        cross.add(featBtn, 1, 1);

        // alle Buttons gleich groß
        for (var node : cross.getChildren()) {
            GridPane.setFillWidth(node, true);
            GridPane.setFillHeight(node, true);
        }

        // PAPERDOLL BUTTON
        VBox paperdollArea = new VBox(4);
        paperdollArea.setPadding(new Insets(6));
        paperdollArea.setBackground(new Background(new BackgroundFill(
                Color.rgb(50, 30, 50), new CornerRadii(4), Insets.EMPTY)));

        Button paperdoll = sb.createMenuButton("⧉ Paperdoll");
        paperdoll.setMaxWidth(Double.MAX_VALUE);
        // TODO: PaperdollWindow.toggle();

        paperdollArea.getChildren().add(paperdoll);

        leftPanel.getChildren().addAll(charSwitcher, quickStats, cross, paperdollArea);
        VBox.setVgrow(quickStats, Priority.ALWAYS);
        return leftPanel;
    }

    // HILFSMETHODEN LEFT
    private Label statLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.LIGHTGRAY);
        l.setStyle("-fx-font-size: 11px;");
        return l;
    }

    private Label statValue(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private Button crossBtn(String label, Color bg) {
        Button btn = new Button(label);
        btn.setPrefSize((LEFT_WIDTH - 28) / 2, 60);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(4), Insets.EMPTY)));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-font-size: 12px;");
        return btn;
    }

    // CENTER PANEL
    private VBox buildCenter() {
        // Story-Textbereich (scrollbar)
        storyFlow = sb.mainFlow();
        storyFlow.setPadding(new Insets(16));
        storyFlow.setBackground(Background.EMPTY);

        // Platzhalter-Text
        storyFlow.getChildren().add(sb.makeText(
                "[ Hier erscheint die Story, Beschreibungen und Dialoge... ]\n\n" +
                        "Du stehst am Hafendeck. Das Holz unter deinen Füßen knarrt im " +
                        "Rhythmus der Wellen. Der Geruch von Salz und Teer liegt in der Luft."
        ));

        ScrollPane storyScroll = new ScrollPane(storyFlow);
        storyScroll.setFitToWidth(true);
        storyScroll.setBackground(Background.EMPTY);
        storyScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        storyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(storyScroll, Priority.ALWAYS);

        // Aktionsmenü (unterhalb des Textes)
        ActionMenu actionMenuObj = new ActionMenu();
        actionMenuObj.setMode(ActionMenu.Mode.TRAVEL);
        actionMenu = actionMenuObj.getRoot();

        centerContent = new VBox(storyScroll, actionMenu);
        centerContent.setBackground(GameOptions.hintergrund);
        centerContent.setPadding(new Insets(0, 8, 8, 8));
        VBox.setVgrow(storyScroll, Priority.ALWAYS);

        return centerContent;
    }

    // RIGHT PANEL  –  Map / Navigation / Zeitsteuerung
    private VBox buildRight() {
        rightPanel = new VBox(10);
        rightPanel.setPrefWidth(RIGHT_WIDTH);
        rightPanel.setMaxWidth(RIGHT_WIDTH);
        rightPanel.setPadding(new Insets(12));
        rightPanel.setBackground(GameOptions.rowHintergrundTrans40);

        // MINI-MAP via MapRenderer
        GameMap miniMap = new GameMap(20, 20);
        LightingSystem miniLight = new LightingSystem();
        MapRenderer miniRenderer = new MapRenderer(miniMap, miniLight);
        miniRenderer.setZoom(0.3);

        Canvas miniCanvas = miniRenderer.getCanvas();
        miniCanvas.setWidth(RIGHT_WIDTH - 24);
        miniCanvas.setHeight(RIGHT_WIDTH - 24);

        miniCanvas.widthProperty().addListener((o, ov, nv) -> miniRenderer.renderWorldmap());
        miniCanvas.heightProperty().addListener((o, ov, nv) -> miniRenderer.renderWorldmap());
        miniRenderer.renderWorldmap();

        StackPane mapBox = new StackPane(miniCanvas);
        mapBox.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 35, 50), new CornerRadii(4), Insets.EMPTY)));

        // NAVIGATION
        Label navHeader = sb.makeWhiteLabel("Navigation");
        navHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox navTree = new VBox(2);
        navTree.setPadding(new Insets(4));

        // Alle Top-Level Places aus Game
        for (Place place : getTopLevelPlaces()) {
            navTree.getChildren().add(buildNavEntry(place, 0));
        }

        ScrollPane navScroll = new ScrollPane(navTree);
        navScroll.setFitToHeight(true);
        navScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // --- Zeitsteuerung ---
        Label timeHeader = sb.makeWhiteLabel("Zeit");
        timeHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label currentTime = sb.makeWhiteLabel("Tag 1  –  06:00");
        currentTime.setStyle("-fx-font-size: 12px;");

        HBox speedRow = new HBox(4);
        speedRow.setAlignment(Pos.CENTER_LEFT);
        for (String speed : new String[]{"⏸", "▶", "▶▶", "▶▶▶"}) {
            Button btn = new Button(speed);
            btn.getStyleClass().add("menu-button");
            btn.setPrefWidth(52);
            // TODO: GameLoop-Geschwindigkeit setzen
            speedRow.getChildren().add(btn);
        }

        // --- Reise-Info ---
        Label travelHeader = sb.makeWhiteLabel("Reise");
        travelHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label travelInfo = sb.makeWhiteLabel("Kein Ziel gewählt.");
        travelInfo.setWrapText(true);
        travelInfo.setStyle("-fx-font-size: 11px;");

        rightPanel.getChildren().addAll(
                mapBox,
                new Separator(),
                navHeader, navTree,
                new Separator(),
                timeHeader, currentTime, speedRow,
                new Separator(),
                travelHeader, travelInfo
        );

        return rightPanel;
    }

    // Hilfsmethoden
    private VBox placeholderSection(String title, String... lines) {
        VBox box = new VBox(4);
        Label header = sb.makeWhiteLabel(title);
        header.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(header);
        for (String line : lines) {
            Label l = new Label(line);
            l.setTextFill(Color.LIGHTGRAY);
            l.setStyle("-fx-font-size: 11px;");
            box.getChildren().add(l);
        }
        return box;
    }

    /**
     * Kleiner Navigations-Button
     */
    private Button navBtn(String symbol) {
        Button btn = new Button(symbol);
        btn.setPrefSize(40, 40);
        btn.getStyleClass().add("menu-button");
        return btn;
    }

    /**
     * Wird aufgerufen bei WASD oder Nav-Klick. Koordinaten in Tile-Schritten.
     */
    private void onMove(int dx, int dy) {
        // TODO: GameLoop / Map-Navigation ansteuern
        logger.fine("Move: dx=" + dx + " dy=" + dy);
    }

    private List<Place> getTopLevelPlaces() {
        // TODO: später aus einer zentralen PlaceRegistry laden
        return List.of(
                Game.getInstance().whitebridge,
                Game.getInstance().sudbury,
                Game.getInstance().unbridledland,
                Game.getInstance().shallowmill
        );
    }

    private VBox buildNavEntry(Place place, int depth) {
        VBox entry = new VBox(2);

        HBox row = new HBox(4);
        row.setPadding(new Insets(2, 2, 2, 8 + depth * 12));
        row.setBackground(new Background(new BackgroundFill(
                depth == 0 ? Color.rgb(30, 45, 60) : Color.rgb(20, 30, 45),
                new CornerRadii(3), Insets.EMPTY)));

        Label arrow = sb.makeWhiteLabel(place.getSubPlaces().isEmpty() ? "  " : "▶");
        arrow.setStyle("-fx-font-size: 10px;");
        Label name = sb.makeWhiteLabel(place.getName());
        name.setStyle("-fx-font-size: 11px;");

        row.getChildren().addAll(arrow, name);
        entry.getChildren().add(row);

        // Unterliste – ein/ausklappbar
        VBox children = new VBox(2);
        children.setVisible(false);
        children.setManaged(false);
        for (Place sub : place.getSubPlaces()) {
            children.getChildren().add(buildNavEntry(sub, depth + 1));
        }

        row.setOnMouseClicked(e -> {
            boolean open = children.isVisible();
            children.setVisible(!open);
            children.setManaged(!open);
            arrow.setText(open ? "▶" : "▼");
        });

        entry.getChildren().add(children);
        return entry;
    }

    // Saveble Interface
    @Override
    public Scene getScene(int stage) {
        build();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}