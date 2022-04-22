package com.fuchsbau.shorin.Spiel.Intro;

import com.fuchsbau.shorin.Optionen.GameOption;
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

public class Creation implements Saveble {

    private Scene scene;


    public void makeScene(int stage) {
        VBox controls = new VBox();
        HBox first = SceneBuilder.makeButtonrow();
        HBox second = SceneBuilder.makeButtonrow();
        HBox third = SceneBuilder.makeButtonrow();

        controls.getChildren().addAll(first, second, third);
        TextFlow gametext = SceneBuilder.mainFlow();


        if (stage == 1) {
            {
                StringBuilder name;
                name = new StringBuilder();
                name.append("Welcome to the World of Shorin\n\n");
                name.append("You are a male Human Soldier. ");

                name.append("\nYour name is ... ");

                //@TODO Check Text
                Text a = SceneBuilder.makeText();
                a.setText(name.toString());

                gametext.getChildren().addAll(a);
            }

            Label name = new Label();
            name.prefWidthProperty().bind(Bindings.divide(first.widthProperty(), 7));
            name.setTextFill(Paint.valueOf("ffffff"));
            name.setText("Name :");

            TextField eingabe = new TextField();
            eingabe.prefWidthProperty().bind(Bindings.divide(first.widthProperty(), 7));

            Button accept = SceneBuilder.makeButton(first);
            accept.setText("Accept");
            accept.setOnMouseClicked(event -> {
                if (eingabe.getText().equals("")) {
                    Game.getInstance().spieler.setName("Jan");
                } else {
                    Game.getInstance().spieler.setName(eingabe.getText());
                }
                Main.getStage().setScene(getScene(2));
            });
            first.getChildren().addAll(name, eingabe, accept);
        }


        if (stage == 2) {//intro to you
            Text a = SceneBuilder.makeText("Welcome to the World of Shorin\n\nYou're a ");
            Text aa = SceneBuilder.makeText(String.valueOf(Game.getInstance().spieler.getAge()));
            Text ab = SceneBuilder.makeText(" years old freshly trained soldier, who was just assigned for his first official mission.\nYour name is ");

            Text b = Game.getInstance().spieler.getText();

            Text c = SceneBuilder.makeText(".");
            //@TODO check Text
            Text d = SceneBuilder.makeText("\n\n10 Years ago your parents died in the Great war when you were only ");
            Text daa = SceneBuilder.makeText(String.valueOf(Game.getInstance().spieler.getAge() - 10));
            Text dab = SceneBuilder.makeText(" Years old. You didn't knew them well but people say they were brave soldiers fighting for humanity.\n");
            Text da = Game.getInstance().joshua.getText();
            Text db = SceneBuilder.makeText(", a family friend, took you in and treated you like his own son.\nHe loved to tell stories about your Parents and the war, but a few months, after he adopted you, he grew tired and seeks peace in his life. \n\nThe story about the death of your parents, still haunts you and you aim to seek revenge and defend humanity against the evil forces across the rivers: Fen and Hedge.\n\n");

            Text e = SceneBuilder.makeText("Then, at the age of 12, you officially joined the army under the Command of ");
            Text ea = Game.getInstance().joshua.getText();
            Text eb = SceneBuilder.makeText(". He thought you the local courtesy, how to survive in the wild, make a fire and to fight against humanoids. You then spent most of the time clearing disputes and helping the locals. It was hard at first, but with time the local population accepted you as a peacekeeper. \nNow, with your first mission, you can finally leave this city and change your live.");


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
            gametext.getChildren().addAll(a, aa, ab, b, c, d, daa, dab,da, db, e, ea, eb);

            Button cont = SceneBuilder.makeButton(first);
            cont.setText("Continue");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(getScene(3)));

            first.getChildren().addAll(cont);
        }

        if (stage == 3) {//home

            Text a = SceneBuilder.makeText();
            a.setText("You currently live in the city of ");

            Text b = Game.getInstance().whitebridge.getOrtText();

            Text c = SceneBuilder.makeText();//TODO check text
            c.setText(", the first city outside the walls of ");

            Text d = Game.getInstance().sudbury.getOrtText();

            Text e = SceneBuilder.makeText();
            e.setText(", the main capital.");

            Text f = SceneBuilder.makeText();
            f.setText("Here you live in a room inside the barracks, where you voluntarily joined ");

            Text g = Game.getInstance().greysmanace.getName();


            Text h = SceneBuilder.makeText();
            h.setText(" to become a soldier.\n\n");

            Text i = SceneBuilder.makeText();
            i.setText("Your mission:\n");
            i.setFill(GameOption.highlightBlue);

            Text j = SceneBuilder.makeText();
            j.setText("In 2 days a convoy is heading towards ");
            j.setFill(GameOption.missionDescription);

            Text k = Game.getInstance().shallowmill.getOrtText();

            Text l = SceneBuilder.makeText();
            l.setText(".\n");
            l.setFill(GameOption.missionDescription);

            Text la = Game.getInstance().spieler.getText();

            //TODO make Dave NPC color
            Text lb = SceneBuilder.makeText(" and ");
            lb.setFill(GameOption.missionDescription);
            Text lc = Game.getInstance().dave.getText();
            Text ld = SceneBuilder.makeText(" will guard them on the journey and protect them against Human/Orc Thieves.\nThere are no dangers expected, but the party will come close to the ");
            ld.setFill(GameOption.missionDescription);

            Text m = Game.getInstance().unbridledland.getOrtText();

            Text n = SceneBuilder.makeText();//TODO check text
            n.setText(".\nIf there are kitsune spotted, avoid the fight. They will likely not attack and are no threat to the group.\nWe expect to reach ");
            n.setFill(GameOption.missionDescription);

            Text o = Game.getInstance().shallowmill.getOrtText();

            Text p = SceneBuilder.makeText(" in 3 Days. After Arrival The Caravan will depart again 2 Days later, returning to ");
            p.setFill(GameOption.missionDescription);

            Text q = Game.getInstance().whitebridge.getOrtText();

            Text r = SceneBuilder.makeText(".");
            gametext.getChildren().addAll(a, b, c, d, e, f, g, h, i, j, k, l, la, lb, lc, ld, m, n, o, p, q, r);

            Button cont = SceneBuilder.makeButton(first);
            cont.setText("Start your story");
            cont.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.yourroom().getScene(0)));

            first.getChildren().addAll(cont);
        }
        scene = new Scene(SceneBuilder.buildGameScene(first, second, third, gametext));
    }

    @Override
    public Scene getScene(int stage) {
        makeScene(stage);
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
