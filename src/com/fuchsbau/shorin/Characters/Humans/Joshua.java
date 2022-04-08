package com.fuchsbau.shorin.Characters.Humans;

import com.fuchsbau.shorin.Characters.Character;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Joshua extends Character implements Saveble {
    public int gone = 0;
    private Scene scene;
    private int favor = 0;
    /*
    case favor
    0 = nicht geredet
    1 = zugesagt
    2 = abgesagt
     */

    private int stage;

    public Joshua() {
        super(100, 46, "Josha", Color.valueOf("068927"));
    }
    /*
    case Stage
    0 = erster talk
    1 = über Mission reden
    2 = Favor angenommen
    3 = Favor abgelehnt
    4 = über Familie reden
    5 = über barracken reden
     */


    private void makeScene() {

        TextFlow flow = SceneBuilder.mainFlow();

        if (stage == 0) {
            Text a = SceneBuilder.makeText();
            if (favor == 2) {
                a.setText("\"Hey, ");
                Text b = Game.getInstance().spieler.getText();
                Text c = SceneBuilder.makeText();
                c.setText(" what's up?\"");
                flow.getChildren().addAll(a, b, c);
            } else {
                a.setText("\"Hey, my Boy. How you doing ? \nI hope this mission is not to hard for you. I mean its the first time that you make such a long journey.\"");
                flow.getChildren().addAll(a);
            }

        }

        HBox erste = SceneBuilder.makeButtonrow();

        if (stage == 1) {
            missionTalkBarracks(erste, flow);
        }

        if (stage == 2) {
            Text a = SceneBuilder.makeText();
            a.setText("He gives you a Hug\n\n");
            a.setText("\"Thank you. I knew i can rely on you, if you find something there, you know where to find me. I would really appriciate a gift\"");
            flow.getChildren().addAll(a);
        }

        if (stage == 3) {
            Text a = SceneBuilder.makeText();
            a.setText("\"Ok, i understand that you have no time. Maybe i find someone else.\"");
            flow.getChildren().addAll(a);
        }

        if (stage == 4) {

            //TODO Text neu schreiben
            Text a = SceneBuilder.makeText();
            a.setText("\"Yes, they died when you were a small kid.\"\n\nYou see that sadness overcomes him\n\"They were good people. I meet them, when your mother was heavyly pragnent. " +
                    "They lived in the East, your father was always training the sword, preparing for the next Attack. It was a scary time\"\n\n He sights \n\"The Weres... rebelled and a bloody war raged right besides us in the woods." +
                    " I mean, thanks to the rebellion, the Orcs stayed away. But....\n\n\"Let's talk again some other time. I need some rest\"");

            gone = 10;

            flow.getChildren().add(a);
        } else if (stage != 1) {
            Button family = SceneBuilder.makeButton(erste);
            family.setText("Talk about Family");
            family.setOnMouseClicked(event -> Main.getStage().setScene(getScene(4)));
            erste.getChildren().add(family);
        }

        if (stage == 5) {

            Text a = SceneBuilder.makeText();
            a.setText("These old Barrack? \n there is nothing special about them");

            flow.getChildren().add(a);

        } else if (stage != 1 && stage != 4) {
            Button barracks = SceneBuilder.makeButton(erste);
            barracks.setText("Talk about barracks");
            barracks.setOnMouseClicked(event -> Main.getStage().setScene(getScene(5)));
            erste.getChildren().add(barracks);
        }


        HBox dritte = SceneBuilder.makeButtonrow();

        if (stage != 1) {
            if (favor == 0 && stage != 4) {
                Button mission = SceneBuilder.makeButton(erste);
                mission.setText("Thoughts about mission");
                mission.setOnMouseClicked(event -> Main.getStage().setScene(getScene(1)));
                erste.getChildren().add(mission);
            }


            Button zurueck = SceneBuilder.makeButton(dritte);
            zurueck.setText("Back to barracks");
            zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.getScene(0)));


            dritte.getChildren().addAll(zurueck);

        }

        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, flow));
    }

    private void missionTalkBarracks(HBox erste, TextFlow flow) {

        Text a = SceneBuilder.makeText();
        a.setText("Yes~~, i though this would be a good mission for you.\nGetting out of this missery we call ");

        Text b = Game.getInstance().whitebridge.getOrtText();

        Text c = SceneBuilder.makeText();
        c.setText(".The 'white' city, so clean and cultivated you might get sick by looking at it. And the 'bridge' to our greates city and Capital ");

        Text d = Game.getInstance().sudbury.getOrtText();

        Text e = SceneBuilder.makeText();
        e.setText(". \nMaybe one day i can visit the east again. Even if only war brought me there, i want to go back.\n\nCan i ask you for a favor?\n" +
                "In the east there is a river that formes out of 3 rivers. There is a special city, pls if you visit this place, tell me how it's going ? ");

        Button yes = SceneBuilder.makeButton(erste);
        yes.setText("Sure");
        yes.setOnMouseClicked(event -> {
            favor = 1;
            Main.getStage().setScene(getScene(2));
        });

        Button no = SceneBuilder.makeButton(erste);
        no.setText("No, sorry");
        no.setOnMouseClicked(event -> {
            favor = 2;
            Main.getStage().setScene(getScene(3));
        });

        flow.getChildren().addAll(a, b, c, d, e);
        erste.getChildren().addAll(yes, no);
    }

    public Scene getScene(int stage) {
        this.stage = stage;
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }

    public void update() {
        if (gone > 0) {
            gone--;
        }
    }
}
