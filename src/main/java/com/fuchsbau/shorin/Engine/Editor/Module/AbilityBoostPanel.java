package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.System.Character.*;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.logging.Logger;

public class AbilityBoostPanel {

    private static final Logger logger = FileLogger.getLogger();
    private static final AbilityScore[] ALL = AbilityScore.values();

    private final PlayerCharacter character;
    private final Ancestrie ancestrie;
    private final PlayerBackground background;
    private final ClassBuild classBuild;

    private final VBox root = new VBox(12);

    // Gewählte Boosts pro Quelle
    private AbilityScore bgChoice = null;
    private AbilityScore classChoice = null;
    private final Set<AbilityScore> freeChoices = EnumSet.noneOf(AbilityScore.class);
    private final Map<AbilityScore, CheckBox> bgFreeBoxes = new EnumMap<>(AbilityScore.class);

    // Checkboxen Refs für UI-Sync
    private final Map<AbilityScore, CheckBox> bgBoxes = new EnumMap<>(AbilityScore.class);
    private final Map<AbilityScore, CheckBox> classBoxes = new EnumMap<>(AbilityScore.class);
    private final Map<AbilityScore, CheckBox> freeBoxes = new EnumMap<>(AbilityScore.class);

    private final Runnable onChanged;

    private static final int FREE_COUNT = 4;

    public AbilityBoostPanel(Ancestrie anc, PlayerBackground bg, ClassBuild cls,
                             PlayerCharacter character, Runnable onChanged) {

        this.onChanged = onChanged;
        this.ancestrie = anc;
        this.background = bg;
        this.classBuild = cls;
        this.character = character;
        restoreChoices();
        build();
    }

    public Node getRoot() {
        return root;
    }

    // Gespeicherte Choices aus dem Character zurückladen
    private void restoreChoices() {
        if (background != null) {
            Set<AbilityScore> choiceSet = EnumSet.copyOf(background.choiceBoosts.isEmpty()
                    ? EnumSet.noneOf(AbilityScore.class) : EnumSet.copyOf(background.choiceBoosts));
            for (AbilityScoreEntry e : character.backgroundBoostChoice) {
                if (choiceSet.contains(e.abilityScore)) {
                    bgChoice = e.abilityScore;
                    break;
                }
            }
        }

        if (character.classBoostChoice != null)
            classChoice = character.classBoostChoice.abilityScore;

        for (AbilityScoreEntry e : character.freeBoostChoices)
            freeChoices.add(e.abilityScore);


        logger.fine("Choices restored — bg=" + bgChoice + " class=" + classChoice + " free=" + freeChoices);
    }

    private void build() {
        root.setPadding(new Insets(8));
        root.getChildren().addAll(
                buildAncestrySection(),
                new Separator(),
                buildBgSection(),
                new Separator(),
                buildClassSection(),
                new Separator(),
                buildFreeSection()
        );
    }

    private Node buildAncestrySection() {
        VBox box = new VBox(6);
        box.getChildren().add(sectionLabel("Ancestry — " + (ancestrie != null ? ancestrie.name : "–")));

        if (ancestrie == null) return box;

        Set<AbilityScore> fixedBoosts = EnumSet.noneOf(AbilityScore.class);
        for (AbilityScoreEntry e : ancestrie.abilityBoosts) {
            CheckBox cb = new CheckBox(e.abilityScore.name() + (e.value < 0 ? " (Flaw)" : ""));
            cb.setSelected(true);
            cb.setDisable(true);
            cb.setStyle("-fx-opacity: 0.85;");
            box.getChildren().add(cb);

            if (e.value > 0) fixedBoosts.add(e.abilityScore);
        }

        if (ancestrie.freeBoosts > 0) {
            box.getChildren().add(smallLabel("Freie Boosts (" + ancestrie.freeBoosts + "x wählen):"));
            Set<AbilityScore> ancestryFreeChoices = EnumSet.noneOf(AbilityScore.class);

            for (AbilityScoreEntry e : character.ancestryBoostChoices) {
                if (e.value > 0 && !fixedBoosts.contains(e.abilityScore))
                    ancestryFreeChoices.add(e.abilityScore);
            }

            HBox row = new HBox(6);
            for (AbilityScore score : ALL) {
                if (fixedBoosts.contains(score)) continue;
                CheckBox cb = new CheckBox(score.name());
                cb.setSelected(ancestryFreeChoices.contains(score));
                cb.setOnAction(e -> {
                    if (cb.isSelected()) {
                        if (ancestryFreeChoices.size() >= ancestrie.freeBoosts) {
                            cb.setSelected(false);
                            logger.fine("Ancestry free limit erreicht");
                            return;
                        }
                        ancestryFreeChoices.add(score);
                    } else {
                        ancestryFreeChoices.remove(score);
                    }
                    syncAncestryFreeToCharacter(ancestryFreeChoices, fixedBoosts);
                    logger.fine("Ancestry free -> " + ancestryFreeChoices);
                });
                row.getChildren().add(cb);
            }
            box.getChildren().add(row);
        }

        return box;
    }

    private void syncAncestryFreeToCharacter(Set<AbilityScore> freeChosen, Set<AbilityScore> fixedBoosts) {
        character.ancestryBoostChoices.removeIf(e -> e.value > 0 && !fixedBoosts.contains(e.abilityScore));
        for (AbilityScore s : freeChosen)
            character.ancestryBoostChoices.add(new AbilityScoreEntry(s, 1));
        character.refresh();
        onChanged.run();
    }

    // Background: eine aus choiceBoosts + freeBoosts freie
    private Node buildBgSection() {
        VBox box = new VBox(6);
        box.getChildren().add(sectionLabel("Background — " + (background != null ? background.name : "–")));

        if (background == null) return box;

        Set<AbilityScore> choiceSet = background.choiceBoosts.isEmpty()
                ? EnumSet.noneOf(AbilityScore.class)
                : EnumSet.copyOf(background.choiceBoosts);

        Set<AbilityScore> backgroundFreeChoices = EnumSet.noneOf(AbilityScore.class);
        for (AbilityScoreEntry e : character.backgroundBoostChoice) {
            if (!choiceSet.contains(e.abilityScore)) backgroundFreeChoices.add(e.abilityScore);
        }

        HBox choiceRow = new HBox(6);
        for (AbilityScore score : background.choiceBoosts) {
            CheckBox cb = new CheckBox(score.name());
            bgBoxes.put(score, cb);
            cb.setSelected(score == bgChoice);
            choiceRow.getChildren().add(cb);
        }
        box.getChildren().add(choiceRow);

        bgBoxes.forEach((score, cb) -> cb.setOnAction(e -> {
            if (cb.isSelected()) {
                bgChoice = score;
                bgBoxes.forEach((s, b) -> {
                    if (s != score) b.setSelected(false);
                });

                // Conflict: gewählter Choice-Score im Free rauswerfen
                if (backgroundFreeChoices.remove(score)) {
                    bgFreeBoxes.get(score).setSelected(false);
                    syncBgToCharacter(backgroundFreeChoices);
                    logger.fine("BG free conflict cleared: " + score);
                }
                // Nur den GEWÄHLTEN Score sperren, nicht alle choiceBoosts
                bgFreeBoxes.forEach((fs, fcb) -> fcb.setDisable(fs == score));

                syncBgToCharacter(backgroundFreeChoices);
                logger.fine("BG choice -> " + score);
            } else {
                bgChoice = null;
                bgFreeBoxes.forEach((fs, fcb) -> fcb.setDisable(false));
                syncBgToCharacter(backgroundFreeChoices);
            }
        }));

        if (background.freeBoosts > 0) {
            box.getChildren().add(smallLabel("Freie Boosts (" + background.freeBoosts + "x wählen):"));

            HBox row = new HBox(6);
            for (AbilityScore score : ALL) {
                // Nur den aktuell gewählten bgChoice sperren, nicht alle choiceBoosts
                CheckBox cb = new CheckBox(score.name());
                bgFreeBoxes.put(score, cb);
                cb.setSelected(backgroundFreeChoices.contains(score));
                cb.setDisable(score == bgChoice);
                cb.setOnAction(e -> {
                    if (cb.isSelected()) {
                        if (backgroundFreeChoices.size() >= background.freeBoosts) {
                            cb.setSelected(false);
                            logger.fine("BG free limit erreicht");
                            return;
                        }
                        backgroundFreeChoices.add(score);
                    } else {
                        backgroundFreeChoices.remove(score);
                    }
                    syncBgToCharacter(backgroundFreeChoices);
                    logger.fine("BG free -> " + backgroundFreeChoices);
                });
                row.getChildren().add(cb);
            }
            box.getChildren().add(row);
        }

        return box;
    }

    private void syncBgToCharacter(Set<AbilityScore> freeChosen) {
        character.backgroundBoostChoice.clear();
        if (bgChoice != null)
            character.backgroundBoostChoice.add(new AbilityScoreEntry(bgChoice));
        for (AbilityScore s : freeChosen)
            character.backgroundBoostChoice.add(new AbilityScoreEntry(s));
        character.refresh();
        onChanged.run();
    }

    private Node buildClassSection() {
        VBox box = new VBox(6);
        box.getChildren().add(sectionLabel("Klasse — " + (classBuild != null ? classBuild.name : "–")));

        if (classBuild == null || classBuild.keyAbilities.isEmpty()) return box;

        box.getChildren().add(smallLabel("Key Ability:"));
        HBox row = new HBox(6);
        for (AbilityScoreEntry entry : classBuild.keyAbilities) {
            AbilityScore score = entry.abilityScore;
            CheckBox cb = new CheckBox(score.name());
            classBoxes.put(score, cb);
            cb.setSelected(score == classChoice);
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    classChoice = score;
                    classBoxes.forEach((s, b) -> {
                        if (s != score) b.setSelected(false);
                    });
                    syncClassToCharacter();
                    logger.fine("Class choice -> " + score);
                } else {
                    classChoice = null;
                    syncClassToCharacter();
                }
            });
            row.getChildren().add(cb);
        }
        box.getChildren().add(row);
        return box;
    }

    private void syncClassToCharacter() {
        character.classBoostChoice = classChoice != null ? new AbilityScoreEntry(classChoice) : null;
        character.refresh();
        onChanged.run();
    }

    // Freie Boosts: 4x, nichts doppelt (auch nicht mit anderen Quellen)
    private Node buildFreeSection() {
        VBox box = new VBox(6);
        box.getChildren().add(sectionLabel("Freie Boosts (4x)"));

        HBox row = new HBox(6);
        for (AbilityScore score : ALL) {
            CheckBox cb = new CheckBox(score.name());
            freeBoxes.put(score, cb);
            cb.setSelected(freeChoices.contains(score));
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    if (freeChoices.size() >= FREE_COUNT) {
                        cb.setSelected(false);
                        logger.fine("Free boost limit erreicht");
                        return;
                    }
                    freeChoices.add(score);
                } else {
                    freeChoices.remove(score);
                }
                syncFreeToCharacter();
                logger.fine("Free choices -> " + freeChoices);
            });
            row.getChildren().add(cb);
        }
        box.getChildren().add(row);
        return box;
    }

    private void syncFreeToCharacter() {
        character.freeBoostChoices.clear();
        for (AbilityScore s : freeChoices)
            character.freeBoostChoices.add(new AbilityScoreEntry(s));
        character.refresh();
        onChanged.run();
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        l.setTextFill(Color.ORANGE);
        return l;
    }

    private Label smallLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px;");
        l.setTextFill(Color.BLACK);
        return l;
    }
}