package com.fuchsbau.shorin.Characters.Humans;

import com.fuchsbau.shorin.Characters.Char;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Dave extends Char {


    private Scene scene;
    private int ort;
    /*ort
     1 = Whitebridge
     */
    private int stage;
    /*stage
      0 = erstes mal treffen
     */


    public Dave() {
        super(100);
        this.stage = 0;

    }


    private Scene kitsuneTalk() {

        TextFlow field = SceneBuilder.mainFlow();

        Text text = SceneBuilder.makeText();

        String build = "\"Yes i heard about this Beasts. They are rumors that they read your mind and controll it.\n " +
                "Maybe i wake up one morning and i'm gay.\"" +
                "\n\n\nHe says Laughing";
        text.setText(build);


        HBox dritte = SceneBuilder.makeButtonrow();

        Button zurueck = SceneBuilder.makeButton();
        zurueck.setText("Back");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(getScene(ort)));

        dritte.getChildren().add(zurueck);
        field.getChildren().add(text);

        scene = new Scene(SceneBuilder.buildGameScene(null, null, dritte, field));


        return scene;
    }


    private void buildScene(int ort) {

        this.ort = ort;


        HBox erste = SceneBuilder.makeButtonrow();
        HBox dritte = SceneBuilder.makeButtonrow();

        TextFlow haupt = SceneBuilder.mainFlow();

        if (stage == 0) {

            Text a = SceneBuilder.makeText();
            a.setText("Hi, ");

            Text b = Game.getInstance().spieler.getName();

            Text c = SceneBuilder.makeText();
            c.setText("\"Seems we are both on the same mission, to escort some traders.\n" +
                    "I hope you bought some potions, or this will be a hard tour.\"");


            haupt.getChildren().addAll(a, b, c);
        }


        if (Game.getInstance().spieler.kitsune == 1) {
            Button kitsune = SceneBuilder.makeButton();
            kitsune.setText("Talk about Kitsune");
            kitsune.setOnMouseClicked(event -> Main.getStage().setScene(kitsuneTalk()));
        }


        Button zurueck = SceneBuilder.makeButton();

        if (ort == 1) {
            zurueck.setText("Back to Whitebridge");
            zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene()));
            dritte.getChildren().add(zurueck);
        }


        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, haupt));

    }

    public Scene getScene(int ort) {

        buildScene(ort);
        Game.getInstance().spieler.setAktuell(scene);
        return scene;
    }

}
