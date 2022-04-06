package com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Barracks;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class YourRoom implements Saveble {

    private Scene scene;


    /***
     *
     * @param stage
     * 0 = Startscreen
     * 1 = Sit on Bed
     */
    private void makeScene(int stage) {


        TextFlow flow = SceneBuilder.mainFlow();

        if (stage == 0) {
            Text a = SceneBuilder.makeText("You're in one of many sleeping quarters inside the Barracks of ");
            Text b = Game.getInstance().whitebridge.getOrtText();
            Text c = SceneBuilder.makeText(".\n\nThere are four bunk beds in this room with a stone table in front of every bed. They a very heavy tables and you can only put a bread or yufka on it. \nThere are also some racks for clothes,armor and weapons right at the entrance and a tried rag just before the entrance. \nThe upper bunk bed in the left corner is yours.");

            flow.getChildren().addAll(a, b, c);
        } else if (stage == 1) {

            Text a = SceneBuilder.makeText();//@Todo check text
            a.setText("You lived in this room since you joined the army.\n\nWhen you joined the ");
            Text b = Game.getInstance().greysmanace.getName();
            Text c = SceneBuilder.makeText(", you were assigned to this bed. Your commander is ");
            Text ca = Game.getInstance().joshua.getText();
            Text d = SceneBuilder.makeText(", your stepfather.\nYou dont know what happened to your old home but its probably in possession of another family working and destroying memories.\nMaybe you should just forget everything about your past, right now you have a mission to fulfill.");

            flow.getChildren().addAll(a, b, c, ca, d);
        }

        HBox erste = SceneBuilder.makeButtonrow();
        if (stage == 0) {
            Button bed = SceneBuilder.makeButton(erste);
            bed.setText("Sit on your old bed");
            bed.setOnMouseClicked(event -> Main.getStage().setScene(getScene(1)));

            erste.getChildren().addAll(bed);
        }

        HBox dritte = SceneBuilder.makeButtonrow();

        Button zurueck = SceneBuilder.makeButton(dritte);
        zurueck.setText("Back to the Barracks");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.getScene(0)));

        dritte.getChildren().addAll(zurueck);

        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, flow));
    }

    @Override
    public Scene getScene(int stage) {
        makeScene(stage);
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }
}
