package com.fuchsbau.shorin.RPG.Places.Whitebrigde.Barracks;

import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class Barracks implements Saveble {
    private Scene scene;
    private final YourRoom yourRoom = new YourRoom();
    private final Training trainroom = new Training();

    public Barracks() {

    }

    private void makeScene() {
        TextFlow text = SceneBuilder.getSceneBuilder().mainFlow();

        Text a = SceneBuilder.getSceneBuilder().makeText("These are the barracks of ");
        Text b = Game.getInstance().whitebridge.getOrtText();
        Text c = SceneBuilder.getSceneBuilder().makeText(". ");
        Text d = Game.getInstance().joshua.getText();
        Text e = SceneBuilder.getSceneBuilder().makeText(" is propably somewhere around. \nThere is also your old room you can visit or go to the trainingsroom in the back. ");
        text.getChildren().addAll(a, b, c, d, e);


        if (Game.getInstance().joshua.gone == 0) {
            Button joshua = SceneBuilder.getSceneBuilder().makeButton(1, "Look for Joshua");
            joshua.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().joshua.getScene(0)));
            SceneBuilder.getSceneBuilder().addButton(joshua, 1);
        }

        Button yroom = SceneBuilder.getSceneBuilder().makeButton(1, "Go to your old room");
        yroom.setOnMouseClicked(event -> Main.getStage().setScene(yourRoom.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(yroom, 1);

        Button training = SceneBuilder.getSceneBuilder().makeButton(1, "To the trainingsroom");
        training.setOnMouseClicked(event -> Main.getStage().setScene(trainroom.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(training, 1);

        Button zurueck = SceneBuilder.getSceneBuilder().makeButton(3, "Back to Whitebridge");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(zurueck, 3);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(text));
    }

    public YourRoom yourRoom() {
        return yourRoom;
    }

    public Training getTrainroom() {
        return trainroom;
    }

    public Scene getScene(int stage) {
        SceneBuilder.getSceneBuilder().resetButtonrows();
        makeScene();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
