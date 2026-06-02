package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextAdventure;

import com.fuchsbau.shorin.Engine.Map.MapModel;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.Controls.ButtonFactory;
import com.fuchsbau.shorin.Engine.RPG.Controls.ButtonStyle;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Logger.FileLogger;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.Places.Place;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.logging.Logger;

/**
 * Rechtes Panel — Minimap, Navigation, Zeit, Reise.
 * Minimap-Expand (→ Battlemap) wird als Callback an den Controller delegiert.
 */
public class RightPanelView implements Renderable, Hideable {

    private static final double WIDTH = 350;

    private final SceneBuilder sb = SceneBuilder.getSceneBuilder();
    private final Logger logger = FileLogger.getLogger();
    private final MapModel mapModel;

    // Controller setzt diesen Callback für den Screen-Swap
    private Runnable onMinimapExpand;

    private VBox root;

    public RightPanelView(MapModel mapModel) {
        this.mapModel = mapModel;
    }

    public void setOnMinimapExpand(Runnable callback) {
        this.onMinimapExpand = callback;
    }

    @Override
    public Node build() {
        root = new VBox(10);
        root.setPrefWidth(WIDTH);
        root.setMaxWidth(WIDTH);
        root.setPadding(new Insets(12));
        root.setBackground(GameOptions.rowHintergrundTrans40);

        root.getChildren().addAll(
                buildMinimapSection(),
                new Separator(),
                buildNavSection(),
                new Separator(),
                buildTimeSection(),
                new Separator(),
                buildTravelSection()
        );

        logger.fine("RightPanelView gebaut");
        return root;
    }

    @Override
    public void refresh() {
        // @TODO Zeit-Label aus GameClock neu binden
        logger.fine("RightPanelView refresh");
    }

    @Override
    public void show() {
        root.setVisible(true);
        root.setManaged(true);
    }

    @Override
    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    @Override
    public boolean isVisible() {
        return root != null && root.isVisible();
    }

    // Minimap — Canvas aus MapModel, Expand-Button → Controller
    private Node buildMinimapSection() {
        javafx.scene.canvas.Canvas miniCanvas = mapModel.getRenderer().getCanvas();
        miniCanvas.setWidth(WIDTH - 24);
        miniCanvas.setHeight(WIDTH - 24);
        mapModel.setZoom(0.3);
        mapModel.getRenderer().renderWorldmap();

        miniCanvas.widthProperty().addListener((o, ov, nv) -> mapModel.getRenderer().renderWorldmap());
        miniCanvas.heightProperty().addListener((o, ov, nv) -> mapModel.getRenderer().renderWorldmap());

        // Expand-Button → Screen-Swap, nicht Fullscreen
        Button expandBtn = ButtonFactory.make(ButtonStyle.ICON, "⛶", () -> {
            logger.info("Minimap expand → Battlemap");
            if (onMinimapExpand != null) onMinimapExpand.run();
        });

        StackPane mapBox = new StackPane(miniCanvas, expandBtn);
        mapBox.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 35, 50), new CornerRadii(4), Insets.EMPTY)));
        StackPane.setAlignment(expandBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(expandBtn, new Insets(4));

        return mapBox;
    }

    // Navigation — Place-Baum
    private Node buildNavSection() {
        Label header = sb.makeWhiteLabel("Navigation");
        header.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox navTree = new VBox(2);
        navTree.setPadding(new Insets(4));
        for (Place place : getTopLevelPlaces()) {
            navTree.getChildren().add(buildNavEntry(place, 0));
        }

        ScrollPane scroll = new ScrollPane(navTree);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox section = new VBox(4, header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(section, Priority.ALWAYS);
        return section;
    }

    // Zeit + Speed-Buttons
    private Node buildTimeSection() {
        Label header = sb.makeWhiteLabel("Zeit");
        header.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label currentTime = sb.makeWhiteLabel("Tag 1  –  06:00");
        currentTime.setStyle("-fx-font-size: 12px;");
        // @TODO GameClock-Property binden

        HBox speedRow = new HBox(4);
        speedRow.setAlignment(Pos.CENTER_LEFT);
        for (String speed : new String[]{"⏸", "▶", "▶▶", "▶▶▶"}) {
            Button btn = ButtonFactory.make(ButtonStyle.MENU, speed);
            btn.setPrefWidth(52);
            // @TODO GameLoop-Geschwindigkeit setzen
            speedRow.getChildren().add(btn);
        }

        return new VBox(4, header, currentTime, speedRow);
    }

    // Reise-Info
    private Node buildTravelSection() {
        Label header = sb.makeWhiteLabel("Reise");
        header.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label info = sb.makeWhiteLabel("Kein Ziel gewählt.");
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 11px;");
        // @TODO GameClock.TravelListener binden

        return new VBox(4, header, info);
    }

    // Nav-Baum-Eintrag
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

    private List<Place> getTopLevelPlaces() {
        return List.of(
                Game.getInstance().whitebridge,
                Game.getInstance().sudbury,
                Game.getInstance().unbridledland,
                Game.getInstance().shallowmill
        );
    }
}