package com.fuchsbau.shorin.Charakters;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.SceneBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class Charaktererstellung {

    private Pane pane;


    public Charaktererstellung() {
        VBox charakter = new VBox();

        Label name = new Label();
        name.setText("Dein Name");
        name.prefHeight(80);


        ImageView ich = new ImageView("/images/meikonol.jpg");
        ich.setFitHeight(100);
        ich.setFitWidth(100);

        ImageView inventory = new ImageView("/images/plastic_bag.png");
        inventory.setFitHeight(100);
        inventory.setFitWidth(100);

        ImageView map = new ImageView("/images/map.jpg");
        map.setFitHeight(100);
        map.setFitWidth(100);

        charakter.getChildren().addAll(name, ich, inventory, map);

        VBox controls = new VBox();
        HBox rowone = new HBox();
        HBox rowtwo = new HBox();
        HBox rowthree = new HBox();

        rowone.setSpacing(10);
        rowone.setAlignment(Pos.CENTER);

        rowtwo.setSpacing(10);
        rowtwo.setAlignment(Pos.CENTER);

        rowthree.setSpacing(10);
        rowthree.setAlignment(Pos.CENTER);

        controls.getChildren().addAll(rowone, rowtwo, rowthree);

        Button one = new Button();
        one.setText("Knopf Eins");

        Button two = new Button();
        two.setText("Knopf Zwei");

        Button three = new Button();
        three.setText("Knopf Drei");

        Button four = new Button();
        four.setText("Knopf Vier");

        Button five = new Button();
        five.setText("Knopf Fünf");

        Button six = new Button();
        six.setText("Knopf Sechs");

        rowone.getChildren().addAll(one, two, three, four);
        rowtwo.getChildren().addAll(five, six);


        ScrollPane spiel = new ScrollPane();
        spiel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Label spieltext = new Label();
        spieltext.setText("dies ist ein test text der die länge eines gegebenen textes automatisch auf ein gewisses format bringen soll. ich hoffe das sich das wiederspiegelt in allen Designs und das alles richtig funktioniert. test.test.test.test. . ... Meikono ist toll Pakz ist besser als Meikono und sowieso der beste der existiert.");
        spieltext.setPadding(new Insets(20, 100, 0, 100));
        spieltext.setMaxWidth(GameOptionen.width - 200);
        spieltext.setFont(Font.font("Cambria", 18));
        spieltext.setWrapText(true);

        spiel.setContent(spieltext);


        pane = SceneBuilder.buildBorderPane(null, null, charakter, controls);
        ((BorderPane) pane).setCenter(spiel);

    }


    public Pane getPane() {
        return pane;
    }
}
