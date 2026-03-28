package com.fuchsbau.shorin.Engine.Editor.Module.Actions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuchsbau.shorin.Engine.Editor.IO.EditorIO;
import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import com.fuchsbau.shorin.Engine.System.*;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ActionModule implements EditorModule {

    private final Logger logger = FileLogger.getLogger();

    private static final String FILE = "Engine/actions.json";

    private ListView<GameAction> actionListView;
    private final ObservableList<GameAction> actions = FXCollections.observableArrayList();
    private GameAction selectedAction = null;

    private TextField actionNameField = new TextField();
    private ComboBox<ActionCategory> categoryBox = new ComboBox<>();
    private ComboBox<ActionCost> costBox = new ComboBox<>();

    private TextArea descriptionArea = new TextArea();
    private TextField traitInput = new TextField();
    private FlowPane traitPane = new FlowPane();

    private TextField triggerField = new TextField();
    private TextField requirementsField = new TextField();

    private CheckBox hasDCBox = new CheckBox();
    private ComboBox<DCType> dcTypeBox = new ComboBox<>();
    private Spinner<Integer> fixedDCSpinner = new Spinner<>(1, 99, 15);
    private ComboBox<AbilityScores> dcStatBox = new ComboBox<>();

    private TextArea critSuccessArea = new TextArea();
    private TextArea successArea = new TextArea();
    private TextArea failureArea = new TextArea();
    private TextArea critFailureArea = new TextArea();

    @Override
    public String getTitle() {
        return "Actions";
    }

    @Override
    public Node buildContent() {
        VBox sectionsCol = new VBox(12);
        sectionsCol.setPadding(new Insets(8));
        sectionsCol.setFillWidth(true);

        sectionsCol.getChildren().addAll(
                buildNameSection(),
                buildDescriptionSection(),
                buildTraitsSection(),
                buildTriggerSection(),
                buildDCSection(),
                buildDegreeOfSuccessSection()
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
        actionNameField = new TextField();
        actionNameField.setPromptText("Aktionsname...");
        actionNameField.setMaxWidth(Double.MAX_VALUE);
        actionNameField.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) {
                selectedAction.name = nv.trim();
                logger.fine("Action Name: " + nv);
            }
        });

        categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(ActionCategory.values());
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        categoryBox.setOnAction(e -> {
            if (selectedAction != null) {
                selectedAction.category = categoryBox.getValue();
                logger.fine("Kategorie: " + categoryBox.getValue());
            }
        });

        costBox = new ComboBox<>();
        costBox.getItems().addAll(ActionCost.values());
        costBox.setMaxWidth(Double.MAX_VALUE);
        costBox.setOnAction(e -> {
            if (selectedAction != null) {
                selectedAction.cost = costBox.getValue();
                logger.fine("Kosten: " + costBox.getValue());
            }
        });

        HBox row = new HBox(8,
                new Label("Kategorie"), categoryBox,
                new Label("Kosten"), costBox
        );

        return buildSection("Action", actionNameField, row);
    }

    private Node buildDescriptionSection() {
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Beschreibung...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(120);
        descriptionArea.setMaxWidth(Double.MAX_VALUE);
        descriptionArea.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) {
                selectedAction.description = nv;
                logger.fine("Beschreibung gesetzt: " + selectedAction.name);
            }
        });

        return buildSection("Beschreibung", descriptionArea);
    }

    private Node buildTraitsSection() {
        traitPane = new FlowPane(4, 4);

        traitInput = new TextField();
        traitInput.setPromptText("Trait hinzufügen...");
        traitInput.setOnAction(e -> addTrait(traitInput.getText().trim()));

        Button addBtn = new Button("+ Hinzufügen");
        addBtn.setOnAction(e -> addTrait(traitInput.getText().trim()));

        HBox inputRow = new HBox(4, traitInput, addBtn);
        HBox.setHgrow(traitInput, Priority.ALWAYS);

        return buildSection("Traits", traitPane, inputRow);
    }

    private Node buildTriggerSection() {
        triggerField = new TextField();
        triggerField.setPromptText("Trigger (nur bei Reaction)...");
        triggerField.setMaxWidth(Double.MAX_VALUE);
        triggerField.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) {
                selectedAction.trigger = nv.trim();
                logger.fine("Trigger: " + nv);
            }
        });

        requirementsField = new TextField();
        requirementsField.setPromptText("Voraussetzungen...");
        requirementsField.setMaxWidth(Double.MAX_VALUE);
        requirementsField.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) {
                selectedAction.requirements = nv.trim();
                logger.fine("Requirements: " + nv);
            }
        });

        return buildSection("Trigger & Requirements",
                new Label("Trigger"), triggerField,
                new Label("Requirements"), requirementsField
        );
    }

    private Node buildDCSection() {
        hasDCBox = new CheckBox("Hat DC");
        hasDCBox.setOnAction(e -> {
            if (selectedAction != null) {
                selectedAction.hasDC = hasDCBox.isSelected();
                updateDCVisibility();
                logger.fine("HasDC: " + hasDCBox.isSelected());
            }
        });

        dcTypeBox = new ComboBox<>();
        dcTypeBox.getItems().addAll(DCType.values());
        dcTypeBox.setOnAction(e -> {
            if (selectedAction != null) {
                selectedAction.dcType = dcTypeBox.getValue();
                updateDCVisibility();
            }
        });

        fixedDCSpinner = new Spinner<>(1, 99, 15);
        fixedDCSpinner.setEditable(true);
        fixedDCSpinner.valueProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) selectedAction.fixedDC = nv;
        });

        dcStatBox = new ComboBox<>();
        dcStatBox.getItems().addAll(AbilityScores.values());
        dcStatBox.setOnAction(e -> {
            if (selectedAction != null) selectedAction.dcStat = dcStatBox.getValue();
        });

        HBox dcRow = new HBox(8, dcTypeBox, fixedDCSpinner, dcStatBox);
        updateDCVisibility();

        return buildSection("DC", hasDCBox, dcRow);
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        // --- Buttons ---
        Button newBtn = new Button("+ Neu");
        newBtn.setMaxWidth(Double.MAX_VALUE);
        newBtn.setOnAction(e -> createNew());

        Button deleteBtn = new Button("✕ Löschen");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox btnRow = new HBox(4, newBtn, deleteBtn);

        // --- Suche ---
        TextField search = new TextField();
        search.setPromptText("Name, Trait, Beschreibung...");

        // --- Filter ---
        Label filterLabel = new Label("Filter");
        FlowPane filterPane = new FlowPane(4, 4);

        ToggleGroup filterGroup = new ToggleGroup();

        // "Alle" als erster Filter
        ToggleButton allBtn = new ToggleButton("Alle");
        allBtn.setToggleGroup(filterGroup);
        allBtn.setSelected(true);
        filterPane.getChildren().add(allBtn);

        for (ActionCategory cat : ActionCategory.values()) {
            ToggleButton btn = new ToggleButton(cat.displayName());
            btn.setToggleGroup(filterGroup);
            filterPane.getChildren().add(btn);
        }

        // --- Gefilterte Liste ---
        FilteredList<GameAction> filtered = new FilteredList<>(actions, a -> true);

        // Filter + Suche kombiniert
        Runnable applyFilter = () -> {
            Toggle active = filterGroup.getSelectedToggle();
            String searchText = search.getText().trim().toLowerCase();

            filtered.setPredicate(a -> {
                // Kategorie-Filter
                boolean catMatch = active == allBtn || active == null ||
                        (active instanceof ToggleButton tb &&
                                tb.getText().equals(
                                        a.category != null ? a.category.displayName() : ""));

                // Suche — Name, Traits, Beschreibung
                boolean textMatch = searchText.isBlank() ||
                        a.name.toLowerCase().contains(searchText) ||
                        a.description.toLowerCase().contains(searchText) ||
                        a.traits.stream().anyMatch(t -> t.toLowerCase().contains(searchText));

                return catMatch && textMatch;
            });
        };

        search.textProperty().addListener((obs, ov, nv) -> applyFilter.run());
        filterGroup.selectedToggleProperty().addListener((obs, ov, nv) -> applyFilter.run());

        actionListView = new ListView<>(filtered);
        actionListView.setOnMouseClicked(e -> {
            GameAction a = actionListView.getSelectionModel().getSelectedItem();
            if (a != null) loadAction(a);
        });
        VBox.setVgrow(actionListView, Priority.ALWAYS);

        VBox panel = new VBox(6, btnRow, search, filterLabel, filterPane, actionListView);
        panel.setPadding(new Insets(8));
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxHeight(Double.MAX_VALUE);
        return panel;
    }

    private Node buildDegreeOfSuccessSection() {
        critSuccessArea = makeDegreeArea("Kritischer Erfolg...");
        successArea = makeDegreeArea("Erfolg...");
        failureArea = makeDegreeArea("Misserfolg...");
        critFailureArea = makeDegreeArea("Kritischer Misserfolg...");

        critSuccessArea.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) selectedAction.criticalSuccess = nv;
        });
        successArea.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) selectedAction.success = nv;
        });
        failureArea.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) selectedAction.failure = nv;
        });
        critFailureArea.textProperty().addListener((obs, ov, nv) -> {
            if (selectedAction != null) selectedAction.criticalFailure = nv;
        });

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(4);
        grid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);

        grid.add(new Label("Kritischer Erfolg"), 0, 0);
        grid.add(new Label("Erfolg"), 1, 0);
        grid.add(critSuccessArea, 0, 1);
        grid.add(successArea, 1, 1);
        grid.add(new Label("Misserfolg"), 0, 2);
        grid.add(new Label("Kritischer Misserfolg"), 1, 2);
        grid.add(failureArea, 0, 3);
        grid.add(critFailureArea, 1, 3);

        return buildSection("Degree of Success", grid);
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    private void loadAction(GameAction a) {
        selectedAction = a;
        actionNameField.setText(a.name);
        descriptionArea.setText(a.description);
        categoryBox.setValue(a.category);
        costBox.setValue(a.cost);
        triggerField.setText(a.trigger);
        requirementsField.setText(a.requirements);
        hasDCBox.setSelected(a.hasDC);
        dcTypeBox.setValue(a.dcType);
        fixedDCSpinner.getValueFactory().setValue(a.fixedDC > 0 ? a.fixedDC : 15);
        dcStatBox.setValue(a.dcStat);
        updateDCVisibility();
        refreshTraitPane();
        critSuccessArea.setText(a.criticalSuccess);
        successArea.setText(a.success);
        failureArea.setText(a.failure);
        critFailureArea.setText(a.criticalFailure);
        logger.fine("Action geladen: " + a.name);
    }

    private void addTrait(String trait) {
        if (trait.isBlank() || selectedAction == null) return;
        if (selectedAction.traits.contains(trait)) return;

        selectedAction.traits.add(trait);
        traitInput.clear();
        refreshTraitPane();
        logger.fine("Trait hinzugefügt: " + trait);
    }

    private void refreshTraitPane() {
        traitPane.getChildren().clear();
        if (selectedAction == null) return;

        for (String trait : selectedAction.traits) {
            Button chip = new Button(trait + " ✕");
            chip.setOnAction(e -> {
                selectedAction.traits.remove(trait);
                refreshTraitPane();
                logger.fine("Trait entfernt: " + trait);
            });
            traitPane.getChildren().add(chip);
        }
    }

    private void updateDCVisibility() {
        boolean active = hasDCBox.isSelected();
        dcTypeBox.setVisible(active);
        boolean isFixed = active && dcTypeBox.getValue() == DCType.FIXED;
        boolean isStat = active && dcTypeBox.getValue() == DCType.SKILL;
        fixedDCSpinner.setVisible(isFixed);
        dcStatBox.setVisible(isStat);
    }

    private TextArea makeDegreeArea(String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefHeight(80);
        area.setMaxWidth(Double.MAX_VALUE);
        return area;
    }

    private void refreshList() {
        GameAction current = selectedAction;
        List<GameAction> sorted = new ArrayList<>(actions);
        sorted.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        actions.setAll(sorted);
        // Selektion wiederherstellen
        if (current != null) actionListView.getSelectionModel().select(current);
        logger.fine("Klassenliste aktualisiert: " + actions.size());
    }

    // --- Disk ---
    private void saveToDisk() {
        try {
            EditorIO.save(FILE, new ArrayList<>(actions));
            refreshList();
            logger.info("Actions gespeichert: " + actions.size());
        } catch (Exception ex) {
            logger.severe("Fehler beim Speichern: " + ex.getMessage());
        }
    }

    private void loadFromDisk() {
        List<GameAction> loaded = EditorIO.load(FILE,
                new TypeReference<>() {
                }, new ArrayList<>());
        actions.setAll(loaded);
        if (loaded.isEmpty()) {
            GameAction neu = new GameAction("Neue Action");
            loaded.add(neu);
            logger.info("Keine Action gefunden — leere Action erstellt");
        }

        actions.setAll(loaded);
        loadAction(actions.getFirst());
        logger.info("Action geladen: " + actions.size());
    }

    // --- Neue Action anlegen ---
    private void createNew() {
        GameAction ga = new GameAction("Neue Action");
        actions.add(ga);
        actionListView.getSelectionModel().select(ga);
        loadAction(ga);
        logger.info("Neue Action angelegt");
    }

    private void deleteSelected() {
        if (selectedAction == null) return;
        actions.remove(selectedAction);
        selectedAction = null;
        actionNameField.clear();
        descriptionArea.clear();
        saveToDisk();
    }

    @Override
    public void onActivate() {
        loadFromDisk();
    }
}
