package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.Spiel.Game;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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

        if(erste==null){
            erste=makeButtonrow();
        }
        int lauf = erste.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            erste.getChildren().add(a);
        }

        if(zweite==null){
            zweite=makeButtonrow();
        }
        lauf = zweite.getChildren().size();
        for (int i = 0; i < (7 - lauf); i++) {

            Button a = new Button();
            a.setPrefWidth(GameOptionen.buttonwidth);
            zweite.getChildren().add(a);

        }

        if(dritte==null){
            dritte=makeButtonrow();
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

        Label name = new Label();
        name.setTextFill(Paint.valueOf("868686"));
        name.setText(Game.getInstance().spieler.getName().getText());
        name.prefHeight(80);


        ImageView ich = new ImageView("/images/meikonol.jpg");
        ich.setFitHeight(200);
        ich.setFitWidth(200);

        ImageView inventory = new ImageView("/images/plastic_bag.png");
        inventory.setFitHeight(200);
        inventory.setFitWidth(200);

        ImageView map = new ImageView("/images/ShorinMap3.png");
        map.setFitHeight(200);
        map.setFitWidth(200);

        charakter.getChildren().addAll(name, ich, inventory, map);

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

    public static Text makeText(){
        Text text = new Text();
        text.setFont(Font.font("Cambria", 21));
        text.setFill(Paint.valueOf("868686"));
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
        button.setPrefWidth(GameOptionen.buttonwidth);
        return button;
    }


}
