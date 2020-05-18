package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Items.Inventory;
import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Iterator;
import java.util.List;

public class SceneBuilder {

    public static BorderPane buildBorderPane(Node top, Node right, Node left, Node buttom) {
        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);

        if (top != null) {
            haupt.setTop(top);
        }
        if (buttom != null) {
            haupt.setBottom(buttom);
        }
        if (left != null) {
            haupt.setLeft(left);
        }
        if (right != null) {
            haupt.setRight(right);
        }

        return haupt;


    }

    public static BorderPane buildShop(HBox erste, HBox zweite, ScrollPane scrole, List<Item> liste) {
        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);
        haupt.setBackground(GameOptionen.hintergrund);

        int lauf = zweite.getChildren().size();
        for (int i = 0; i < 7 - lauf; i++) {
            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            zweite.getChildren().add(a);
        }

        lauf = erste.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {
            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            erste.getChildren().add(a);
        }

        HBox box = new HBox();

        VBox pane = new VBox();

        Iterator<Item> iter = liste.iterator();

        while (iter.hasNext()) {
            pane.getChildren().add(createItem(iter.next()));
        }

        pane.getChildren().addAll(box);
        pane.setBackground(GameOptionen.hintergrund);
        pane.setPrefHeight(800);
        pane.setPrefWidth(GameOptionen.width);

        scrole.setContent(pane);

        VBox unten = new VBox();
        unten.getChildren().addAll(erste, zweite);


        haupt.setCenter(scrole);
        haupt.setBottom(unten);

        return haupt;

    }

    /***
     * Baut die Borderpane auf die notfalls aufgefüllt wird, Bereits vorhanden sind die 3 Buttons links.
     * @param erste Buttenreihe 1
     * @param zweite Buttonreihe mitte
     * @param dritte Buttonreihe unten
     * @param text Der Haupttext der ausgegeben wird
     * @return Die fertige Pane
     */
    public static BorderPane buildGameScene(HBox erste, HBox zweite, HBox dritte, TextFlow text) {
        BorderPane haupt = new BorderPane();

        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);

        if (erste == null) {
            erste = makeButtonrow();
        }
        int lauf = erste.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            erste.getChildren().add(a);
        }

        if (zweite == null) {
            zweite = makeButtonrow();
        }
        lauf = zweite.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            zweite.getChildren().add(a);

        }

        if (dritte == null) {
            dritte = makeButtonrow();
        }
        lauf = dritte.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setMinWidth(GameOptionen.buttonwidth);
            dritte.getChildren().add(a);


        }

        VBox unten = new VBox();

        unten.getChildren().addAll(erste, zweite, dritte);

        VBox charakter = new VBox();
        charakter.setPrefWidth(GameOptionen.imagewidth + 15);

        Label name = new Label();
        name.setFont(Font.font("Cambria", 22));
        name.setTextFill(Paint.valueOf("868686"));
        name.setText(Game.getInstance().spieler.getName().getText());
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(GameOptionen.imagewidth);
        name.prefHeight(GameOptionen.imageheight);


        ImageView ich = new ImageView("/images/char.png");

        ich.setFitHeight(GameOptionen.imageheight);
        ich.setFitWidth(GameOptionen.imagewidth);

        ImageView inventory = new ImageView("/images/inv.png");
        inventory.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().inventory.getScene()));
        inventory.setFitHeight(GameOptionen.imageheight);
        inventory.setFitWidth(GameOptionen.imagewidth);

        ImageView map = new ImageView("/images/ShorinMap3.png");
        map.setFitHeight(GameOptionen.imageheight);
        map.setFitWidth(GameOptionen.imagewidth);

        charakter.getChildren().addAll(name, ich, inventory, map);
        charakter.setSpacing(10);

        haupt.setLeft(charakter);
        haupt.setBottom(unten);
        haupt.setCenter(text);

        haupt.setBackground(GameOptionen.hintergrund);
        return haupt;
    }

    public static HBox makeButtonrow() {
        HBox box = new HBox();
        box.setPadding(GameOptionen.padding);
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);


        return box;
    }

    public static Text makeText() {
        Text text = new Text();
        text.setFont(Font.font("Cambria", GameOptionen.textsize));
        text.setFill(Paint.valueOf("868686"));
        return text;
    }

    public static Text makeText(String inhalt) {
        Text text = makeText();
        text.setText(inhalt);

        return text;
    }

    public static TextFlow mainFlow() {
        TextFlow flow = new TextFlow();

        flow.setPadding(new Insets(20, 100, 0, 100));
        flow.setMaxHeight(Double.MIN_VALUE);
        flow.setMaxWidth(GameOptionen.width - 200);

        return flow;
    }

    public static Button makeButton() {
        Button button = new Button();
        button.setMinWidth(GameOptionen.buttonwidth);
        return button;
    }

    public static ScrollPane makeScrollpane() {

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(GameOptionen.hintergrund);

        return scrollPane;
    }

    public static BorderPane makePlayerInventory(HBox erste, ScrollPane scrole, List<Item> liste) {

        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);
        haupt.setBackground(GameOptionen.hintergrund);

        int lauf = erste.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {
            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            erste.getChildren().add(a);
        }

        HBox box = new HBox();

        Text a = makeText();
        a.setText("Blahblab          ");

        Text b = makeText();
        b.setText("Blahbdfgsfdhjhjfgdlab               ");
        Text c = makeText();
        c.setFill(Paint.valueOf("fcba03"));
        c.setText("Bsdasdlahblab");

        HBox boxd = new HBox();

        Text pakz = SceneBuilder.makeText();
        pakz.setText("Pakz <3<3<3    ");
        pakz.setFill(Paint.valueOf("fc0303"));

        boxd.getChildren().add(pakz);

        box.getChildren().addAll(a, b);

        HBox boxt = new HBox();
        boxt.getChildren().add(c);

        VBox pane = new VBox();

        Iterator<Item> iter = liste.iterator();

        while (iter.hasNext()) {
            pane.getChildren().add(createItem(iter.next()));
        }


        pane.getChildren().addAll(box, boxt, boxd);
        pane.setBackground(GameOptionen.hintergrund);
        pane.setPrefHeight(800);
        pane.setPrefWidth(GameOptionen.width);

        scrole.setContent(pane);

        haupt.setCenter(scrole);
        haupt.setBottom(erste);

        return haupt;
    }

    private static HBox createItem(Item item) {
        HBox zurueck = new HBox();
        zurueck.getChildren().addAll(item.getBeschreibung());

        return zurueck;
    }


}
