package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Waffe;
import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
            pane.getChildren().add(createShopItem(iter.next()));
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

    public static BorderPane makePlayerInventory(HBox erste, List<Item> liste, List<Item> equip) {

        BorderPane haupt = new BorderPane();
        haupt.setPrefHeight(GameOptionen.height);
        haupt.setPrefWidth(GameOptionen.width);
        haupt.setMaxHeight(GameOptionen.height);
        haupt.setMaxWidth(GameOptionen.width);
        haupt.setBackground(GameOptionen.hintergrund);

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
        health.setFill(GameOptionen.goodPaint);

        stats.getChildren().addAll(stat, health);

        //Equipment
        VBox equipt = new VBox();
        Text equ = makeText();
        equ.setText("Equipped: ");
        equipt.getChildren().addAll(equ);
        {
            Iterator<Item> iter = equip.iterator();
            while (iter.hasNext()) {
                equipt.getChildren().add(createEquipItem(iter.next()));
                //@TODO Methode für Equipte Items einfügen
            }
        }

        left.getChildren().addAll(stats, equipt);

        //Center Abschnitt
        VBox center = new VBox();
        center.setPadding(GameOptionen.padding);
        center.setBackground(GameOptionen.hintergrund);
        center.setPrefWidth(780);

        //Beschreibung
        TextFlow flow = new TextFlow();
        flow.setPrefWidth(780);
        flow.setPrefHeight(350);
        Text allgemein = makeText();
        allgemein.setText("Spieler: \n" + Game.getInstance().spieler.getBeschreibung());
        //TODO spieler beschreibung generieren und einfügen.
        flow.getChildren().addAll(allgemein);

        //Items abschnitt
        //Items einfügen in Liste
        VBox items = new VBox();

        ScrollPane pane = new ScrollPane();
        //TODO Color korrigieren
        pane.prefHeight(350);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setStyle("-fx-background: rgb(19,20,28);\n -fx-background-color: rgb(19,20,28)");

        {
            Iterator<Item> iter = liste.iterator();
            while (iter.hasNext()) {
                HBox next = createInventarItem(iter.next());
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
        int lauf = erste.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {
            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            erste.getChildren().add(a);
        }


        haupt.setLeft(left);
        haupt.setCenter(center);
        haupt.setBottom(erste);
        haupt.setRight(rechts);

        return haupt;
    }

    private static HBox createEquipItem(Item item) {
        HBox zurueck = new HBox();
        Button use = SceneBuilder.makeItemButton();
        use.setText("Unequip");
        use.setOnMouseClicked(event -> {
            item.dequip();
            Main.getStage().setScene(Game.getInstance().inventory.getScene());
        });
        //TODO Button aussehen bearbeiten.
        Text beschreibung = item.getBeschreibung();
        beschreibung.setWrappingWidth(0);
        zurueck.getChildren().addAll(beschreibung, use);
        zurueck.setBackground(GameOptionen.hintergrund);
        zurueck.setPadding(GameOptionen.padding);

        return zurueck;
    }

    private static HBox createInventarItem(Item item) {
        HBox zurueck = new HBox();
        zurueck.setMinWidth(780);
        Button use = SceneBuilder.makeItemButton();
        use.prefWidth(230);
        use.setText(item.getuseText());
        use.setOnMouseClicked(event -> {
            item.itemUse();
            Main.getStage().setScene(Game.getInstance().inventory.getScene());
        });
        //TODO Button aussehen bearbeiten.
        Text beschreibung = item.getBeschreibung();
        beschreibung.setWrappingWidth(550);
        zurueck.getChildren().addAll(beschreibung, use);
        zurueck.setBackground(GameOptionen.hintergrund);
        zurueck.setPadding(GameOptionen.padding);

        return zurueck;
    }

    private static Button makeItemButton() {
        Button button = new Button();
        button.setMinWidth(GameOptionen.itembuttonwidth);
        return button;
    }

    private static HBox createShopItem(Item item) {
        HBox zurueck = new HBox();
        // Button use = SceneBuilder.makeButton();
        //use.setText("Buy");
        //TODO Button aussehen bearbeiten.
        //TODO Use button funktion geben, kaufen.
        zurueck.getChildren().addAll(item.getBeschreibung()/*,use*/);

        return zurueck;
    }


}
