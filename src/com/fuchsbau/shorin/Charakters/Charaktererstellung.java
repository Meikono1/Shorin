package com.fuchsbau.shorin.Charakters;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.SceneBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class Charaktererstellung {

    private Pane pane;


    public Charaktererstellung() {


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
        one.setPrefWidth(GameOptionen.buttonwidth);
        one.setText("Knopf Eins");

        Button two = new Button();
        two.setPrefWidth(GameOptionen.buttonwidth);
        two.setText("Knopf Zwei");

        Button three = new Button();
        three.setPrefWidth(GameOptionen.buttonwidth);
        three.setText("Knopf Drei");

        Button four = new Button();
        four.setPrefWidth(GameOptionen.buttonwidth);
        four.setText("Knopf Vier");

        Button five = new Button();
        five.setPrefWidth(GameOptionen.buttonwidth);
        five.setText("Knopf Fünf");

        Button six = new Button();
        six.setPrefWidth(GameOptionen.buttonwidth);
        six.setText("Knopf Sechs");

        rowone.getChildren().addAll(one, two, three, four, five, six);
        rowtwo.getChildren().addAll(five, six);


        Label spieltext = new Label();
        spieltext.setText("dies ist ein test text der die länge eines gegebenen textes automatisch auf ein gewisses format bringen soll. ich hoffe das sich das wiederspiegelt in allen Designs und das alles richtig funktioniert. test.test.test.test. . ... Meikono ist toll Pakz ist besser als Meikono und sowieso der beste der existiert.");
        spieltext.setPadding(new Insets(20, 100, 0, 100));
        spieltext.setMaxWidth(GameOptionen.width - 200);
        spieltext.setFont(Font.font("Cambria", 18));

        spieltext.setWrapText(true);


        pane = SceneBuilder.buildGameScene(rowone, rowtwo, rowthree, spieltext);

    }


    public Pane getPane() {
        return pane;
    }
}
