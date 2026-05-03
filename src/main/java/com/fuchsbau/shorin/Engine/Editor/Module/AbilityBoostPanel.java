package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fuchsbau.shorin.Engine.Race.Ancestrie;
import com.fuchsbau.shorin.Engine.System.Character.*;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.logging.Logger;

public class AbilityBoostPanel {

    private static final Logger logger = FileLogger.getLogger();
    private static final AbilityScore[] ALL = AbilityScore.values();

    private final Ancestrie ancestrie;
    private final PlayerBackground background;
    private final ClassBuild classBuild;
    private final PlayerCharacter character;

    private final VBox root = new VBox(8);

    // Auswahl-State
    private AbilityScore ancestryFreeChoice = null;
    private AbilityScore bgChoice = null;
    private AbilityScore classChoice = null;
    private final Set<AbilityScore> freeChoices = new LinkedHashSet<>();
    private static final int FREE_BOOST_COUNT = 4;

    public AbilityBoostPanel(Ancestrie anc, PlayerBackground bg, ClassBuild cls,
                             PlayerCharacter character) {
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

    private void restoreChoices() {
        /*
        // Ancestry free
        if (character.ancestryBoostChoices.containsKey("free_0")) {
            try {
                ancestryFreeChoice = AbilityScore.valueOf(character.ancestryBoostChoices.get("free_0"));
            } catch (Exception ignored) {
            }
        }
        // Background
        if (!character.backgroundBoostChoice.isBlank()) {
            try {
                bgChoice = AbilityScore.valueOf(character.backgroundBoostChoice);
            } catch (Exception ignored) {
            }
        }
        // Class
        if (!character.classBoostChoice.isBlank()) {
            try {
                classChoice = AbilityScore.valueOf(character.classBoostChoice);
            } catch (Exception ignored) {
            }
        }
        // Free
        for (String s : character.freeBoostChoices) {
            try {
                freeChoices.add(AbilityScore.valueOf(s));
            } catch (Exception ignored) {
            }
        }
        logger.fine("Choices wiederhergestellt: bg=" + bgChoice
                + " cls=" + classChoice + " free=" + freeChoices);

         */
    }

    private void build() {
        /*root.setPadding(new Insets(8));
        root.getChildren().clear();

        // --- Ancestry feste Boosts ---
        VBox ancSection = section("Ancestry Boosts");
        for (Ancestrie.AbilityBoost b : ancestrie.abilityBoosts) {
            Label lbl = new Label((b.value > 0 ? "+" : "") + b.value + "  " + b.score.name()
                    + "  (" + b.score.fullName() + ")");
            lbl.setTextFill(b.value > 0 ? Color.LIGHTGREEN : Color.SALMON);
            ancSection.getChildren().add(lbl);
        }
        // Ancestry freeBoosts — Dropdown
        for (int i = 0; i < ancestrie.freeBoosts; i++) {
            ComboBox<AbilityScore> pick = scoreCombo("Freier Ancestry-Boost...");
            pick.setOnAction(e -> {
                ancestryFreeChoice = pick.getValue();
                logger.fine("Ancestry Free Boost: " + ancestryFreeChoice);
                recalculate();
            });
            // vorherige Wahl laden
            if (character.ancestryBoostChoices.containsKey("free_" + i)) {
                try {
                    pick.setValue(AbilityScore.valueOf(character.ancestryBoostChoices.get("free_" + i)));
                } catch (Exception ignored) {
                }
            }
            ancSection.getChildren().add(pick);
        }
        root.getChildren().add(ancSection);

        // --- Background Boosts ---
        VBox bgSection = section("Background Boosts");
        if (!background.choiceBoosts.isEmpty()) {
            Label info = new Label("Wähle einen: " + String.join(" / ", background.choiceBoosts));
            info.setStyle("-fx-font-size: 11px;");
            bgSection.getChildren().add(info);

            ObservableList<AbilityScore> bgOptions = FXCollections.observableArrayList();
            background.choiceBoosts.forEach(s -> {
                try {
                    bgOptions.add(AbilityScore.valueOf(s));
                } catch (Exception ignored) {
                }
            });
            ComboBox<AbilityScore> bgPick = new ComboBox<>(bgOptions);
            bgPick.setPromptText("Boost wählen...");
            bgPick.setMaxWidth(Double.MAX_VALUE);
            bgPick.setOnAction(e -> {
                bgChoice = bgPick.getValue();
                logger.fine("Background Choice Boost: " + bgChoice);
                recalculate();
            });
            if (!character.backgroundBoostChoice.isBlank()) {
                try {
                    bgPick.setValue(AbilityScore.valueOf(character.backgroundBoostChoice));
                } catch (Exception ignored) {
                }
            }
            bgSection.getChildren().add(bgPick);
        }
        // Background freeBoosts
        for (int i = 0; i < background.freeBoosts; i++) {
            ComboBox<AbilityScore> pick = scoreCombo("Freier Background-Boost...");
            pick.setOnAction(e -> {
                bgChoice = pick.getValue();  // falls kein choiceBoost
                recalculate();
            });
            bgSection.getChildren().add(pick);
        }
        root.getChildren().add(bgSection);

        // --- Class Boost ---
        VBox classSection = section("Class Boost");
        if (!classBuild.keyAbilities.isEmpty()) {
            Label info = new Label("Key Ability: " + classBuild.keyAbilities.stream()
                    .map(AbilityScore::name).reduce((a, b) -> a + " / " + b).orElse("–"));
            info.setStyle("-fx-font-size: 11px;");

            ObservableList<AbilityScore> clsOptions =
                    FXCollections.observableArrayList(classBuild.keyAbilities);
            ComboBox<AbilityScore> clsPick = new ComboBox<>(clsOptions);
            clsPick.setPromptText("Key Ability wählen...");
            clsPick.setMaxWidth(Double.MAX_VALUE);
            clsPick.setOnAction(e -> {
                classChoice = clsPick.getValue();
                logger.fine("Class Key Ability: " + classChoice);
                recalculate();
            });
            if (!character.classBoostChoice.isBlank()) {
                try {
                    clsPick.setValue(AbilityScore.valueOf(character.classBoostChoice));
                } catch (Exception ignored) {
                }
            }
            classSection.getChildren().addAll(info, clsPick);
        }
        root.getChildren().add(classSection);

        // --- Freie Boosts (4 Checkboxen) ---
        VBox freeSection = section("Freie Boosts (wähle " + FREE_BOOST_COUNT + ")");
        GridPane freeGrid = new GridPane();
        freeGrid.setHgap(16);
        freeGrid.setVgap(4);

        AbilityScore[] scores = AbilityScore.values();
        for (int i = 0; i < scores.length; i++) {
            AbilityScore s = scores[i];
            CheckBox cb = new CheckBox(s.name());
            cb.setSelected(character.freeBoostChoices.contains(s.name()));
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    if (freeChoices.size() >= FREE_BOOST_COUNT) {
                        cb.setSelected(false);
                        logger.fine("Freie Boosts: Maximum erreicht (" + FREE_BOOST_COUNT + ")");
                        return;
                    }
                    freeChoices.add(s);
                } else {
                    freeChoices.remove(s);
                }
                logger.fine("Freie Boosts: " + freeChoices);
                recalculate();
            });
            freeGrid.add(cb, i % 2, i / 2);
        }
        freeSection.getChildren().add(freeGrid);
        root.getChildren().add(freeSection);

        // Separator + Übersicht
        root.getChildren().add(new Separator());
        root.getChildren().add(buildSummary());

        // Initiale Berechnung
        recalculate();

         */
    }

    // --- Übersicht-Label (wird nach recalculate aktualisiert) ---
    private final Map<AbilityScore, Label> summaryLabels = new EnumMap<>(AbilityScore.class);

    private Node buildSummary() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);
        grid.setPadding(new Insets(4));

        AbilityScore[] scores = AbilityScore.values();
        for (int i = 0; i < scores.length; i++) {
            AbilityScore s = scores[i];
            Label name = new Label(s.name());
            name.setStyle("-fx-font-weight: bold;");
            Label val = new Label("10  (+0)");
            summaryLabels.put(s, val);
            grid.add(name, (i % 2) * 2, i / 2);
            grid.add(val, (i % 2) * 2 + 1, i / 2);
        }
        return grid;
    }

    private void updateSummary(Map<AbilityScore, Integer> totals) {
        summaryLabels.forEach((s, lbl) -> {
            int boost = totals.getOrDefault(s, 0);
            int score = 10 + boost;
            int mod = (score - 10) / 2;
            lbl.setText(score + "  (" + (mod >= 0 ? "+" : "") + mod + ")");
            lbl.setTextFill(boost > 0 ? Color.LIGHTGREEN : boost < 0 ? Color.SALMON : Color.GRAY);
        });
    }

    // --- Helpers ---
    private VBox section(String title) {
        Label hdr = new Label(title);
        hdr.setStyle("-fx-font-weight: bold;");
        VBox box = new VBox(4, hdr, new Separator());
        box.setPadding(new Insets(4, 0, 4, 0));
        return box;
    }

    private ComboBox<AbilityScore> scoreCombo(String prompt) {
        ComboBox<AbilityScore> cb = new ComboBox<>(
                FXCollections.observableArrayList(ALL));
        cb.setPromptText(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }
}