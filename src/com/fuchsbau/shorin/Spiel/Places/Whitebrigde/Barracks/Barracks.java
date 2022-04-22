package com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Barracks;

import com.fuchsbau.shorin.Spiel.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class Barracks implements Saveble {


    private Scene scene;
    private final YourRoom yourRoom = new YourRoom();
    private final Training trainroom = new Training();

    public Barracks() {

    }


    private void makeScene() {


        TextFlow text = SceneBuilder.mainFlow();


        Text a = SceneBuilder.makeText("These are the barracks of ");
        Text b = Game.getInstance().whitebridge.getOrtText();
        Text c = SceneBuilder.makeText(". ");
        Text d = Game.getInstance().joshua.getText();
        Text e = SceneBuilder.makeText(" is propably somewhere around. \nThere is also your old room you can visit or go to the trainingsroom in the back. ");


        text.getChildren().addAll(a,b,c,d,e);

        HBox erste = SceneBuilder.makeButtonrow();

        if (Game.getInstance().joshua.gone == 0) {
            Button jush = SceneBuilder.makeButton(erste);
            jush.setText("Look for Joshua");
            jush.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().joshua.getScene(0)));
            erste.getChildren().add(jush);
        }

        Button yroom = SceneBuilder.makeButton(erste);
        yroom.setText("Go to your old room");
        yroom.setOnMouseClicked(event -> Main.getStage().setScene(yourRoom.getScene(0)));

        Button training = SceneBuilder.makeButton(erste);
        training.setText("To the trainingsroom");
        training.setOnMouseClicked(event -> Main.getStage().setScene(trainroom.getScene(0)));


        erste.getChildren().addAll(yroom, training);

        HBox dritte = SceneBuilder.makeButtonrow();

        Button zurueck = SceneBuilder.makeButton(dritte);
        zurueck.setText("Back to Whitebridge");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));


        dritte.getChildren().addAll(zurueck);

        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, text));

    }

    public YourRoom yourroom() {
        return yourRoom;
    }

    public Training getTrainroom() {
        return trainroom;
    }

    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
