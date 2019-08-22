package com.fuchsbau.shorin.Spiel.Intro;

import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class Charaktererstellung {

    private Pane pane;


    public Charaktererstellung(int stage) {


        VBox controls = new VBox();
        HBox rowone = SceneBuilder.makeButtonrow();
        HBox rowtwo = SceneBuilder.makeButtonrow();
        HBox rowthree = SceneBuilder.makeButtonrow();

        controls.getChildren().addAll(rowone, rowtwo, rowthree);


        Label spieltext = SceneBuilder.mainLabel();
        StringBuilder build = new StringBuilder();


        if (stage == 1) {//intro
            build.append("Welcome to the World of Shorin\n\n");
            build.append("In this world you are ");
            build.append(" ... ");
        }

        if (stage == 2 ) {//intro to surroundings
            build.append("Welcome to the World of Shorin\n\n");
            build.append("In this world you are ");
            build.append(Game.spieler.getName()).append(".\n");
            build.append("A Male Human Soldier ");
            build.append("that lives in a devided world. The only things wellknown to Humankind are the:\n\n");
            build.append("Dryads: 2.2 Meters high, thin Creatures living in the Woods. The Relationship between Humans and Dryads is complicated at least.");
            build.append("Both Races swore to never inflict demage on the other.");
            build.append("But Humans have the tendency to feed their people with Agriculture, wich uses space,that wildlive and nature could use.\n\n");
            build.append("Gnomes: Small 1.3 Meters high mountain dwellers. They have a secret creating tools and Weapons and they are by far he strongest beeings. ");
            build.append("not Physical but technological. \nWe depend on a good relationship for our farms and military");

        }

        if(stage == 3 ){//Human World
          //  build.append("");
        }



        TextField eingabe = new TextField();
        eingabe.setPrefWidth(GameOptionen.buttonwidth);
        if (stage == 1) {
            Label name = new Label();
            name.minWidth(GameOptionen.buttonwidth);
            name.setPrefWidth(GameOptionen.buttonwidth);
            name.setTextFill(Paint.valueOf("ffffff"));
            name.setText("Name :");


            Button annahme = SceneBuilder.makeButton();
            annahme.setText("Accept");
            annahme.setOnMouseClicked(event -> {

                Game.spieler.setName(eingabe.getText());
                Main.getStage().setScene(new Scene(new Charaktererstellung(2).getPane()));


            });

            rowone.getChildren().addAll(name, eingabe, annahme);
        }

        if (stage == 2) {
            Button cont = SceneBuilder.makeButton();
            cont.setText("Human World");
            cont.setOnMouseClicked(event -> {

                Main.getStage().setScene(new Scene(new Charaktererstellung(3).getPane()));

            });

            rowone.getChildren().addAll(cont);
        }
        if (stage == 3) {
            Button cont = SceneBuilder.makeButton();
            cont.setText("Your Story");
            cont.setOnMouseClicked(event -> {

                Main.getStage().setScene(new Scene(new Charaktererstellung(4).getPane()));

            });

            rowone.getChildren().addAll(cont);
        }


        spieltext.setText(build.toString());


        pane = SceneBuilder.buildGameScene(rowone, rowtwo, rowthree, spieltext);

    }


    public Pane getPane() {
        return pane;
    }
}
