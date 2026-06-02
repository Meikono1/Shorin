package com.fuchsbau.shorin.Engine;

import com.fuchsbau.shorin.Engine.Options.StyleOptions;
import com.fuchsbau.shorin.Engine.RPG.ScenarioDefinition;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.RPG.Game;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class SceneBuilder {
    private static SceneBuilder sceneBuilder;
    private final Paint primaryTextColourWhite = Paint.valueOf("ffffff");

    public final Paint strokeRGB = Color.rgb(17, 17, 24, 0.50);
    public final Paint whiteRGB = Color.rgb(200, 200, 220);
    public final Paint beigeRGB = Color.rgb(230, 230, 255, 0.90);
    public final Paint blackRGB = Color.rgb(10, 10, 16);
    public final Paint redRGB = Color.rgb(255, 60, 60, 0.9);
    public final Paint worldMapBlue = Color.rgb(114,194,207,0.8);

    private HBox firstButtonrow;
    private HBox secondButtonrow;
    private HBox thirdButtonrow;


    private SceneBuilder() {
        firstButtonrow = makeButtonrow();
        secondButtonrow = makeButtonrow();
        thirdButtonrow = makeButtonrow();
    }

    public static SceneBuilder getSceneBuilder() {
        if (sceneBuilder == null) {
            sceneBuilder = new SceneBuilder();
        }
        return sceneBuilder;
    }

    public static Label createHeaderLabel(String intro) {
        Label label = new Label(intro);
        label.setStyle("""
                -fx-font-size: %spx;
                -fx-font-weight: %s;
                """.formatted(
                StyleOptions.largeFontSize,
                StyleOptions.largeFontWeight
        ));
        label.setTextFill(Paint.valueOf("#ffffff"));

        return label;
    }

    public static Label createTextLabel(String intro) {
        Label label = new Label(intro);
        label.setStyle("""
                -fx-font-size: %spx;
                """.formatted(
                StyleOptions.baseFontSize
        ));
        label.setTextFill(Paint.valueOf("#ffffff"));

        return label;
    }

    public static ListView<ScenarioDefinition> createScenarioList() {
        ListView<ScenarioDefinition> listView = new ListView<>();
        listView.getStyleClass().add("list-view");

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ScenarioDefinition def, boolean empty) {
                super.updateItem(def, empty);
                getStyleClass().remove("unfinished");

                if (empty || def == null) {
                    setText(null);
                    return;
                }

                setText(def.name());

                if (def.finishState() == 0) {
                    getStyleClass().add("unfinished");
                }
            }
        });

        return listView;
    }

    /***
     * Baut die Borderpane auf die notfalls aufgefüllt wird, Bereits vorhanden sind die 3 Buttons links.
     * @param text Der Haupttext der ausgegeben wird
     * @return Die fertige Pane
     */
    public BorderPane buildGameScene(TextFlow text) {
        BorderPane haupt = new BorderPane();

        haupt.setPrefHeight(GameOptions.height);
        haupt.setPrefWidth(GameOptions.width);
        haupt.setMaxHeight(GameOptions.height);
        haupt.setMaxWidth(GameOptions.width);

        int lauf = firstButtonrow.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {
            Button a = makeButton(1);
            firstButtonrow.getChildren().add(a);
        }

        lauf = secondButtonrow.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = makeButton(2);
            secondButtonrow.getChildren().add(a);
        }

        lauf = thirdButtonrow.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = makeButton(3);
            thirdButtonrow.getChildren().add(a);
        }

        if (text == null) {
            text = mainFlow();
        }

        VBox unten = new VBox();
        unten.setBackground(GameOptions.rowHintergrund);

        unten.getChildren().addAll(firstButtonrow, secondButtonrow, thirdButtonrow);

        VBox charakter = new VBox();
        charakter.setPrefWidth(GameOptions.imagewidth + 15);
        charakter.setMaxWidth(GameOptions.imagewidth + 40);

        Label name = new Label();
        name.setFont(Font.font("Cambria", 22));
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(GameOptions.imagewidth);
        name.prefHeight(GameOptions.imageheight);


        ImageView ich = new ImageView("/images/char.png");

        ich.setFitHeight(GameOptions.imageheight);
        ich.setFitWidth(GameOptions.imagewidth);

        ImageView inventory = new ImageView("/images/inv.png");
        //inventory.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().inventory.getScene()));
        inventory.setFitHeight(GameOptions.imageheight);
        inventory.setFitWidth(GameOptions.imagewidth);

        ImageView map = new ImageView("/images/ShorinMap3.png");
        map.setFitHeight(GameOptions.imageheight);
        map.setFitWidth(GameOptions.imagewidth);

        charakter.getChildren().addAll(name, ich, inventory, map);
        charakter.setSpacing(10);

        VBox pane = new VBox();
        ScrollPane scrollPane = makeScrollpane();
        pane.getChildren().addAll(text);
        pane.setBackground(GameOptions.hintergrund);

        pane.prefWidthProperty().bind(Bindings.subtract(haupt.widthProperty(), 150));
        pane.prefHeightProperty().bind(haupt.heightProperty());

        scrollPane.setContent(pane);

        haupt.setLeft(charakter);
        haupt.setBottom(unten);
        haupt.setCenter(scrollPane);

        haupt.setBackground(GameOptions.hintergrund);
        return haupt;
    }

    public ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add(StyleOptions.ScrollPaneStyle.CLASS);
        return scrollPane;
    }

    private HBox makeButtonrow() {
        HBox box = new HBox();
        box.setPadding(GameOptions.padding);
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);

        return box;
    }

    public Text makeText() {
        Text text = new Text();
        text.setFont(Font.font("Cambria", GameOptions.textsize));
        text.setFill(Paint.valueOf("989898"));
        return text;
    }

    public Text makeText(String inhalt) {
        Text text = makeText();
        text.setText(inhalt);

        return text;
    }

    public TextFlow mainFlow() {
        TextFlow flow = new TextFlow();
        flow.setStyle("""
                -fx-font-size: %spx;
                """.formatted(
                StyleOptions.baseFontSize
        ));

        return flow;
    }

    public Button makeButton(int row, String text) {
        Button button = new Button();
        switch (row) {
            case 1: {
                button.prefWidthProperty().bind(Bindings.divide(firstButtonrow.widthProperty(), 7));
                break;
            }
            case 2: {
                button.prefWidthProperty().bind(Bindings.divide(secondButtonrow.widthProperty(), 7));
                break;
            }
            case 3: {
                button.prefWidthProperty().bind(Bindings.divide(thirdButtonrow.widthProperty(), 7));
                break;
            }
            default: {
                System.out.println("Error in SceneBuilder, makeButton(int row, String text)");
            }
        }
        button.setText(text);
        return button;
    }


    public Button makeButton(String label) {
        Button button = new Button(label);
        button.getStyleClass().add("stat-button");

        return button;
    }

    public Button makeActionButton(String key, String label) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(56);
        btn.getStyleClass().addAll("menu-button", "action-button");

        Label keyLabel = new Label(key);
        keyLabel.setTextFill(Color.rgb(150, 150, 180));
        keyLabel.setStyle("-fx-font-size: 15px;");
        keyLabel.setPadding(new Insets(2, 0, 0, 5));

        StackPane graphic = new StackPane(keyLabel);
        StackPane.setAlignment(keyLabel, Pos.TOP_LEFT);
        graphic.setMaxWidth(Double.MAX_VALUE);
        graphic.prefWidthProperty().bind(btn.widthProperty());
        graphic.setPrefHeight(14);

        btn.setGraphic(graphic);
        btn.setContentDisplay(ContentDisplay.TOP);
        return btn;
    }

    public Button makeButton(int row) {
        Button button = new Button();
        switch (row) {
            case 1: {
                button.prefWidthProperty().bind(Bindings.divide(firstButtonrow.widthProperty(), 7));
                break;
            }
            case 2: {
                button.prefWidthProperty().bind(Bindings.divide(secondButtonrow.widthProperty(), 7));
                break;
            }
            case 3: {
                button.prefWidthProperty().bind(Bindings.divide(thirdButtonrow.widthProperty(), 7));
                break;
            }
            default: {
                System.out.println("Error in SceneBuilder, makeButton(int row, String text)");
            }
        }
        return button;
    }


    public ScrollPane makeScrollpane() {

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //scrollPane.setBackground(GameOptions.hintergrund);

        return scrollPane;
    }

    public void resetButtonrows() {
        firstButtonrow = makeButtonrow();
        secondButtonrow = makeButtonrow();
        thirdButtonrow = makeButtonrow();
    }

    public void addButton(Button button, int row) {
        switch (row) {
            case 1: {
                firstButtonrow.getChildren().add(button);
                break;
            }
            case 2: {
                secondButtonrow.getChildren().add(button);
                break;
            }
            case 3: {
                thirdButtonrow.getChildren().add(button);
                break;
            }
            default: {
                System.out.println("error in SceneBuilder addButton");
            }
        }
    }

    public Scene makeGameOption(ScrollPane optionWindow) {
        BorderPane pane = new BorderPane();
        pane.setBottom(firstButtonrow);
        pane.setCenter(optionWindow);

        return new Scene(pane);
    }

    public Button createMenuButton(String label) {
        Button button = new Button(label);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public ToggleButton makeMenuToggleButton(String label) {
        ToggleButton button = new ToggleButton(label);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public Label makeWhiteLabel(String s) {
        Label label = new Label(s);
        label.setTextFill(primaryTextColourWhite);
        return label;
    }
}
