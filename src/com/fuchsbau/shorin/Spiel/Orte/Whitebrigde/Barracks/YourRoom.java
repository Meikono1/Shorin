package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Barracks;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


public class YourRoom {

    private Scene scene;


    /***
     *
     * @param stage
     * 0 = Vom Startbildschirm
     * 1 = Visit
     * 2 = Sit on Bed
     */
    private void makeScene(int stage) {


        TextFlow flow = SceneBuilder.mainFlow();

        if (stage == 0) {
            Text a = SceneBuilder.makeText();
            a.setText("You're in your Barracksroom. \n\nIt´s a four bed room with a stone table for every person. Here you met Dave, he doesn't like xenos or in other words, non humans, but i can rely on him and his skills." +
                    "\nThe bed in the left backcorner is yours. 5 Years you lived on this bed");

            flow.getChildren().addAll(a);
        } else if (stage == 1) {

            Text a = SceneBuilder.makeText();
            a.setText("In this room you lived since your familys death. \n\nJoshua, an old family friend, brought you here since you couldnt take care of your home all alone.\n" +
                    "You dont know what happend to your old home but its probably in posession of another family working and destroying memories.\nMaybe you should just forget everything about your past, right now you have a mission to fulfill.");


            flow.getChildren().addAll(a);
        }


        HBox erste = SceneBuilder.makeButtonrow();
        if (stage == 0) {
            Button bed = SceneBuilder.makeButton();
            bed.setText("Sit on your old bed");
            bed.setOnMouseClicked(event -> Main.getStage().setScene(getScene(1)));

            erste.getChildren().addAll(bed);
        }


        HBox dritte = SceneBuilder.makeButtonrow();

        Button zurueck = SceneBuilder.makeButton();
        zurueck.setText("Back to the Barracks");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.getScene()));

        dritte.getChildren().addAll(zurueck);


        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, flow));

    }


    public Scene getScene(int stage) {
        makeScene(stage);
        Game.getInstance().spieler.setAktuell(scene);
        return scene;
    }
}
