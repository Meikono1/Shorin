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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Charaktererstellung {

    Text b = SceneBuilder.makeText();
    private Pane pane;


    public Charaktererstellung(int stage) {


        VBox controls = new VBox();
        HBox rowone = SceneBuilder.makeButtonrow();
        HBox rowtwo = SceneBuilder.makeButtonrow();
        HBox rowthree = SceneBuilder.makeButtonrow();

        controls.getChildren().addAll(rowone, rowtwo, rowthree);


        TextFlow spieltext = SceneBuilder.mainFlow();


        if (stage == 1) {//intro
            StringBuilder name;
            name = new StringBuilder();
            name.append("Welcome to the World of Shorin\n\n");
            name.append("In this world you are a 19 Years old Soldier, who just signed for his First official Mission.\nYour name is ...");
            name.append(" ... ");
            Text a = SceneBuilder.makeText();
            a.setText(name.toString());

            spieltext.getChildren().addAll(a);
        }

        if (stage == 2) {//intro to surroundings

            Text a = SceneBuilder.makeText();
            String Intro = "Welcome to the World of Shorin\n\n" +
                    "In this world you are a 19 Years old Soldier, who just signed for his First official Mission.\nYour name is " +
                    Game.spieler.getName() + ".\n" +
                    "You live in a divided world with many Races.\n\n" +
                    "Dryads: 2.2 Meters high, thin Creatures living in the Woods. The Relationship between Humans and Dryads is complicated at least but" +
                    "both Races swore to never inflict damage on the other." +
                    "But Humans have cut down many trees in order to use the place for agricultur.\n\n" +
                    "Gnomes: Small 0.85 Meters humanoids, living in the Mountains. They have a secret for creating tools and Weapons what makes them the most advanced beings." +
                    "We depend on a good relationship for our farms and military\n\n" +
                    "Orcs: Physical strong Beasts living on the other side of the River fen. We use them as Slaves on our Farms and to fish on the lake Tribar. We live in a cold war since the";
            a.setText(Intro);

            Text b = SceneBuilder.makeText();
            b.setFill(GameOptionen.highlightRed);
            b.setText("Birth of Magic\n");

            Text c = SceneBuilder.makeText();
            c.setText("\nThere are more Races but they are not Importend right now.");


            spieltext.getChildren().addAll(a, b, c);


        }

        if (stage == 3) {//home
            StringBuilder home = new StringBuilder();
            home.append("You live in the city of Whitebridge, the first city outside the Walls from Sudbury.\n");
            home.append("Here you live in a house near the Barracks, where you trained to bekome a Soldier.\n\n");

            Text a = SceneBuilder.makeText();
            a.setText(home.toString());

            Text b = SceneBuilder.makeText();
            b.setText("Your Mission:\n");
            b.setFill(GameOptionen.highlightBlue);

            home = new StringBuilder();

            home.append("In 14 Days there will be a Convoy Heading to Shallow-Mill. \nYou will Guard them on the journey und protect them if something happens.\n");
            home.append("There are no Dangers expected, but you will be near the ");

            Text c = SceneBuilder.makeText();
            c.setText(home.toString());
            c.setFill(GameOptionen.missionDescription);

            Text d = Game.getInstance().unbriddledland.getOrtText();

            home = new StringBuilder();
            home.append(" you may Encounter some Thieves, so prepare and be mindful. You should also read a Book about the Kitsune");

            Text e = SceneBuilder.makeText();
            e.setText(home.toString());
            e.setFill(GameOptionen.missionDescription);

            spieltext.getChildren().addAll(a, b, c, d, e);
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
            cont.setText("Continue");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(new Charaktererstellung(3).getPane())));

            rowone.getChildren().addAll(cont);
        }
        if (stage == 3) {
            Button cont = SceneBuilder.makeButton();
            cont.setText("Start you Story");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.getPane())));

            rowone.getChildren().addAll(cont);
        }


        pane = SceneBuilder.buildGameScene(rowone, rowtwo, rowthree, spieltext);

    }


    public Pane getPane() {
        return pane;
    }
}
