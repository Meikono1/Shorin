package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.RPG.DisplayText.Styler.TextStyler;
import com.fuchsbau.shorin.Engine.Logger.FileLogger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.logging.Logger;

/**
 * Zeigt TextSegmente als Chat-ähnliches Layout an.
 * Spieler linksbündig, NPC rechtsbündig, Narration zentriert.
 * Max 150 Nodes — älteste werden entfernt wenn Limit erreicht.
 * clearDisplay() fügt Trenner ein und scrollt ans Ende.
 */
public class TextAdventureDisplayView implements Renderable, Hideable {

    private static final int MAX_NODES = 150;
    private static final double PORTRAIT_SIZE = 48;

    private final Logger logger = FileLogger.getLogger();

    private TextDisplayConfig config = TextDisplayConfig.defaults();

    private BorderPane root;
    private VBox bubbleList;   // alle Segmente landen hier
    private ScrollPane scroll;

    private Region currentSpacer;
    private int sceneStartIndex;

    private Timeline typewriter;
    private boolean skipRequested = false;
    private Runnable onFinished;

    @Override
    public Node build() {
        bubbleList = new VBox(8);
        bubbleList.setPadding(new Insets(12));
        bubbleList.setFillWidth(true);
        bubbleList.setMinHeight(Region.USE_COMPUTED_SIZE);
        bubbleList.setPrefHeight(Region.USE_COMPUTED_SIZE);
        bubbleList.setAlignment(Pos.TOP_LEFT);

        scroll = new ScrollPane(bubbleList);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("scrollPane");
        scroll.setOnMouseClicked(e -> requestSkip());

        scroll.sceneProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            scroll.heightProperty().addListener((o, oldH, newH) -> updateSpacer());
        });

        root = new BorderPane(scroll);
        root.setBackground(Background.EMPTY);

        // Erste Szene startet mit Spacer
        addNewSceneSpacer();

        logger.fine("TextAdventureDisplayView gebaut");
        return root;
    }

    private void addNewSceneSpacer() {
        sceneStartIndex = bubbleList.getChildren().size();
        currentSpacer = new Region();
        currentSpacer.setPrefHeight(scroll.getHeight());
        bubbleList.getChildren().add(currentSpacer);
    }

    private void updateSpacer() {
        Platform.runLater(() -> {
            if (currentSpacer == null || scroll == null) return;

            double sceneContentHeight = 0;
            for (int i = sceneStartIndex; i < bubbleList.getChildren().size(); i++) {
                Node node = bubbleList.getChildren().get(i);
                if (node == currentSpacer) continue;
                sceneContentHeight += node.getBoundsInParent().getHeight() + 25;
            }

            double remaining = scroll.getHeight() - sceneContentHeight;
            currentSpacer.setPrefHeight(Math.max(100, remaining));
            scrollToBottom();
            logger.finest("Spacer → " + currentSpacer.getPrefHeight()
                    + " | sceneContent → " + sceneContentHeight);
        });
    }

    // Neue Szenenkonfiguration
    public void applyConfig(TextDisplayConfig cfg) {
        this.config = cfg;
        applySceneImage(cfg);
        logger.fine("Config | speed=" + cfg.typewriterSpeed + " imageMode=" + cfg.imageMode);
    }

    // Segment anzeigen — Typewriter startet sofort
    public void show(TextSegment segment, Runnable onDone) {
        this.onFinished = onDone;
        this.skipRequested = false;

        stopTypewriter();
        enforceNodeLimit();

        Node bubble = buildBubble(segment);
        int insertIndex = bubbleList.getChildren().indexOf(currentSpacer);
        bubbleList.getChildren().add(insertIndex, bubble);

        updateSpacer();
        scrollToBottom();

        startTypewriter(segment, bubble);
        logger.fine("Segment | " + segment.style + " | " + segment.text.length() + " Zeichen");
    }

    // Trenner einfügen + ans Ende scrollen — altes bleibt lesbar
    public void clearDisplay() {
        stopTypewriter();

        if (currentSpacer != null) {
            bubbleList.getChildren().remove(currentSpacer);
            currentSpacer = null;
        }

        Separator sep = new Separator();
        sep.setPadding(new Insets(8, 0, 8, 0));
        sep.setStyle("-fx-background-color: rgba(160,160,255,0.2);");
        bubbleList.getChildren().add(sep);

        addNewSceneSpacer();
        updateSpacer();
        scrollToBottom();
        logger.fine("Display getrennt | " + bubbleList.getChildren().size() + " Nodes");
    }

    public void requestSkip() {
        skipRequested = true;
    }

    public void setOnFinished(Runnable callback) {
        this.onFinished = callback;
    }

    // Bubble je nach Style bauen — nur Platzhalter, Text kommt per Typewriter
    private Node buildBubble(TextSegment segment) {
        return switch (segment.style) {
            case NARRATION -> buildNarrationBubble();
            case DIALOG -> buildDialogBubble(segment);
            case SYSTEM -> buildSystemBubble();
        };
    }

    // NARRATION — volle Breite, kursiv, zentriert, kein Rahmen
    private VBox buildNarrationBubble() {
        TextFlow flow = new TextFlow();
        flow.setTextAlignment(TextAlignment.CENTER);
        flow.setPadding(new Insets(8, 24, 8, 24));
        flow.setStyle("-fx-font-style: italic;");
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setUserData("flow"); // Typewriter findet ihn

        VBox box = new VBox(flow);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    // DIALOG — Portrait + Rahmen-Box, links oder rechts
    private HBox buildDialogBubble(TextSegment segment) {
        TextFlow flow = new TextFlow();
        flow.setPadding(new Insets(8, 12, 8, 12));
        flow.setMaxWidth(500);
        flow.setUserData("flow");

        // Name-Label
        Text nameTag = new Text(
                segment.speakerName != null ? segment.speakerName + "\n" : "");
        nameTag.setFill(segment.speakerLeft != null && segment.speakerLeft
                ? Color.rgb(160, 220, 160)   // Spieler — grünlich
                : Color.rgb(180, 180, 255));  // NPC — bläulich
        nameTag.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        flow.getChildren().add(nameTag);

        VBox textBox = new VBox(flow);
        textBox.setBackground(new Background(new BackgroundFill(
                segment.speakerLeft != null && segment.speakerLeft
                        ? Color.rgb(30, 50, 30)    // Spieler
                        : Color.rgb(25, 25, 55),   // NPC
                new CornerRadii(6), Insets.EMPTY)));
        textBox.setBorder(new Border(new BorderStroke(
                segment.speakerLeft != null && segment.speakerLeft
                        ? Color.rgb(80, 140, 80)
                        : Color.rgb(80, 80, 160),
                BorderStrokeStyle.SOLID, new CornerRadii(6), BorderWidths.DEFAULT)));

        // Portrait
        Node portrait = buildPortrait(segment.speakerImage);

        HBox row = new HBox(8);
        row.setPadding(new Insets(4, 8, 4, 8));

        boolean left = segment.speakerLeft == null || segment.speakerLeft;
        if (left) {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(portrait, textBox);
        } else {
            row.setAlignment(Pos.CENTER_RIGHT);
            row.getChildren().addAll(textBox, portrait);
        }

        return row;
    }

    // SYSTEM — volle Breite, gedimmt, klein
    private VBox buildSystemBubble() {
        TextFlow flow = new TextFlow();
        flow.setTextAlignment(TextAlignment.CENTER);
        flow.setPadding(new Insets(4, 24, 4, 24));
        flow.setStyle("-fx-font-size: 11px;");
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setUserData("flow");

        VBox box = new VBox(flow);
        box.setAlignment(Pos.CENTER);
        box.setOpacity(0.6);
        return box;
    }

    // Portrait aus ImagePaths oder Platzhalter
    private Node buildPortrait(ImagePaths path) {
        ImageView iv;
        if (path != null) {
            iv = new ImageView(ImagePreLoader.getCached(path));
        } else {
            iv = new ImageView();
        }
        iv.setFitWidth(PORTRAIT_SIZE);
        iv.setFitHeight(PORTRAIT_SIZE);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.2, 0, 2);");

        // Platzhalter-Hintergrund wenn kein Bild
        StackPane frame = new StackPane(iv);
        frame.setPrefSize(PORTRAIT_SIZE, PORTRAIT_SIZE);
        frame.setMaxSize(PORTRAIT_SIZE, PORTRAIT_SIZE);
        frame.setBackground(new Background(new BackgroundFill(
                Color.rgb(30, 30, 50), new CornerRadii(4), Insets.EMPTY)));
        return frame;
    }

    // Typewriter — befüllt den TextFlow im Bubble Zeichen für Zeichen
    private void startTypewriter(TextSegment segment, Node bubble) {
        TextFlow flow = findFlow(bubble);
        if (flow == null) {
            logger.warning("Kein TextFlow in Bubble gefunden");
            if (onFinished != null) onFinished.run();
            return;
        }

        String full = segment.text;
        double msPerChar = 1000.0 / config.typewriterSpeed;

        Text animated = new Text();
        animated.setFill(Color.rgb(220, 220, 220));
        if (segment.style == TextSegment.Style.NARRATION) {
            animated.setStyle("-fx-font-style: italic;");
        }
        flow.getChildren().add(animated);

        int[] idx = {0};

        typewriter = new Timeline(new KeyFrame(Duration.millis(msPerChar), e -> {
            if (skipRequested || idx[0] >= full.length()) {
                // Skip → ganzen Text sofort mit Keywords stylen
                flow.getChildren().remove(animated);
                TextStyler.addRestyledText(flow, full);
                stopTypewriter();
                updateSpacer();
                scrollToBottom();
                if (onFinished != null) onFinished.run();
                return;
            }
            animated.setText(full.substring(0, ++idx[0]));
        }));

        typewriter.setCycleCount(full.length() + 1);
        typewriter.play();
    }

    // TextFlow aus Bubble-Node finden via UserData
    private TextFlow findFlow(Node bubble) {
        if (bubble instanceof VBox vb) {
            for (Node child : vb.getChildren()) {
                if (child instanceof TextFlow tf && "flow".equals(tf.getUserData())) return tf;
                if (child instanceof VBox inner) {
                    TextFlow found = findFlow(inner);
                    if (found != null) return found;
                }
            }
        }
        if (bubble instanceof HBox hb) {
            for (Node child : hb.getChildren()) {
                if (child instanceof VBox vb) {
                    TextFlow found = findFlow(vb);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    // Älteste Nodes entfernen wenn Limit erreicht
    private void enforceNodeLimit() {
        while (bubbleList.getChildren().size() > MAX_NODES + 1) {
            Node first = bubbleList.getChildren().getFirst();
            if (first == currentSpacer) break;
            bubbleList.getChildren().removeFirst();
            logger.finest("Node entfernt | limit=" + MAX_NODES);
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    private void stopTypewriter() {
        if (typewriter != null) {
            typewriter.stop();
            typewriter = null;
        }
    }

    private void applySceneImage(TextDisplayConfig cfg) {
        root.getChildren().removeIf(n -> n != scroll);

        if (cfg.sceneImage == null || cfg.imageMode == TextDisplayConfig.ImageMode.NONE) return;

        switch (cfg.imageMode) {
            case BACKGROUND -> {
                ImageView bg = new ImageView(ImagePreLoader.getCached(cfg.sceneImage));
                bg.setPreserveRatio(true);
                bg.setSmooth(true);

                // Breite bindet sich an root — Höhe skaliert proportional
                bg.fitWidthProperty().bind(root.widthProperty());

                ColorAdjust dim = new ColorAdjust();
                dim.setBrightness(-0.7);
                bg.setEffect(dim);

                StackPane.setAlignment(bg, Pos.TOP_CENTER);
                root.getChildren().addFirst(bg);
                logger.fine("Hintergrundbild | " + cfg.sceneImage);
            }
            case ABOVE -> {
                logger.fine("@TODO ABOVE Szenenbild");
            }
            default -> {
            }
        }
    }

    @Override
    public void refresh() {
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
}