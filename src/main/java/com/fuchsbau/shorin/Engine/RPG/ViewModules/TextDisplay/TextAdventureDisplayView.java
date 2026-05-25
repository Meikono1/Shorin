package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.Styler.TextStyler;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.logging.Logger;

/**
 * Zeigt TextSegmente mit Typewriter-Effekt an.
 * Speaker-Bilder links/rechts, optionales Szenenbild.
 * <p>
 * Kein Spielzustand hier — bekommt alles übergeben.
 */
public class TextAdventureDisplayView implements Renderable, Hideable {

    private final Logger logger = FileLogger.getLogger();

    private TextDisplayConfig config = TextDisplayConfig.defaults();

    private StackPane root;
    private BorderPane speakerPane;   // links / rechts Speaker-Bilder
    private TextFlow storyFlow;
    private VBox contentBox;
    private ImageView sceneImageView;

    private Timeline typewriter;
    private boolean skipRequested = false;
    private Runnable onFinished;    // Controller-Callback wenn Segment fertig

    @Override
    public Node build() {
        storyFlow = new TextFlow();
        storyFlow.setPadding(new Insets(16));
        storyFlow.setLineSpacing(4);

        ScrollPane scroll = new ScrollPane(storyFlow);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        sceneImageView = new ImageView();
        sceneImageView.setPreserveRatio(true);
        sceneImageView.setFitWidth(600);
        sceneImageView.setVisible(false);
        sceneImageView.setManaged(false);

        contentBox = new VBox(sceneImageView, scroll);
        contentBox.setBackground(Background.EMPTY);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Speaker-Bilder links/rechts
        speakerPane = new BorderPane(contentBox);
        speakerPane.setBackground(Background.EMPTY);

        root = new StackPane(speakerPane);
        root.setBackground(Background.EMPTY);

        // Klick/Leertaste → Typewriter skippen
        root.setOnMouseClicked(e -> requestSkip());

        logger.fine("TextAdventureDisplayView gebaut");
        return root;
    }

    // Neue Konfiguration laden (vor dem ersten Segment einer Szene)
    public void applyConfig(TextDisplayConfig cfg) {
        this.config = cfg;
        applySceneImage(cfg);
        logger.fine("Config angewendet | speed=" + cfg.typewriterSpeed
                + " | imageMode=" + cfg.imageMode);
    }

    // Segment anzeigen — Typewriter startet sofort
    public void show(TextSegment segment, Runnable onDone) {
        this.onFinished = onDone;
        this.skipRequested = false;

        stopTypewriter();
        applySpeakerImages(segment);
        addSpeakerLabel(segment);
        startTypewriter(segment);

        logger.fine("Segment | style=" + segment.style
                + " | sprecher=" + segment.speakerName
                + " | zeichen=" + segment.text.length());
    }

    // Alle Texte löschen — neue Szene
    public void clear() {
        stopTypewriter();
        storyFlow.getChildren().clear();
        clearSpeakerImages();
        logger.fine("Display geleert");
    }

    // Typewriter überspringen — sofort ganzen Text zeigen
    public void requestSkip() {
        skipRequested = true;
        logger.fine("Skip angefordert");
    }

    // Callback wenn Typewriter fertig
    public void setOnFinished(Runnable callback) {
        this.onFinished = callback;
    }

    // Typewriter via Timeline — nicht AnimationTimer, Text ist nicht frame-gebunden
    private void startTypewriter(TextSegment segment) {
        String full = segment.text;
        double msPerChar = 1000.0 / config.typewriterSpeed;

        // Placeholder-Text der Zeichen für Zeichen befüllt wird
        Text animated = new Text();
        animated.getStyleClass().add(cssForStyle(segment.style));

        // Keywords werden erst nach Typewriter durch TextStyler ersetzt
        // @TODO Keywords während Typewriter erkennen (komplexer, später)
        storyFlow.getChildren().add(animated);

        int[] index = {0};

        typewriter = new Timeline(new KeyFrame(
                Duration.millis(msPerChar),
                e -> {
                    if (skipRequested || index[0] >= full.length()) {
                        // Skip oder fertig — ganzen Text sofort setzen
                        storyFlow.getChildren().remove(animated);
                        TextStyler.addRestyledText(storyFlow, full);
                        storyFlow.getChildren().add(new Text("\n\n"));
                        stopTypewriter();
                        if (onFinished != null) onFinished.run();
                        return;
                    }
                    animated.setText(full.substring(0, ++index[0]));
                }
        ));
        typewriter.setCycleCount(full.length() + 1);
        typewriter.play();
    }

    private void stopTypewriter() {
        if (typewriter != null) {
            typewriter.stop();
            typewriter = null;
        }
    }

    // Szenenbild anwenden
    private void applySceneImage(TextDisplayConfig cfg) {
        if (cfg.sceneImagePath == null || cfg.imageMode == TextDisplayConfig.ImageMode.NONE) {
            sceneImageView.setVisible(false);
            sceneImageView.setManaged(false);
            root.setStyle("");
            return;
        }

        Image img = new Image(cfg.sceneImagePath, true);

        switch (cfg.imageMode) {
            case ABOVE -> {
                sceneImageView.setImage(img);
                sceneImageView.setVisible(true);
                sceneImageView.setManaged(true);
                root.setStyle("");
            }
            case BACKGROUND -> {
                sceneImageView.setVisible(false);
                sceneImageView.setManaged(false);
                // Hintergrundbild via CSS-Inline auf root
                // @TODO BackgroundImage-API nutzen statt CSS-String sobald Pfad-Handling steht
                root.setStyle("-fx-background-image: url('" + cfg.sceneImagePath + "');"
                        + "-fx-background-size: cover; -fx-background-position: center;");
            }
            default -> {
            }
        }

        logger.fine("Szenenbild | mode=" + cfg.imageMode + " | " + cfg.sceneImagePath);
    }

    // Speaker-Bilder links/rechts setzen
    private void applySpeakerImages(TextSegment segment) {
        if (!config.showSpeakerImages || segment.speakerImage == null) return;

        ImageView portrait = new ImageView(new Image(segment.speakerImage, true));
        portrait.setFitWidth(80);
        portrait.setFitHeight(120);
        portrait.setPreserveRatio(true);
        portrait.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0.3, 0, 4);");

        VBox portraitBox = new VBox(portrait);
        portraitBox.setAlignment(Pos.BOTTOM_CENTER);
        portraitBox.setPadding(new Insets(0, 8, 0, 8));

        if (segment.speakerLeft) {
            speakerPane.setLeft(portraitBox);
        } else {
            speakerPane.setRight(portraitBox);
        }
    }

    // Speaker-Label über dem Text
    private void addSpeakerLabel(TextSegment segment) {
        if (segment.speakerName == null) return;

        Text nameTag = new Text(segment.speakerName + ":\n");
        nameTag.setFill(Color.rgb(180, 180, 255));
        nameTag.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        storyFlow.getChildren().add(nameTag);
    }

    private void clearSpeakerImages() {
        speakerPane.setLeft(null);
        speakerPane.setRight(null);
    }

    private String cssForStyle(SegmentStyle style) {
        return switch (style) {
            case NARRATION -> "KEYWORD-base";
            case DIALOG -> "KEYWORD-keyword";
            case SYSTEM -> "KEYWORD-base";  // @TODO eigene CSS-Klasse für System-Messages
        };
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