package com.fuchsbau.shorin.Engine.Editor.Module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.System.*;
import com.fuchsbau.shorin.Engine.System.Character.AbilityScore;
import com.fuchsbau.shorin.Engine.System.Character.Sense;
import com.fuchsbau.shorin.Engine.System.Character.SenseEntry;
import com.fuchsbau.shorin.Engine.System.Character.Skill;
import com.fuchsbau.shorin.Engine.System.Combat.ActionCost;
import com.fuchsbau.shorin.Engine.System.Combat.DamageModifier;
import com.fuchsbau.shorin.Engine.System.Combat.DamageType;
import com.fuchsbau.shorin.Engine.System.Misc.RecallKnowledge;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class NpcModule implements EditorModule {

    private final Logger logger = FileLogger.getLogger();

    private static final String FILE = "Ingame/npcs.json";

    private final ObservableList<NpcBuild> npcs = FXCollections.observableArrayList();
    private NpcBuild selectedNpc = null;
    private ListView<NpcBuild> npcListView;
    private TextField searchField;
    private Spinner<Integer> levelFilterSpinner;

    private Label tokenPreview;
    private Label tokenPathLabel;

    private TextField npcNameField;
    private Spinner<Integer> levelSpinner;
    private ComboBox<String> sizeBox;
    private FlowPane traitPane;
    private TextField traitInput;

    private final Map<AbilityScore, Spinner<Integer>> statSpinners = new EnumMap<>(AbilityScore.class);

    private final Map<Skill, Spinner<Integer>> skillSpinners = new EnumMap<>(Skill.class);
    private VBox recallBox;
    private VBox sensesBox;
    private VBox attacksBox;
    private VBox actionsBox;

    private Spinner<Integer> acSpinner, hpSpinner, speedSpinner;
    private Spinner<Integer> fortSpinner, refSpinner, willSpinner, perceptionSpinner;

    private VBox immunitiesBox, resistancesBox, weaknessesBox;

    @Override
    public String getTitle() {
        return "Npcs";
    }

    @Override
    public Node buildContent() {
        VBox sectionsCol = new VBox(12);
        sectionsCol.setPadding(new Insets(8));
        sectionsCol.setFillWidth(true);

        sectionsCol.getChildren().addAll(
                buildNameSection(),
                buildTokenSection(),
                buildStatsSection(),
                buildSkillsSection(),
                buildCombatSection(),
                buildAttacksSection(),
                buildActionsSection()
        );

        ScrollPane scroll = new ScrollPane(sectionsCol);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button saveBtn = new Button("Speichern");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveToDisk());

        BorderPane root = new BorderPane();
        root.setCenter(scroll);
        root.setBottom(saveBtn);
        return root;
    }

    private Node buildNameSection() {
        npcNameField = new TextField();
        npcNameField.setPromptText("NPC Name...");
        npcNameField.setMaxWidth(Double.MAX_VALUE);
        npcNameField.textProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) {
                selectedNpc.name = nv.trim();
                logger.fine("Name: " + nv);
            }
        });

        levelSpinner = new Spinner<>(-1, 25, 0);
        levelSpinner.setEditable(true);
        levelSpinner.setPrefWidth(70);
        levelSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) {
                selectedNpc.level = nv;
                logger.fine("Level: " + nv);
            }
        });

        sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan");
        sizeBox.setValue("Medium");
        sizeBox.setOnAction(e -> {
            if (selectedNpc != null) selectedNpc.size = sizeBox.getValue();
        });

        HBox topRow = new HBox(8,
                new Label("Level"), levelSpinner,
                new Label("Größe"), sizeBox
        );

        // Traits
        traitPane = new FlowPane(4, 4);
        traitInput = new TextField();
        traitInput.setPromptText("Trait hinzufügen...");
        traitInput.setOnAction(e -> addTrait(traitInput.getText().trim()));

        Button addTraitBtn = new Button("+ Trait");
        addTraitBtn.setOnAction(e -> addTrait(traitInput.getText().trim()));

        HBox traitRow = new HBox(4, traitInput, addTraitBtn);
        HBox.setHgrow(traitInput, Priority.ALWAYS);

        return buildSection("NPC", npcNameField, topRow, new Label("Traits"), traitPane, traitRow);
    }

    private Node buildTokenSection() {
        tokenPreview = new Label("[ Kein Token ]");
        tokenPreview.setPrefSize(80, 80);
        tokenPreview.setStyle("-fx-border-color: #333350; -fx-border-width: 1; -fx-alignment: center;");

        tokenPathLabel = new Label("(kein Pfad)");
        tokenPathLabel.setTextFill(Color.GRAY);
        tokenPathLabel.setWrapText(true);

        Button chooseBtn = new Button("Bild auswählen...");
        chooseBtn.setOnAction(e -> chooseTokenImage());

        Button clearBtn = new Button("✕");
        clearBtn.setOnAction(e -> {
            if (selectedNpc == null) return;
            selectedNpc.tokenPath = "";
            tokenPathLabel.setText("(kein Pfad)");
            tokenPreview.setGraphic(null);
            tokenPreview.setText("[ Kein Token ]");
            logger.fine("Token entfernt");
        });

        HBox btnRow = new HBox(4, chooseBtn, clearBtn);
        HBox content = new HBox(12, tokenPreview, new VBox(6, tokenPathLabel, btnRow));

        return buildSection("Token", content);
    }

    private void chooseTokenImage() {
        if (selectedNpc == null) return;

        // Startverzeichnis = resources/images
        Path resourcesBase = Paths.get("src/main/resources");
        File initialDir = resourcesBase.resolve("images").toFile();
        if (!initialDir.exists()) initialDir = resourcesBase.toFile();

        FileChooser fc = new FileChooser();
        fc.setTitle("Token-Bild auswählen");
        fc.setInitialDirectory(initialDir.exists() ? initialDir : new File("."));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        File file = fc.showOpenDialog(null);
        if (file == null) return;

        // Prüfen ob Datei im Resources-Ordner liegt
        Path filePath = file.toPath().toAbsolutePath();
        Path resourcesPath = resourcesBase.toAbsolutePath();

        if (!filePath.startsWith(resourcesPath)) {
            logger.warning("Token muss im Resources-Ordner liegen: " + filePath);
            // Fehlermeldung anzeigen
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ungültiger Pfad");
            alert.setHeaderText("Bild liegt außerhalb des Resources-Ordners");
            alert.setContentText("Bitte nur Bilder aus:\n" + resourcesPath);
            alert.showAndWait();
            return;
        }

        // Relativen Pfad speichern
        String relative = resourcesPath.relativize(filePath).toString()
                .replace("\\", "/"); // Windows → Unix Trennzeichen
        selectedNpc.tokenPath = relative;
        tokenPathLabel.setText(relative);
        loadTokenPreview(file.toURI().toString());
        logger.info("Token gesetzt: " + relative);
    }

    private void loadTokenPreview(String url) {
        try {
            Image img = new Image(url, 80, 80, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(80);
            iv.setFitHeight(80);
            tokenPreview.setGraphic(iv);
            tokenPreview.setText("");
        } catch (Exception e) {
            tokenPreview.setGraphic(null);
            tokenPreview.setText("[ Fehler ]");
            logger.warning("Token konnte nicht geladen werden: " + url);
        }
    }

    private Node buildStatsSection() {
        // Stats Grid bleibt gleich
        GridPane statGrid = new GridPane();
        statGrid.setHgap(12);
        statGrid.setVgap(4);

        AbilityScore[] abilities = AbilityScore.values();
        for (int i = 0; i < abilities.length; i++) {
            AbilityScore ab = abilities[i];
            Spinner<Integer> spinner = new Spinner<>(-5, 10, 0);
            spinner.setEditable(true);
            spinner.setPrefWidth(70);
            spinner.valueProperty().addListener((obs, ov, nv) -> {
                if (selectedNpc == null) return;
                switch (ab) {
                    case STR -> selectedNpc.str = nv;
                    case DEX -> selectedNpc.dex = nv;
                    case CON -> selectedNpc.con = nv;
                    case INT -> selectedNpc.intel = nv;
                    case WIS -> selectedNpc.wis = nv;
                    case CHA -> selectedNpc.cha = nv;
                }
            });
            statSpinners.put(ab, spinner);
            statGrid.add(new Label(ab.name()), i % 3 * 2, i / 3);
            statGrid.add(spinner, i % 3 * 2 + 1, i / 3);
        }

        // Recall Knowledge
        recallBox = new VBox(4);

        Button addRecallBtn = new Button("+ Recall Knowledge");
        addRecallBtn.setMaxWidth(Double.MAX_VALUE);
        addRecallBtn.setOnAction(e -> addRecall());

        return buildSection("Stats", statGrid, new Separator(),
                new Label("Recall Knowledge"), recallBox, addRecallBtn);
    }

    private Node buildSkillsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);

        Skill[] skills = Skill.values();
        for (int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            Spinner<Integer> spinner = new Spinner<>(-10, 30, 0);
            spinner.setEditable(true);
            spinner.setPrefWidth(70);
            spinner.valueProperty().addListener((obs, ov, nv) -> {
                if (selectedNpc != null) selectedNpc.skills.put(skill, nv);
            });
            skillSpinners.put(skill, spinner);
            grid.add(new Label(skill.displayName()), i % 3 * 2, i / 3);
            grid.add(spinner, i % 3 * 2 + 1, i / 3);
        }

        return buildSection("Skills", grid);
    }

    private Node buildCombatSection() {
        // Spinner bleiben gleich
        acSpinner = makeCombatSpinner(10, 50, 10);
        hpSpinner = makeCombatSpinner(1, Integer.MAX_VALUE, 5);
        speedSpinner = makeCombatSpinner(0, 200, 25);
        fortSpinner = makeCombatSpinner(-10, 30, 0);
        refSpinner = makeCombatSpinner(-10, 30, 0);
        willSpinner = makeCombatSpinner(-10, 30, 0);
        perceptionSpinner = makeCombatSpinner(-10, 30, 0);

        acSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.ac = nv;
        });
        hpSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.hp = nv;
        });
        speedSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.speed = nv;
        });
        fortSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.fortitude = nv;
        });
        refSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.reflex = nv;
        });
        willSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.will = nv;
        });
        perceptionSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedNpc != null) selectedNpc.perception = nv;
        });

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);

        grid.add(new Label("AC"), 0, 0);
        grid.add(acSpinner, 1, 0);
        grid.add(new Label("HP"), 2, 0);
        grid.add(hpSpinner, 3, 0);
        grid.add(new Label("Speed"), 4, 0);
        grid.add(speedSpinner, 5, 0);
        grid.add(new Label("Fort"), 0, 1);
        grid.add(fortSpinner, 1, 1);
        grid.add(new Label("Ref"), 2, 1);
        grid.add(refSpinner, 3, 1);
        grid.add(new Label("Will"), 4, 1);
        grid.add(willSpinner, 5, 1);
        grid.add(new Label("Perception"), 0, 2);
        grid.add(perceptionSpinner, 1, 2);

        // Senses
        sensesBox = new VBox(4);

        Button addSenseBtn = new Button("+ Sinn");
        addSenseBtn.setMaxWidth(Double.MAX_VALUE);
        addSenseBtn.setOnAction(e -> addSense());


        // Immunities
        immunitiesBox = new VBox(4);
        Button addImmunityBtn = new Button("+ Immunität");
        addImmunityBtn.setMaxWidth(Double.MAX_VALUE);
        addImmunityBtn.setOnAction(e -> {
            if (selectedNpc == null) return;
            selectedNpc.immunities.add(DamageType.PHYSICAL);
            refreshImmunitiesBox(immunitiesBox);
        });

        // Resistances
        resistancesBox = new VBox(4);
        Button addResistanceBtn = new Button("+ Resistenz");
        addResistanceBtn.setMaxWidth(Double.MAX_VALUE);
        addResistanceBtn.setOnAction(e -> {
            if (selectedNpc == null) return;
            selectedNpc.resistances.add(new DamageModifier());
            refreshModifierBox(resistancesBox, selectedNpc.resistances);
        });

        // Weaknesses
        weaknessesBox = new VBox(4);
        Button addWeaknessBtn = new Button("+ Schwäche");
        addWeaknessBtn.setMaxWidth(Double.MAX_VALUE);
        addWeaknessBtn.setOnAction(e -> {
            if (selectedNpc == null) return;
            selectedNpc.weaknesses.add(new DamageModifier());
            refreshModifierBox(weaknessesBox, selectedNpc.weaknesses);
        });

        // Als 3 Spalten nebeneinander
        VBox immCol = new VBox(4, new Label("Immunitäten"), new Separator(), immunitiesBox, addImmunityBtn);
        VBox resCol = new VBox(4, new Label("Resistenzen"), new Separator(), resistancesBox, addResistanceBtn);
        VBox weakCol = new VBox(4, new Label("Schwächen"), new Separator(), weaknessesBox, addWeaknessBtn);
        HBox.setHgrow(immCol, Priority.ALWAYS);
        HBox.setHgrow(resCol, Priority.ALWAYS);
        HBox.setHgrow(weakCol, Priority.ALWAYS);

        HBox irwRow = new HBox(12, immCol, new Separator(), resCol, new Separator(), weakCol);

        return buildSection("Combat", grid, new Separator(),
                new Label("Sinne"), sensesBox, addSenseBtn,
                new Separator(), irwRow);
    }

    private Node buildAttacksSection() {
        attacksBox = new VBox(6);

        Button addBtn = new Button("+ Angriff");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addAttack());

        return buildSection("Angriffe", attacksBox, addBtn);
    }

    private Node buildActionsSection() {
        actionsBox = new VBox(6);

        // Später: Dropdown mit allen GameActions aus ActionModule
        TextField actionIdField = new TextField();
        actionIdField.setPromptText("Action Name...");

        Button addBtn = new Button("+ Action");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            String id = actionIdField.getText().trim();
            if (id.isBlank() || selectedNpc == null) return;
            selectedNpc.actionIds.add(id);
            actionIdField.clear();
            refreshActionsBox();
        });

        HBox inputRow = new HBox(4, actionIdField, addBtn);
        HBox.setHgrow(actionIdField, Priority.ALWAYS);

        return buildSection("Aktionen", actionsBox, inputRow);
    }

    private void addSense() {
        if (selectedNpc == null) return;
        SenseEntry entry = new SenseEntry();
        selectedNpc.senses.add(entry);
        refreshSensesBox();
        logger.fine("Sinn hinzugefügt");
    }

    private void refreshSensesBox() {
        sensesBox.getChildren().clear();
        if (selectedNpc == null) return;

        for (SenseEntry entry : selectedNpc.senses) {
            ComboBox<Sense> senseBox = new ComboBox<>();
            senseBox.getItems().addAll(Sense.values());
            senseBox.setValue(entry.sense);

            // Range nur anzeigen wenn nötig
            Spinner<Integer> rangeSpinner = new Spinner<>(0, 999, entry.range);
            rangeSpinner.setEditable(true);
            rangeSpinner.setPrefWidth(70);

            Label feetLabel = new Label("ft");

            senseBox.setOnAction(e -> entry.sense = senseBox.getValue());

            rangeSpinner.valueProperty().addListener((obs, ov, nv) -> entry.range = nv);

            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                selectedNpc.senses.remove(entry);
                refreshSensesBox();
            });

            HBox row = new HBox(6, senseBox, rangeSpinner, feetLabel, removeBtn);
            sensesBox.getChildren().add(row);
        }
    }

    private void refreshActionsBox() {
        actionsBox.getChildren().clear();
        if (selectedNpc == null) return;

        for (String id : selectedNpc.actionIds) {
            Button chip = new Button(id + " ✕");
            chip.setOnAction(e -> {
                selectedNpc.actionIds.remove(id);
                refreshActionsBox();
            });
            actionsBox.getChildren().add(chip);
        }
    }

    private void addAttack() {
        if (selectedNpc == null) return;
        NpcBuild.NpcAttack attack = new NpcBuild.NpcAttack();
        selectedNpc.attacks.add(attack);
        refreshAttacksBox();
        logger.fine("Angriff hinzugefügt");
    }

    private void refreshAttacksBox() {
        attacksBox.getChildren().clear();
        if (selectedNpc == null) return;

        for (NpcBuild.NpcAttack attack : selectedNpc.attacks) {
            TextField nameField = new TextField(attack.name);
            nameField.setPromptText("Name");
            nameField.setPrefWidth(100);
            nameField.textProperty().addListener((obs, ov, nv) -> attack.name = nv.trim());

            ComboBox<ActionCost> costBox = new ComboBox<>();
            costBox.getItems().addAll(ActionCost.values());
            costBox.setValue(attack.cost);
            costBox.setOnAction(e -> attack.cost = costBox.getValue());

            Spinner<Integer> bonusSpinner = new Spinner<>(-10, 30, attack.bonus);
            bonusSpinner.setEditable(true);
            bonusSpinner.setPrefWidth(70);
            bonusSpinner.valueProperty().addListener((obs, ov, nv) -> attack.bonus = nv);

            TextField dmgField = new TextField(attack.damage);
            dmgField.setPromptText("1d6+2");
            dmgField.setPrefWidth(70);
            dmgField.textProperty().addListener((obs, ov, nv) -> attack.damage = nv.trim());

            ComboBox<DamageType> dmgTypeBox = new ComboBox<>();
            dmgTypeBox.getItems().addAll(DamageType.values());
            dmgTypeBox.setValue(attack.damageType != null ? attack.damageType : DamageType.PIERCING);
            dmgTypeBox.setOnAction(e -> attack.damageType = dmgTypeBox.getValue());

            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                selectedNpc.attacks.remove(attack);
                refreshAttacksBox();
            });

            HBox row = new HBox(6, nameField, costBox, new Label("+"), bonusSpinner,
                    new Label("DMG"), dmgField, dmgTypeBox, removeBtn);
            attacksBox.getChildren().add(row);
        }
    }

    private void refreshImmunitiesBox(VBox box) {
        box.getChildren().clear();
        if (selectedNpc == null) return;

        for (int i = selectedNpc.immunities.size() - 1; i >= 0; i--) {
            int idx = i;
            ComboBox<DamageType> typeBox = new ComboBox<>();
            typeBox.getItems().addAll(DamageType.values());
            typeBox.setValue(selectedNpc.immunities.get(idx));
            typeBox.setOnAction(e ->
                    selectedNpc.immunities.set(idx, typeBox.getValue()));

            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                selectedNpc.immunities.remove(idx);
                refreshImmunitiesBox(box);
            });

            box.getChildren().add(new HBox(4, typeBox, removeBtn));
        }
    }

    private void refreshModifierBox(VBox box, List<DamageModifier> list) {
        box.getChildren().clear();
        if (selectedNpc == null) return;

        for (int i = list.size() - 1; i >= 0; i--) {
            int idx = i;
            DamageModifier mod = list.get(idx);

            ComboBox<DamageType> typeBox = new ComboBox<>();
            typeBox.getItems().addAll(DamageType.values());
            typeBox.setValue(mod.type);
            typeBox.setOnAction(e -> mod.type = typeBox.getValue());

            Spinner<Integer> valueSpinner = new Spinner<>(0, 99, mod.value);
            valueSpinner.setEditable(true);
            valueSpinner.setPrefWidth(65);
            valueSpinner.valueProperty().addListener((obs, ov, nv) -> mod.value = nv);

            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                list.remove(idx);
                refreshModifierBox(box, list);
            });

            box.getChildren().add(new HBox(4, typeBox, valueSpinner, removeBtn));
        }
    }

    private Spinner<Integer> makeCombatSpinner(int min, int max, int initial) {
        Spinner<Integer> s = new Spinner<>(min, max, initial);
        s.setEditable(true);
        s.setPrefWidth(80);
        return s;
    }

    private void addTrait(String trait) {
        if (trait.isBlank() || selectedNpc == null) return;
        if (selectedNpc.traits.contains(trait)) return;
        selectedNpc.traits.add(trait);
        traitInput.clear();
        refreshTraitPane();
        logger.fine("Trait hinzugefügt: " + trait);
    }

    private void addRecall() {
        if (selectedNpc == null) return;
        RecallKnowledge rk = new RecallKnowledge();
        selectedNpc.recallKnowledge.add(rk);
        refreshRecallBox();
        logger.fine("Recall Knowledge hinzugefügt");
    }

    private void refreshRecallBox() {
        recallBox.getChildren().clear();
        if (selectedNpc == null) return;

        for (RecallKnowledge rk : selectedNpc.recallKnowledge) {
            ComboBox<Skill> skillBox = new ComboBox<>();
            skillBox.getItems().addAll(Skill.values());
            skillBox.setValue(rk.skill);
            skillBox.setOnAction(e -> rk.skill = skillBox.getValue());

            Spinner<Integer> dcSpinner = new Spinner<>(1, 99, rk.dc);
            dcSpinner.setEditable(true);
            dcSpinner.setPrefWidth(70);
            dcSpinner.valueProperty().addListener((obs, ov, nv) -> rk.dc = nv);

            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                selectedNpc.recallKnowledge.remove(rk);
                refreshRecallBox();
            });

            HBox row = new HBox(6, skillBox, new Label("DC"), dcSpinner, removeBtn);
            recallBox.getChildren().add(row);
        }
    }

    private void refreshTraitPane() {
        traitPane.getChildren().clear();
        if (selectedNpc == null) return;
        for (String trait : selectedNpc.traits) {
            Button chip = new Button(trait + " ✕");
            chip.setOnAction(e -> {
                selectedNpc.traits.remove(trait);
                refreshTraitPane();
            });
            traitPane.getChildren().add(chip);
        }
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew());

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox btnRow = new HBox(4, newBtn, deleteBtn);

        // --- Suche ---
        searchField = new TextField();
        searchField.setPromptText("Name oder Trait...");

        // --- Level Filter ---
        // -1 = alle Level anzeigen
        Label levelLabel = new Label("Level");
        levelFilterSpinner = new Spinner<>(-1, 25, -1);
        levelFilterSpinner.setEditable(true);
        levelFilterSpinner.setPrefWidth(70);

        HBox levelRow = new HBox(6, levelLabel, levelFilterSpinner);

        // --- Gefilterte + sortierte Liste ---
        FilteredList<NpcBuild> filtered = new FilteredList<>(npcs, n -> true);

        Runnable applyFilter = () -> {
            String text = searchField.getText().trim().toLowerCase();
            int level = levelFilterSpinner.getValue();

            filtered.setPredicate(n -> {
                // Level-Filter (-1 = alle)
                boolean levelMatch = level == -1 || n.level == level;

                // Name oder Trait
                boolean textMatch = text.isBlank()
                        || n.name.toLowerCase().contains(text)
                        || n.traits.stream().anyMatch(t -> t.toLowerCase().contains(text));

                return levelMatch && textMatch;
            });
        };

        searchField.textProperty().addListener((obs, ov, nv) -> applyFilter.run());
        levelFilterSpinner.valueProperty().addListener((obs, ov, nv) -> applyFilter.run());

        // --- ListView mit Level-Anzeige ---
        npcListView = new ListView<>(filtered);
        npcListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NpcBuild n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setText(null);
                    return;
                }
                setText("[" + n.level + "] " + n.name);
            }
        });
        npcListView.setOnMouseClicked(e -> {
            NpcBuild n = npcListView.getSelectionModel().getSelectedItem();
            if (n != null) loadNpc(n);
        });
        VBox.setVgrow(npcListView, Priority.ALWAYS);

        VBox panel = new VBox(6, btnRow, searchField, levelRow, npcListView);
        panel.setPadding(new Insets(8));
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxHeight(Double.MAX_VALUE);
        return panel;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }


    private void sortNpcs() {
        List<NpcBuild> sorted = new ArrayList<>(npcs);
        sorted.sort((a, b) -> {
            // erst Level, dann Name
            if (a.level != b.level) return Integer.compare(a.level, b.level);
            return a.name.compareToIgnoreCase(b.name);
        });
        NpcBuild current = selectedNpc;
        npcs.setAll(sorted);
        if (current != null) npcListView.getSelectionModel().select(current);
        logger.fine("NPCs sortiert: " + npcs.size());
    }

    private void saveToDisk() {
        try {
            EditorIO.save(FILE, new ArrayList<>(npcs));
            sortNpcs();
            logger.info("NPCs gespeichert: " + npcs.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    public static List<NpcBuild> loadNpcsfromDisk(){
        return EditorIO.load(FILE,
                new TypeReference<>() {
                }, new ArrayList<>());
    }

    private void loadFromDisk() {
        List<NpcBuild> loaded = EditorIO.load(FILE,
                new TypeReference<>() {
                }, new ArrayList<>());
        if (loaded.isEmpty()) {
            NpcBuild neu = new NpcBuild();
            neu.name = "Neuer NPC";
            loaded.add(neu);
            logger.info("Keine NPCs gefunden — leerer NPC erstellt");
        }
        npcs.setAll(loaded);
        sortNpcs();
        loadNpc(npcs.getFirst());
        logger.info("NPCs geladen: " + npcs.size());
    }

    private void createNew() {
        NpcBuild n = new NpcBuild();
        n.name = "Neuer NPC";
        npcs.add(n);
        sortNpcs();
        npcListView.getSelectionModel().select(n);
        loadNpc(n);
        logger.info("Neuer NPC angelegt");
    }

    private void deleteSelected() {
        if (selectedNpc == null) return;
        npcs.remove(selectedNpc);
        selectedNpc = null;
        saveToDisk();
        logger.info("NPC gelöscht");
    }

    private void loadNpc(NpcBuild n) {
        selectedNpc = n;

        npcNameField.setText(n.name);
        levelSpinner.getValueFactory().setValue(n.level);
        sizeBox.setValue(n.size);
        refreshTraitPane();
        refreshRecallBox();
        refreshSensesBox();

        statSpinners.get(AbilityScore.STR).getValueFactory().setValue(n.str);
        statSpinners.get(AbilityScore.DEX).getValueFactory().setValue(n.dex);
        statSpinners.get(AbilityScore.CON).getValueFactory().setValue(n.con);
        statSpinners.get(AbilityScore.INT).getValueFactory().setValue(n.intel);
        statSpinners.get(AbilityScore.WIS).getValueFactory().setValue(n.wis);
        statSpinners.get(AbilityScore.CHA).getValueFactory().setValue(n.cha);

        skillSpinners.forEach((skill, spinner) ->
                spinner.getValueFactory().setValue(n.skills.getOrDefault(skill, 0)));

        acSpinner.getValueFactory().setValue(n.ac);
        hpSpinner.getValueFactory().setValue(Math.max(1, n.hp));
        speedSpinner.getValueFactory().setValue(n.speed);
        fortSpinner.getValueFactory().setValue(n.fortitude);
        refSpinner.getValueFactory().setValue(n.reflex);
        willSpinner.getValueFactory().setValue(n.will);
        perceptionSpinner.getValueFactory().setValue(n.perception);

        refreshImmunitiesBox(immunitiesBox);
        refreshModifierBox(resistancesBox, n.resistances);
        refreshModifierBox(weaknessesBox, n.weaknesses);
        refreshAttacksBox();
        refreshActionsBox();

        if (n.tokenPath != null && !n.tokenPath.isBlank()) {
            tokenPathLabel.setText(n.tokenPath);
            String url = PathResolver.resolveString(n.tokenPath);
            if (url != null) loadTokenPreview(url);
        } else {
            tokenPathLabel.setText("(kein Pfad)");
            tokenPreview.setGraphic(null);
            tokenPreview.setText("[ Kein Token ]");
        }

        logger.fine("NPC geladen: " + n.name);
    }

    @Override
    public void onActivate() {
        loadFromDisk();
    }
}
