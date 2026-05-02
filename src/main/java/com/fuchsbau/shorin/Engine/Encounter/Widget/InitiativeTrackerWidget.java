package com.fuchsbau.shorin.Engine.Encounter.Widget;

import com.fuchsbau.shorin.Engine.Encounter.EncounterState;
import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class InitiativeTrackerWidget implements EncounterWidget {

    private static final Logger logger = FileLogger.getLogger();

    // von EncounterPane nach außen steuerbar
    public final BooleanProperty visible = new SimpleBooleanProperty(false);

    // Größen
    private static final double SLOT_W = 72;
    private static final double SLOT_H = 94;
    private static final double SLOT_W_ACTIVE = 90;
    private static final double SLOT_H_ACTIVE = 110;
    private static final double TOKEN_IMG = 50;
    private static final double TOKEN_IMG_ACT = 64;
    private static final double HP_BAR_H = 5;
    private static final double ACTION_DOT = 7;
    private static final double BUFF_ICON = 13;
    private static final double MAX_SLOTS = 20;

    @Override
    public String getId() {
        return "initiative-tracker";
    }

    @Override
    public Node build(EncounterState state) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("""
                -fx-background-color: rgba(8,8,20,0.85);
                -fx-border-color: #2a2a44;
                -fx-border-width: 0 0 1 0;
                """);

        // row NICHT strecken — nur Inhaltsgröße
        row.setMaxWidth(Double.MAX_VALUE); // temporär — wird unten gebunden
        row.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // HBox als wrapper: zentriert row ohne ihn zu strecken
        HBox wrapper = new HBox(row);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setFillHeight(false);
        wrapper.visibleProperty().bind(visible);
        wrapper.managedProperty().bind(visible);

        wrapper.parentProperty().addListener((obs, o, parent) -> {
            if (!(parent instanceof Region p)) return;
            // row darf max 80% des Parents sein — schrumpft aber auf Inhalt
            row.maxWidthProperty().bind(p.widthProperty().multiply(0.80));
            logger.info("TrackerRow maxWidth gebunden: 80%");
        });

        Runnable rebuild = () -> Platform.runLater(() -> {
            row.getChildren().clear();

            Token active = state.activeToken.get();
            int activeIdx = state.initiative.indexOf(active);
            int round = state.round.get();
            int total = state.initiative.size();

            if (total == 0) {
                logger.fine("Initiative rebuild — leer");
                return;
            }

            List<Token> ordered = new ArrayList<>(total);
            if (activeIdx < 0) {
                ordered.addAll(state.initiative);
            } else {
                for (int i = 0; i < total; i++) {
                    ordered.add(state.initiative.get((activeIdx + i) % total));
                }
            }

            logger.fine("Initiative rebuild — Runde " + round
                    + " | aktiv: " + (active != null ? active.name : "–")
                    + " | Reihenfolge: " + ordered.stream().map(t -> t.name).toList());

            int shown = (int) Math.min(ordered.size(), MAX_SLOTS);
            for (int i = 0; i < shown; i++) {
                Token t = ordered.get(i);
                boolean isActive = (i == 0 && active != null);

                if (i > 0) {
                    int prevOrigIdx = state.initiative.indexOf(ordered.get(i - 1));
                    int currOrigIdx = state.initiative.indexOf(t);
                    boolean wraps = currOrigIdx <= prevOrigIdx;
                    if (wraps) {
                        row.getChildren().add(buildRoundMarker(round + 1));
                        logger.fine("Rundenmarker vor: " + t.name);
                    }
                }

                row.getChildren().add(buildSlot(t, isActive, state));
            }

            if (shown > 0 && shown == total) {
                boolean markerShownInLoop = false;
                for (int i = 1; i < shown; i++) {
                    int prevOrigIdx = state.initiative.indexOf(ordered.get(i - 1));
                    int currOrigIdx = state.initiative.indexOf(ordered.get(i));
                    if (currOrigIdx <= prevOrigIdx) {
                        markerShownInLoop = true;
                        break;
                    }
                }
                if (!markerShownInLoop) {
                    row.getChildren().add(buildRoundMarker(round + 1));
                    logger.fine("Rundenmarker am Ende (vollständige Liste, kein Wrap in Schleife)");
                }
            }
        });

        state.initiative.addListener((ListChangeListener<Token>) c -> rebuild.run());
        state.activeToken.addListener((obs, o, n) -> rebuild.run());
        state.round.addListener((obs, o, n) -> rebuild.run());
        state.actionsUsed.addListener((obs, o, n) -> rebuild.run());
        rebuild.run();

        logger.fine("InitiativeTrackerWidget gebaut");
        return wrapper;
    }

    // Slot
    private Node buildSlot(Token t, boolean isActive, EncounterState state) {
        double w = isActive ? SLOT_W_ACTIVE : SLOT_W;
        double h = isActive ? SLOT_H_ACTIVE : SLOT_H;

        VBox slot = new VBox(2);
        slot.setAlignment(Pos.TOP_CENTER);
        slot.setPrefSize(w, h);
        slot.setMaxSize(w, h);
        slot.setMinSize(w, h);
        slot.setPadding(new Insets(4, 3, 4, 3));
        slot.setStyle(isActive
                ? "-fx-background-color: rgba(255,220,50,0.10); -fx-background-radius: 6; -fx-border-color: #ffdd44; -fx-border-width: 1; -fx-border-radius: 6;"
                : "-fx-background-color: rgba(20,20,40,0.70); -fx-background-radius: 6; -fx-border-color: #2a2a44; -fx-border-width: 1; -fx-border-radius: 6;"
        );

        // Drag-Quelle (Delay — nur aktiver Token)
        if (isActive) {
            slot.setOnDragDetected(e -> {
                Dragboard db = slot.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString("DELAY:" + t.id);
                db.setContent(cc);
                logger.fine("Delay-Drag: " + t.name + " (" + t.id + ")");
                e.consume();
            });
        }

        // Drag-Ziel (alle anderen Slots)
        slot.setOnDragOver(e -> {
            if (e.getDragboard().hasString()
                    && e.getDragboard().getString().startsWith("DELAY:"))
                e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });

        slot.setOnDragDropped(e -> {
            String s = e.getDragboard().getString();
            if (!s.startsWith("DELAY:")) { e.setDropCompleted(false); e.consume(); return; }

            String draggedId = s.substring("DELAY:".length());
            Token dragged = state.initiative.stream()
                    .filter(tok -> tok.id.equals(draggedId)) // UUID-Vergleich
                    .findFirst().orElse(null);

            if (dragged == null || dragged == t) { e.setDropCompleted(false); e.consume(); return; }

            state.delay(dragged, t); // NEU: Logik komplett in State
            logger.info("Delay Drop: " + dragged.name + " hinter " + t.name);
            e.setDropCompleted(true);
            e.consume();
        });

        // HP-Balken
        slot.getChildren().add(buildHpBar(t, w - 6));

        // Token-Bild
        slot.getChildren().add(buildTokenImage(t, isActive ? TOKEN_IMG_ACT : TOKEN_IMG));

        // Aktionspunkte: nur beim aktiven Token voll, beim inaktiven nur Reaktion
        if (isActive) {
            slot.getChildren().add(buildActionDots(t, state));
        } else {
            slot.getChildren().add(buildReactionDot(t));
        }

        // Buffs/Debuffs — beide, aber beim aktiven etwas größer
        HBox buffs = buildBuffRow(t, isActive ? BUFF_ICON + 2 : BUFF_ICON);
        slot.getChildren().add(buffs);

        // Delay-Button nur am aktiven Token
        if (isActive) {
            Label delayBtn = new Label("Delay");
            delayBtn.setStyle("-fx-font-size: 9px; -fx-padding: 1 5 1 5; " +
                    "-fx-background-color: rgba(80,80,160,0.7); -fx-text-fill: #ccccff; " +
                    "-fx-background-radius: 3; -fx-cursor: hand;");
            delayBtn.setOnMouseClicked(e ->
                    logger.info("Delay gedrückt: " + t.name + " — per Drag&Drop verschieben"));
            slot.getChildren().add(delayBtn);
        }

        StackPane wrapper = new StackPane(slot);
        wrapper.setMinSize(w, SLOT_H_ACTIVE);
        wrapper.setMaxSize(w, SLOT_H_ACTIVE);
        StackPane.setAlignment(slot, Pos.TOP_CENTER); // alle oben ausgerichtet
        return wrapper;
    }

    // HP-Balken
    private Node buildHpBar(Token t, double width) {
        double pct = t.getHpPercent();
        Color barColor = hpColor(t);

        Rectangle bg = new Rectangle(width, HP_BAR_H);
        bg.setFill(Color.rgb(30, 30, 50));
        bg.setArcWidth(3);
        bg.setArcHeight(3);

        double fillW = Math.max(2, width * pct);
        Rectangle fill = new Rectangle(fillW, HP_BAR_H);
        fill.setFill(barColor);
        fill.setArcWidth(3);
        fill.setArcHeight(3);

        StackPane bar = new StackPane(bg, fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        bar.setMaxWidth(width);
        bar.setMinWidth(width);
        return bar;
    }

    private Color hpColor(Token t) {
        if (t.isPlayer) return Color.rgb(80, 200, 120); // Grün  — Spieler
        if (t.isAlly()) return Color.rgb(80, 140, 220); // Blau  — Verbündeter
        if (t.isNeutral()) return Color.rgb(180, 180, 80);  // Gelb  — Neutral
        return Color.rgb(220, 80, 80);                       // Rot   — Feind
    }

    // Token-Bild
    private Node buildTokenImage(Token t, double size) {
        if (t.npcBuild != null && t.npcBuild.tokenPath != null && !t.npcBuild.tokenPath.isBlank()) {
            String url = PathResolver.resolveString(t.npcBuild.tokenPath);
            if (url != null) {
                try {
                    Image img = new Image(url, size, size, true, true, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(size);
                    iv.setFitHeight(size);
                    iv.setPreserveRatio(true);

                    Rectangle clip = new Rectangle(size, size);
                    clip.setArcWidth(size);
                    clip.setArcHeight(size);
                    iv.setClip(clip);

                    logger.fine("Token-Bild: " + t.name);
                    return iv;
                } catch (Exception ex) {
                    logger.warning("Token-Bild Fehler (" + t.name + "): " + ex.getMessage());
                }
            }
        }

        // Fallback: Initial
        Label fallback = new Label(t.name.isEmpty() ? "?" : String.valueOf(t.name.charAt(0)).toUpperCase());
        fallback.setPrefSize(size, size);
        fallback.setAlignment(Pos.CENTER);
        fallback.setStyle("-fx-background-color: #333355; -fx-text-fill: #a0a0ff; " +
                "-fx-font-weight: bold; -fx-font-size: " + (size * 0.4) + "px; " +
                "-fx-background-radius: " + (size / 2) + ";");
        logger.fine("Token-Fallback (Initial): " + t.name);
        return fallback;
    }

    // Aktionspunkte
    // Volle Aktionspunkt-Reihe — nur beim aktiven Token
    private Node buildActionDots(Token t, EncounterState state) {
        HBox dots = new HBox(2);
        dots.setAlignment(Pos.CENTER);

        int max = t.maxActions;
        int used = state.actionsUsed.get();

        for (int i = 0; i < max; i++) {
            Rectangle dot = new Rectangle(ACTION_DOT, ACTION_DOT);
            dot.setArcWidth(ACTION_DOT);
            dot.setArcHeight(ACTION_DOT);
            dot.setFill(i < used
                    ? Color.rgb(60, 60, 80)      // verbraucht — dunkel
                    : Color.rgb(200, 200, 255));  // verfügbar — hell
            dots.getChildren().add(dot);
        }

        logger.fine("ActionDots: " + t.name + " | " + used + "/" + max);
        return dots;
    }

    // Nur Reaktions-Dot — beim inaktiven Token
    private Node buildReactionDot(Token t) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);

        boolean used = t.reactionUsed.get();

        Label reaction = new Label(used ? "↩" : "↩");
        reaction.setStyle("-fx-font-size: 11px; -fx-text-fill: "
                + (used ? "#444466" : "#a0a0ff") + ";");
        Tooltip.install(reaction, new Tooltip(used ? "Reaktion verbraucht" : "Reaktion verfügbar"));

        box.getChildren().add(reaction);
        return box;
    }

    // Buffs/Debuffs
    private HBox buildBuffRow(Token t, double iconSize) {
        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);

        if (t.npcBuild == null || t.npcBuild.traits.isEmpty()) return row;

        int shown = Math.min(t.npcBuild.traits.size(), 4);
        for (int i = 0; i < shown; i++) {
            String trait = t.npcBuild.traits.get(i);
            Label icon = new Label(trait.substring(0, Math.min(2, trait.length())).toUpperCase());
            icon.setPrefSize(iconSize, iconSize);
            icon.setAlignment(Pos.CENTER);
            icon.setStyle("-fx-background-color: rgba(140,80,200,0.7); -fx-text-fill: white; " +
                    "-fx-font-size: 7px; -fx-background-radius: 2;");
            Tooltip.install(icon, new Tooltip(trait));
            row.getChildren().add(icon);
        }

        return row;
    }

    // Rundenmarker
    private Node buildRoundMarker(int nextRound) {
        VBox marker = new VBox(2);
        marker.setAlignment(Pos.CENTER);
        marker.setPadding(new Insets(0, 3, 0, 3));

        Rectangle line = new Rectangle(3, SLOT_H_ACTIVE);
        line.setFill(Color.rgb(160, 160, 255, 0.65));
        line.setArcWidth(2);
        line.setArcHeight(2);

        Label lbl = new Label("R" + nextRound);
        lbl.setStyle("-fx-text-fill: #8080cc; -fx-font-size: 8px; -fx-font-weight: bold;");

        // Initiative-0-Marker darunter
        Label initZero = new Label("Init 0");
        initZero.setStyle("-fx-text-fill: #555577; -fx-font-size: 7px;");

        marker.getChildren().addAll(lbl, line, initZero);
        return marker;
    }
}