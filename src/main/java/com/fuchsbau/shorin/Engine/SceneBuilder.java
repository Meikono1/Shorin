package com.fuchsbau.shorin.Engine;

import com.fuchsbau.shorin.Engine.Options.StyleOptions;
import com.fuchsbau.shorin.Engine.RPG.ScenarioDefinition;
import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Weapons.Weapon;
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

    public BorderPane buildShop(ScrollPane scrole, List<Item> liste) {
        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptions.height);
        haupt.setPrefWidth(GameOptions.width);
        haupt.setMaxHeight(GameOptions.height);
        haupt.setMaxWidth(GameOptions.width);
        haupt.setBackground(GameOptions.hintergrund);

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

        HBox box = new HBox();
        VBox pane = new VBox();

        for (Item item : liste) {
            pane.getChildren().add(createShopItem(item));
        }

        pane.getChildren().addAll(box);
        pane.setBackground(GameOptions.hintergrund);
        pane.setPrefHeight(800);
        pane.setPrefWidth(GameOptions.width);

        scrole.setContent(pane);

        VBox unten = new VBox();
        unten.getChildren().addAll(firstButtonrow, secondButtonrow);

        haupt.setCenter(scrole);
        haupt.setBottom(unten);

        return haupt;
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
        name.setTextFill(Game.getInstance().spieler.getText().getFill());
        name.setText(Game.getInstance().spieler.getText().getText());
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(GameOptions.imagewidth);
        name.prefHeight(GameOptions.imageheight);


        ImageView ich = new ImageView("/images/char.png");

        ich.setFitHeight(GameOptions.imageheight);
        ich.setFitWidth(GameOptions.imagewidth);

        ImageView inventory = new ImageView("/images/inv.png");
        inventory.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().inventory.getScene()));
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
        scrollPane.setBackground(GameOptions.hintergrund);

        return scrollPane;
    }

    public BorderPane makePlayerInventory(TreeMap<Item, Integer> itemMap, Armor head, Armor chest, Armor arms, Armor pants, Armor boots, Weapon weapon) {
        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptions.height);
        haupt.setPrefWidth(GameOptions.width);
        haupt.setMaxHeight(GameOptions.height);
        haupt.setMaxWidth(GameOptions.width);
        haupt.setBackground(GameOptions.hintergrund);

        //Linken Abschnitt, Stats und Equipment
        VBox left = new VBox();
        left.setPrefWidth(300);

        //Stats
        VBox stats = new VBox();
        stats.setPrefHeight(350);
        Text stat = makeText();
        stat.setText("Stats: ");
        Text health = makeText();
        health.setText("Health: " + Game.getInstance().spieler.getHealth());
        health.setFill(GameOptions.goodPaint);
        Text money = makeText("Currency: " + Game.getInstance().spieler.getMoney() + " " + GameOptions.currency);
        money.setFill(GameOptions.goldPaint);

        stats.getChildren().addAll(stat, health, money);

        //Equipment
        VBox equipt = new VBox();
        Text equ = makeText("Equipped: " + Game.getInstance().inventory.getStats());
        equipt.getChildren().addAll(equ);
        {
            equipt.getChildren().add(createEquipItem(head));
            equipt.getChildren().add(createEquipItem(chest));
            equipt.getChildren().add(createEquipItem(arms));
            equipt.getChildren().add(createEquipItem(pants));
            equipt.getChildren().add(createEquipItem(boots));
            equipt.getChildren().add(createEquipItem(weapon));
            //@TODO Methode für Equipte Items einfügen
        }

        left.getChildren().addAll(stats, equipt);

        //Center Abschnitt
        VBox center = new VBox();
        center.setPadding(GameOptions.padding);
        center.setBackground(GameOptions.hintergrund);

        //Beschreibung
        TextFlow flow = new TextFlow();
        flow.setPrefWidth(780);
        flow.setPrefHeight(350);

        Game.getInstance().spieler.makeBeschreibung(flow);

        ScrollPane pane = new ScrollPane();
        //TODO Color korrigieren

        // pane.prefHeight(350);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setStyle("-fx-background: rgb(19,20,28);\n -fx-background-color: rgb(19,20,28)");

        //Items abschnitt
        //Items einfügen in Liste
        VBox items = new VBox();
        items.minWidthProperty().bind(Bindings.subtract(pane.widthProperty(), 50));

        {
            for (Item item : itemMap.keySet()) {
                HBox next = createInventarItem(item, itemMap.get(item));
                items.getChildren().add(next);
            }
        }
        pane.setContent(items);

        center.getChildren().addAll(flow, pane);

        //Rechter Abschnitt
        VBox rechts = new VBox();
        rechts.setPrefWidth(250);

        //Relations
        VBox relations = new VBox();
        relations.setPrefHeight(350);
        Text rel = makeText();
        rel.setText("Relations: ");
        relations.getChildren().addAll(rel);

        //Follower
        VBox follower = new VBox();
        Text fol = makeText();
        fol.setText("Entourage: ");
        follower.getChildren().addAll(fol);

        rechts.getChildren().addAll(relations, follower);


        //Unteren Abschnitt
        //Knöpfe unten auffüllen.
        int lauf = firstButtonrow.getChildren().size();
        for (int i = 0; i < (6 - lauf); i++) {
            Button a = makeButton(1);
            firstButtonrow.getChildren().add(a);
        }
        Button option = makeButton(1);
        option.setText("Options");
        option.setOnMouseClicked(event -> {
            Main.getStage().setTitle("Shorin - Optionen");
            Main.getStage().setScene(Game.getInstance().optionen.getScene(1));
        });
        firstButtonrow.getChildren().add(option);


        haupt.setLeft(left);
        haupt.setCenter(center);
        haupt.setBottom(firstButtonrow);
        haupt.setRight(rechts);

        return haupt;
    }

    /***
     * creates entries for inventory equiped items
     * @param item the Item (Armor / Weapon)
     * @return the entry as a box
     */
    private VBox createEquipItem(Item item) {
        VBox back = new VBox();
        HBox oben = new HBox();
        Button use = makeItemButton();
        use.setText("Unequip");
        use.setOnMouseClicked(event -> {
            item.dequip();
            Main.getStage().setScene(Game.getInstance().inventory.getScene());
        });

        Text beschreibung = item.getText();
        beschreibung.prefWidth(150);
        beschreibung.minWidth(150);
        beschreibung.maxWidth(150);
        //beschreibung.setWrappingWidth(0);

        HBox size = new HBox();
        size.getChildren().add(beschreibung);
        size.setMinWidth(220);
        if (item.isBase()) {
            oben.getChildren().addAll(size);
        } else {
            oben.getChildren().addAll(size, use);
        }
        oben.setBackground(GameOptions.hintergrund);
        oben.setPadding(GameOptions.padding);

        Text unten;
        if (item instanceof Armor armor) {
            unten = makeText("  " + armor.armor + ",   " + armor.qualitaet + ",   " + armor.zustand);
        } else {
            unten = makeText("  0,   0,   0");
        }


        back.getChildren().addAll(oben, unten);

        return back;
    }

    private HBox createInventarItem(Item item, Integer anzahl) {
        HBox zurueck = new HBox();
        zurueck.setSpacing(10);
        Button use = makeItemButton();
        use.setText(item.getuseText());
        use.setOnMouseClicked(event -> {
            item.itemUse();
            Main.getStage().setScene(Game.getInstance().inventory.getScene());
        });


        Button delete = makeItemButton();
        delete.setText("Delete");

        if (GameOptions.delete) {
            delete.setStyle("-fx-padding: 3;" +
                    "-fx-border-style: solid inside;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-color: #400d21;");
        }

        delete.setOnMouseClicked(event -> {
            if (GameOptions.delete) {
                Game.getInstance().inventory.remove(item);
                Main.getStage().setScene(Game.getInstance().inventory.getScene());
            }
        });


        Text beschreibung = item.getText();

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        HBox size = new HBox();
        size.getChildren().add(beschreibung);

        HBox anz = new HBox();
        Text menge = makeText("   " + anzahl);

        anz.getChildren().add(menge);

        zurueck.getChildren().addAll(size, anz, filler, use, delete);
        zurueck.setPadding(GameOptions.padding);

        return zurueck;
    }

    private Button makeItemButton() {
        Button button = new Button();
        button.setMinWidth(GameOptions.itembuttonwidth);
        return button;
    }

    private HBox createShopItem(Item item) {
        HBox zurueck = new HBox();
        // Button use = SceneBuilder.makeButton();
        // use.setText("Buy");
        // TODO Button aussehen bearbeiten.
        // TODO Use button funktion geben, kaufen.
        zurueck.getChildren().addAll(item.getText()/*,use*/);

        return zurueck;
    }

    public Parent buildBuilderScene(TextFlow gametext) {
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

        if (gametext == null) {
            gametext = mainFlow();
        }

        VBox bottom = new VBox();
        bottom.setBackground(GameOptions.rowHintergrund);

        bottom.getChildren().addAll(firstButtonrow, secondButtonrow, thirdButtonrow);

        VBox charakter = new VBox();
        charakter.setPrefWidth(GameOptions.imagewidth + 15);
        charakter.setMaxWidth(GameOptions.imagewidth + 40);

        Label name = new Label();
        name.setFont(Font.font("Cambria", 22));
        name.setTextFill(Game.getInstance().spieler.getText().getFill());
        name.setText(Game.getInstance().spieler.getText().getText());
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(GameOptions.imagewidth);
        name.prefHeight(GameOptions.imageheight);

        Image image = new Image(
                Objects.requireNonNull(Main.class.getResourceAsStream("/images/char.png"))
        );

        ImageView ich = new ImageView(image);

        ich.setFitHeight(GameOptions.imageheight);
        ich.setFitWidth(GameOptions.imagewidth);

        image = new Image(
                Objects.requireNonNull(Main.class.getResourceAsStream("/images/inv.png"))
        );

        ImageView inventory = new ImageView(image);
        inventory.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().inventory.getScene()));
        inventory.setFitHeight(GameOptions.imageheight);
        inventory.setFitWidth(GameOptions.imagewidth);

        image = new Image(
                Objects.requireNonNull(Main.class.getResourceAsStream("/images/ShorinMap3.png"))
        );

        ImageView map = new ImageView(image);
        map.setFitHeight(GameOptions.imageheight);
        map.setFitWidth(GameOptions.imagewidth);

        charakter.getChildren().addAll(name, ich, inventory, map);
        charakter.setSpacing(10);

        VBox pane = new VBox();
        ScrollPane scrollPane = makeScrollpane();
        pane.getChildren().addAll(gametext);
        pane.setBackground(GameOptions.hintergrund);

        pane.prefWidthProperty().bind(Bindings.subtract(haupt.widthProperty(), 235));
        pane.prefHeightProperty().bind(haupt.heightProperty());
        pane.setPadding(new Insets(30));

        scrollPane.setContent(pane);

        haupt.setLeft(charakter);
        haupt.setBottom(bottom);
        haupt.setCenter(scrollPane);

        haupt.setBackground(GameOptions.hintergrund);
        return haupt;
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

    public void addElement(Node e, int row) {
        switch (row) {
            case 1: {
                firstButtonrow.getChildren().add(e);
                break;
            }
            case 2: {
                secondButtonrow.getChildren().add(e);
                break;
            }
            case 3: {
                thirdButtonrow.getChildren().add(e);
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

    public static ComboBox<String> makeDropdown() {
        ComboBox<String> box = new ComboBox<>();
        box.getStyleClass().add("creation-dropdown");
        box.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            Node arrow = box.lookup(".arrow");
            if (arrow == null) return;

            RotateTransition rt = new RotateTransition(Duration.millis(120), arrow);
            rt.setToAngle(isShowing ? 0 : 180); // passend zu deinem CSS
            rt.playFromStart();
        });
        return box;
    }
}
