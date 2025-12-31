package com.fuchsbau.shorin.RPG.Intro;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.ScenarioDefinition;
import com.fuchsbau.shorin.Engine.RPG.ScenarioLoader;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.RPG.Saveble;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class WorldStartLocationSelector implements Saveble {
    private static final Logger logger = FileLogger.getLogger();
    private static final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();

    private final double DRAG_MARGIN = 200;
    private final double MIN_SCALE = 0.2;
    private final double MAX_SCALE = 8.5;
    private final double HIT_RADIUS = 35;

    private double scale = 0.4;

    private double pressSceneX, pressSceneY;
    private double pressTranslateX, pressTranslateY;

    private Group worldGroup;
    private ImageView mapView;
    private Pane overlayLayer;
    private Pane markerLayer;
    private Pane viewport;
    private VBox legendBox;

    private final Map<String, Node> markerByName = new HashMap<>();
    private ListView<String> scenarioList = new ListView<>();
    private String selectedScenario;

    private ImageView activeOverlayView;

    public Scene create() {
        // Map
        mapView = new ImageView(ImagePreLoader.getCached(ImagePaths.SHORIN_CLEAN_MAP));
        mapView.setPreserveRatio(true);
        mapView.setSmooth(true);

        overlayLayer = new Pane();
        overlayLayer.setManaged(false);
        overlayLayer.setPickOnBounds(false);
        overlayLayer.setMouseTransparent(true);

        markerLayer = new Pane();

        worldGroup = new Group(mapView, overlayLayer, markerLayer);

        // Viewport with clipping
        viewport = new Pane(worldGroup);
        viewport.setPickOnBounds(true);
        viewport.setStyle("-fx-background-color: #7484B1;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());
        viewport.setClip(clip);

        StackPane root = new StackPane();

        // Add markers
        URL url = ScenarioLoader.resolve("Scenarios.json");
        for (ScenarioDefinition definition : ScenarioLoader.load(url)) {
            addScenarioMarker(definition.name(), ImagePreLoader.getCached(ImagePaths.valueOf(definition.icon())), definition.x(), definition.y());
        }


        // UI right panel
        VBox rightPanel = buildRightPanel();
        rightPanel.setMinWidth(160);
        rightPanel.maxWidthProperty().bind(
                root.widthProperty().multiply(0.12)
        );
        StackPane.setAlignment(rightPanel, Pos.TOP_RIGHT);

        // Map
        BorderPane mapLayer = new BorderPane();
        mapLayer.setCenter(viewport);

        root.getChildren().addAll(mapLayer, rightPanel);

        legendBox = buildLegend();
        StackPane.setAlignment(legendBox, Pos.BOTTOM_LEFT);
        StackPane.setMargin(legendBox, new Insets(10));
        root.getChildren().add(legendBox);

        root.setBackground(Background.EMPTY);
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);

        String cssUrl = CSSLoader.resolveUserOrBackupCSS();
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl);
        } else {
            logger.warning("CSS not found: css/main.css");
        }

        installPanAndZoom(viewport);
        viewport.widthProperty().addListener((o, a, b) -> clampWorld());
        viewport.heightProperty().addListener((o, a, b) -> clampWorld());


        return scene;
    }

    private VBox buildLegend() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));
        box.setMaxWidth(220);

        box.setBackground(new Background(new BackgroundFill(
                Color.web("#0c0c12", 0.55),
                new CornerRadii(8),
                Insets.EMPTY
        )));

        box.setMouseTransparent(true); // nur Anzeige
        box.setVisible(false);
        box.setManaged(false);
        return box;
    }

    private void setLegend(List<Pair<String, Color>> items) {
        legendBox.getChildren().clear();

        for (var it : items) {
            Rectangle swatch = new Rectangle(12, 12, it.getValue());
            Label label = new Label(it.getKey());
            label.setTextFill(Color.WHITE);

            HBox row = new HBox(8, swatch, label);
            legendBox.getChildren().add(row);
        }

        boolean visible = !items.isEmpty();
        legendBox.setVisible(visible);
        legendBox.setManaged(visible);
    }

    private VBox buildRightPanel() {
        Label title = SceneBuilder.createHeaderLabel("Select Scenario");

        // Filters
        ToggleButton tClean = sceneBuilder.createMenuTobbleButton("Clean");
        ToggleButton tCultures = sceneBuilder.createMenuTobbleButton("Cultures");
        ToggleButton tKingdoms = sceneBuilder.createMenuTobbleButton("Kingdoms");

        Slider opacitySlider = new Slider(0.05, 1.0, 0.55);
        opacitySlider.setShowTickLabels(false);
        opacitySlider.setShowTickMarks(false);
        opacitySlider.setMaxWidth(Double.MAX_VALUE);

        opacitySlider.valueProperty().addListener((o, a, b) -> {
            if (activeOverlayView != null) {
                activeOverlayView.setOpacity(b.doubleValue());
            }
        });

        tClean.setOnMouseClicked(e -> {
            clearOverlay();
        });

        tCultures.setOnMouseClicked(e -> {
            clearOverlay();
            showImageOverlay(
                    ImagePreLoader.getCached(ImagePaths.MAP_OVERLAY_CULTURES),
                    opacitySlider.getValue()
            );

            setLegend(List.of(
                    new Pair<>("Brutahk", Color.web("#8b0000")),
                    new Pair<>("Coralors", Color.web("#30d5c8")),
                    new Pair<>("Feylin", Color.web("#dfcfa3")),
                    new Pair<>("Goblin", Color.web("#32ff4a")),
                    new Pair<>("Gnomes", Color.web("#f4d03f")),
                    new Pair<>("Elfen", Color.web("#2ecc71")),
                    new Pair<>("Fennari", Color.web("#ff9933")),
                    new Pair<>("Tarpan", Color.web("#a06f3f")),
                    new Pair<>("Human", Color.web("#6bb7ff")),
                    new Pair<>("Hyrax", Color.web("#8b5a2b")),
                    new Pair<>("Kataru", Color.web("#cfa44a")),
                    new Pair<>("Kobold", Color.web("#555555")),
                    new Pair<>("Merman", Color.web("#003f7f")),
                    new Pair<>("Nagalith", Color.web("#5a2a82")),
                    new Pair<>("Rodini", Color.web("#b2905f")),
                    new Pair<>("Tengu", Color.web("#d9d9d9")),
                    new Pair<>("Veskral", Color.web("#3a2e2e"))
            ));
        });

        tKingdoms.setOnMouseClicked(e -> {
            clearOverlay();
        });


        VBox filters = new VBox(8, tClean, tCultures, tKingdoms, opacitySlider);

        // Scenario list
        scenarioList.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
            if (newV == null) return;
            selectScenario(newV);
        });


        VBox box = new VBox(15, title, filters, scenarioList);
        box.setBackground(GameOptions.rowHintergrundTrans40);
        box.setPadding(new Insets(5));
        return box;
    }

    private void selectScenario(String name) {
        if (selectedScenario != null) {
            Node oldMarker = markerByName.get(selectedScenario);
            if (oldMarker != null) {
                oldMarker.setScaleX(1.0);
                oldMarker.setScaleY(1.0);
                oldMarker.setEffect(null);
            }
        }

        selectedScenario = name;

        Node marker = markerByName.get(name);
        if (marker != null) {
            marker.setScaleX(1.35);
            marker.setScaleY(1.35);
            marker.setEffect(new DropShadow(12, Color.web("#a0a0ff")));
        }
    }

    private void addScenarioMarker(String name, Image image, double mapX, double mapY) {
        ImageView marker = new ImageView(
                image
        );
        marker.setPreserveRatio(true);
        marker.setFitWidth(20);

        marker.setTranslateX(mapX - marker.getFitWidth() / 2.0);
        marker.setTranslateY(mapY - marker.getFitHeight() / 2.0);

        Circle hitArea = new Circle(HIT_RADIUS);
        hitArea.setFill(Color.TRANSPARENT);
        hitArea.setTranslateX(mapX);
        hitArea.setTranslateY(mapY);

        Tooltip tip = new Tooltip(name);
        tip.setShowDelay(Duration.millis(60));
        Tooltip.install(hitArea, new Tooltip(name));

        hitArea.setOnMouseEntered(e -> {
            if (name.equals(selectedScenario)) return;
            marker.setScaleX(1.2);
            marker.setScaleY(1.2);
        });
        hitArea.setOnMouseExited(e -> {
            if (name.equals(selectedScenario)) return;
            marker.setScaleX(1.0);
            marker.setScaleY(1.0);
        });

        scenarioList.getItems().add(name);
        hitArea.setOnMouseClicked(e -> scenarioList.getSelectionModel().select(name));

        markerLayer.getChildren().addAll(marker, hitArea);
        markerByName.put(name, marker);
    }

    private void installPanAndZoom(Pane target) {
        viewport.setOnMouseClicked(e -> {
            Point2D scene = new Point2D(e.getSceneX(), e.getSceneY());
            Point2D inViewport = viewport.sceneToLocal(scene);
            Point2D inWorld = worldGroup.parentToLocal(inViewport);

            logger.info("Clicked: World X: " + inWorld.getX() + " | World Y: " + inWorld.getY());
        });

        target.setOnMousePressed(e -> {
            if (!e.isPrimaryButtonDown()) return;
            pressSceneX = e.getSceneX();
            pressSceneY = e.getSceneY();
            pressTranslateX = worldGroup.getTranslateX();
            pressTranslateY = worldGroup.getTranslateY();
        });

        target.setOnMouseDragged(e -> {
            if (!e.isPrimaryButtonDown()) return;
            double dx = e.getSceneX() - pressSceneX;
            double dy = e.getSceneY() - pressSceneY;

            worldGroup.setTranslateX(pressTranslateX + dx);
            worldGroup.setTranslateY(pressTranslateY + dy);

            clampWorld();
        });

        target.setOnScroll(e -> {
            double delta = e.getDeltaY();
            double factor = delta > 0 ? 1.08 : 1.0 / 1.08;

            double newScale = clamp(scale * factor, MIN_SCALE, MAX_SCALE);

            // Zoom around mouse position
            Point2D mouseInParent = target.sceneToLocal(e.getSceneX(), e.getSceneY());
            Point2D mouseInWorldBefore = worldGroup.parentToLocal(mouseInParent);

            scale = newScale;
            worldGroup.setScaleX(scale);
            worldGroup.setScaleY(scale);

            Point2D mouseInWorldAfter = worldGroup.parentToLocal(mouseInParent);
            Point2D diff = mouseInWorldAfter.subtract(mouseInWorldBefore);

            worldGroup.setTranslateX(worldGroup.getTranslateX() + diff.getX() * scale);
            worldGroup.setTranslateY(worldGroup.getTranslateY() + diff.getY() * scale);

            clampWorld();
            e.consume();
        });
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private void clampWorld() {
        if (mapView.getImage() == null) return;

        double iw = mapView.getImage().getWidth();
        double ih = mapView.getImage().getHeight();

        double scaledW = iw * scale;
        double scaledH = ih * scale;

        double viewW = viewport.getWidth();
        double viewH = viewport.getHeight();

        // X
        if (scaledW <= viewW - 2 * DRAG_MARGIN) {
            worldGroup.setTranslateX(viewW / 2.0);
        } else {
            double minX = (viewW - DRAG_MARGIN) - (scaledW / 2.0);
            double maxX = (DRAG_MARGIN) + (scaledW / 2.0);
            worldGroup.setTranslateX(clamp(worldGroup.getTranslateX(), minX, maxX));
        }

        // Y
        if (scaledH <= viewH - 2 * DRAG_MARGIN) {
            worldGroup.setTranslateY(viewH / 2.0);
        } else {
            double minY = (viewH - DRAG_MARGIN) - (scaledH / 2.0);
            double maxY = (DRAG_MARGIN) + (scaledH / 2.0);
            worldGroup.setTranslateY(clamp(worldGroup.getTranslateY(), minY, maxY));
        }
    }

    private void showImageOverlay(Image img, double opacity) {
        ImageView iv = new ImageView(img);
        iv.setOpacity(opacity);

        double w = img.getWidth();
        double h = img.getHeight();

        iv.setTranslateX(-w / 2.0);
        iv.setTranslateY(-h / 2.0);

        iv.setMouseTransparent(true);
        overlayLayer.getChildren().setAll(iv);
        activeOverlayView = iv;
    }

    private void clearOverlay() {
        overlayLayer.getChildren().clear();
        activeOverlayView = null;
        setLegend(Collections.emptyList());
    }

    @Override
    public Scene getScene(int stage) {
        return create();
    }

    @Override
    public void reset() {

    }

    /***
     * Muss nach dem Setzen der Scene neu kalkuliert werden, da die Werte vorher nicht vorhanden sind.
     */
    public void recalcPositions() {
        var img = mapView.getImage();
        if (img == null) return;

        mapView.setTranslateX(-img.getWidth() / 2.0);
        mapView.setTranslateY(-img.getHeight() / 2.0);

        double vw = viewport.getWidth();
        double vh = viewport.getHeight();

        worldGroup.setTranslateX(vw / 2.0);
        worldGroup.setTranslateY(vh / 2.0);

        worldGroup.setScaleX(scale);
        worldGroup.setScaleY(scale);
    }
}
