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
                    "In this world you are a 19 year old freshly trained soldier, who was just signed for his first official mission.\nYour name is " +
                    Game.spieler.getName() + ".\n" +
                    "You live in a divided world with many races. Here are some informations about some of them, you might meet others along the way.\n\n" +
                    "Dryads: ~2.2 meters high, thin creatures living in the woods. The relationship between Humans and Dryads is complicated to say the least, but " +
                    "both races swore to never inflict damage on the other. " +
                    "But Humans have cut down many trees in order to use the place for agriculture.\n\n" +
                    "Gnomes: ~0.85 meters high, humanoids, living in the mountains. They have a secret for creating powerful tools and weapons which makes them the most advanced beings. " +
                    "We depend on a good relationship for our farms and military.\n\n" +
                    "Orcs: Physical strong beasts living on the other side of the river Fen. We use them as slaves on our farms and to fish by the lake Tribar. We live in a cold war since the ";
            a.setText(Intro);

            Text b = SceneBuilder.makeText();
            b.setFill(GameOptionen.timestamp);
            b.setText("Birth of Magic.\n");

            Text c = SceneBuilder.makeText();
            c.setText("\n\nThere are more Races but they are not important right now.");


            spieltext.getChildren().addAll(a, b, c);


        }

        if (stage == 3) {//home
            StringBuilder home = new StringBuilder();
            home.append("You live in the city of Whitebridge, the first city outside the walls from Sudbury, the main capital.\n");
            home.append("Here you are living in a house near the barracks, where you voluntarily joined the army to become a soldier.\n\n");
            // TODO: 22.08.2019 Namen für die Armee einführen

            Text a = SceneBuilder.makeText();
            a.setText(home.toString());

            Text b = SceneBuilder.makeText();
            b.setText("Your mission:\n");
            b.setFill(GameOptionen.highlightBlue);

            home = new StringBuilder();

            home.append("In 14 days there will be a convoy heading to Shallow-Mill. \nYou will guard them on the journey und protect them if something should happen.\n");
            home.append("There are no dangers expected, but you will come close to the ");

            Text c = SceneBuilder.makeText();
            c.setText(home.toString());
            c.setFill(GameOptionen.missionDescription);

            Text d = Game.getInstance().unbridledland.getOrtText();

            home = new StringBuilder();
            home.append(".\nWatch out, you may encounter some thieves, so prepare and be mindful. You should also read a book about the Kitsune, as this is their homeland.");

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
            cont.setText("Start youe story");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.getPane())));

            rowone.getChildren().addAll(cont);
        }


        pane = SceneBuilder.buildGameScene(rowone, rowtwo, rowthree, spieltext);

    }


    public Pane getPane() {
        return pane;
    }
}
