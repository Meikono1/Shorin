package com.fuchsbau.shorin.Spiel.Intro;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Charaktererstellung implements Saveble {

    private Scene scene;


    public void makeScene(int stage) {
        VBox controls = new VBox();
        HBox erste = SceneBuilder.makeButtonrow();
        HBox zweite = SceneBuilder.makeButtonrow();
        HBox dritte = SceneBuilder.makeButtonrow();

        controls.getChildren().addAll(erste, zweite, dritte);
        TextFlow spieltext = SceneBuilder.mainFlow();



        if (stage==1){
            {
                StringBuilder name;
                name = new StringBuilder();
                name.append("Welcome to the World of Shorin\n\n");
                name.append("You are a male Human Soldier. ");

                name.append("\nYour name is ... ");

                //@TODO Check Text
                Text a = SceneBuilder.makeText();
                a.setText(name.toString());

                spieltext.getChildren().addAll(a);
            }

            Label name = new Label();
            name.prefWidthProperty().bind(Bindings.divide(erste.widthProperty(), 7));
            name.setTextFill(Paint.valueOf("ffffff"));
            name.setText("Name :");

            TextField eingabe = new TextField();
            eingabe.prefWidthProperty().bind(Bindings.divide(erste.widthProperty(), 7));

            Button annahme = SceneBuilder.makeButton(erste);
            annahme.setText("Accept");
            annahme.setOnMouseClicked(event -> {
                if (eingabe.getText().equals("")) {
                    Game.getInstance().spieler.setName("Jan");
                } else {
                    Game.getInstance().spieler.setName(eingabe.getText());
                }
                Main.getStage().setScene(getScene(2));
            });
            erste.getChildren().addAll(name, eingabe, annahme);
        }


        if (stage == 2) {//intro to you
            Text a = SceneBuilder.makeText();
            a.setText("Welcome to the World of Shorin\n\nYou are a 18 year old freshly trained soldier, who was just assigned for his first official mission.\nYour name is ");

            Text b = Game.getInstance().spieler.getName();

            Text c = SceneBuilder.makeText();
            //@TODO check Text
            Text d = SceneBuilder.makeText("\n\n14 Years ago your Parents died in the Great war when you were only 4 Years old. You didn't knew them well, but people say, they were brave Soldiers fighting for Humanity.\nYoshua ,a Familyfriend, took you in and treated you like his own Son.\nHe Loved to tell stories about your Parents and the war, but since a few Month he grew Tired and wants Peace in his Life. \n\nThe Story about the death of your Parents, marked you the most.\nYour wish is to take revange and defend Humanity against the Evil forces across the Rivers: Fen and Hedge.\n\n");

            Text e = SceneBuilder.makeText("At the Age of 12 you officially joined the army under the Command of Yoshua. Since then you learned the local courtesy, how to Survive in the wild, make a Fire and to fight against Humanoids.\nYou spent most of the Time clearing disputes and Helping the Locals. But finally you can leave this City and change your live.");


            /*TODO aufräumen in book
            c.setText(".\n" + "You live in a divided world with many races. Here are some informations about some of them, you might meet others along the way.\n\n" +
                    "Dryads: ~2.2 meters high, thin creatures living in the woods. The relationship between Humans and Dryads is complicated to say the least, but " +
                    "both races swore to never inflict damage on the other. " +
                    "But Humans have cut down many trees in order to use the place for agriculture.\n\n" +
                    "Gnomes: ~0.85 meters high, humanoids, living in the mountains. They have a secret for creating powerful tools and weapons which makes them the most advanced beings. " +
                    "We depend on a good relationship for our farms and military.\n\n" +
                    "Orcs: Physical strong beasts living on the other side of the river Fen. We use them as slaves on our farms and to fish by the lake Tribar. We live in a cold war since the ");

            Text d = Game.getInstance().birthofMagic.getName();

            Text e = SceneBuilder.makeText();
            e.setText(".\n\nThere are more Races but they are not important right now.");
             */
            spieltext.getChildren().addAll(a, b, c, d, e);

            Button cont = SceneBuilder.makeButton(erste);
            cont.setText("Continue");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(getScene(3)));

            erste.getChildren().addAll(cont);
        }

        if (stage == 3) {//home

            Text a = SceneBuilder.makeText();
            a.setText("You currently live in the city of ");

            Text b = Game.getInstance().whitebridge.getOrtText();

            Text c = SceneBuilder.makeText();//TODO check text
            c.setText(", the first city outside the walls from ");

            Text d = Game.getInstance().sudbury.getOrtText();

            Text e = SceneBuilder.makeText();
            e.setText(", the main capital. ");

            Text f = SceneBuilder.makeText();
            f.setText("Here you live in a house near the barracks, where you voluntarily joined ");

            Text g = Game.getInstance().greysmanace.getName();


            Text h = SceneBuilder.makeText();
            h.setText(" to become a soldier.\n\n");

            Text i = SceneBuilder.makeText();
            i.setText("Your mission:\n");
            i.setFill(GameOptionen.highlightBlue);

            Text j = SceneBuilder.makeText();
            j.setText("In 2 days a convoy is heading towards ");
            j.setFill(GameOptionen.missionDescription);

            Text k = Game.getInstance().shallowmill.getOrtText();

            Text l = SceneBuilder.makeText();
            l.setText(".\n");
            l.setFill(GameOptionen.missionDescription);

            Text la = Game.getInstance().spieler.getName();

            //TODO make Dave NPC color
            Text lb = SceneBuilder.makeText(" and Dave will guard them on the journey and protect them against Human/Orc Thieves.\nThere are no dangers expected, but the party will come close to the ");
            lb.setFill(GameOptionen.missionDescription);

            Text m = Game.getInstance().unbridledland.getOrtText();

            Text n = SceneBuilder.makeText();//TODO check text
            n.setText(".\nIf there are Kitsune spottet, avoid the Fight. They will not attack and are no Threat to the group.\nWe expect to reach ");
            n.setFill(GameOptionen.missionDescription);

            Text o = Game.getInstance().shallowmill.getOrtText();

            Text p = SceneBuilder.makeText(" in 3 Days. After Arrival The Caravan will depart again 2 Days later, returning to ");
            p.setFill(GameOptionen.missionDescription);

            Text q = Game.getInstance().whitebridge.getOrtText();

            Text r = SceneBuilder.makeText(" .");
            spieltext.getChildren().addAll(a, b, c, d, e, f, g, h, i, j, k, l, la, lb, m, n, o, p, q, r);

            Button cont = SceneBuilder.makeButton(erste);
            cont.setText("Start your story");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.yourroom().getScene(0)));

            erste.getChildren().addAll(cont);
        }
        scene = new Scene(SceneBuilder.buildGameScene(erste, zweite, dritte, spieltext));
    }

    @Override
    public Scene getScene(int stage) {
        makeScene(stage);
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }
}
