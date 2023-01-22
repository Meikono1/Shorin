package com.fuchsbau.shorin.Characters.Humans;

import com.fuchsbau.shorin.Characters.Character;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Dave extends Character implements Saveble {

    private Scene scene;
    private int place;
    /*ort
     1 = Whitebridge
     */
    private int stage;
    /*stage
      0 = erstes mal treffen
     */

    public Dave() {
        super(100, 18, "Dave", 183, 95, 95, 68, Color.valueOf("7516ff"));
        this.stage = 0;
    }

    private Scene kitsuneTalk() {

        TextFlow field = SceneBuilder.getSceneBuilder().mainFlow();

        Text text = SceneBuilder.getSceneBuilder().makeText(
                "\"Yes i heard about this Beasts. They are rumors that they read your mind and controll it.\n " +
                        "Maybe i wake up one morning and i'm gay.\"" +
                        "\n\n\nHe says Laughing");

        Button back = SceneBuilder.getSceneBuilder().makeButton(3);
        back.setText("Back");
        back.setOnMouseClicked(event -> Main.getStage().setScene(getScene(place)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);

        field.getChildren().add(text);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(field));
        return scene;
    }


    private void buildScene(int ort) {
        this.place = ort;
        TextFlow haupt = SceneBuilder.getSceneBuilder().mainFlow();

        if (stage == 0) {
            Text a = SceneBuilder.getSceneBuilder().makeText();
            a.setText("Hi, ");

            Text b = Game.getInstance().spieler.getText();

            Text c = SceneBuilder.getSceneBuilder().makeText();
            c.setText("\"Seems we are both on the same mission, to escort some traders.\n" +
                    "I hope you bought some potions, or this will be a hard tour.\"");

            haupt.getChildren().addAll(a, b, c);
        }

        if (Game.getInstance().spieler.kitsune == 1) {
            Button kitsune = SceneBuilder.getSceneBuilder().makeButton(1, "Talk about Kitsune");
            kitsune.setOnMouseClicked(event -> Main.getStage().setScene(kitsuneTalk()));
            // TODO: 19.02.2021 prüfen
            SceneBuilder.getSceneBuilder().addButton(kitsune, 1);
        }

        if (ort == 1) {
            Button back = SceneBuilder.getSceneBuilder().makeButton(3, "Back to Whitebridge");
            back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
            SceneBuilder.getSceneBuilder().addButton(back, 3);
        }

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(haupt));
    }

    @Override
    public Scene getScene(int ort) {
        SceneBuilder.getSceneBuilder().resetButtonrows();
        buildScene(ort);
        Game.getInstance().spieler.setAktuell(this, ort);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }

}
