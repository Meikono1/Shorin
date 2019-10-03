package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Barracks;

import com.fuchsbau.shorin.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class Barracks {


    private BorderPane pane;
    private YourRoom yourRoom = new YourRoom();
    private Training trainroom = new Training();

    public Barracks() {

    }


    private void makePane() {


        TextFlow text = SceneBuilder.mainFlow();


        Text intro = SceneBuilder.makeText();
        intro.setText("In this barracks you spend most of your live. Joshua is propably somewhere around. \nThere is also your old room you can visit or go to the trainingsroom in the back. ");


        text.getChildren().addAll(intro);

        HBox erste = SceneBuilder.makeButtonrow();

        if (Game.getInstance().joshua.gone == 0) {
            Button jush = SceneBuilder.makeButton();
            jush.setText("Look for Joshua");
            jush.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().joshua.getPane(0))));
            erste.getChildren().add(jush);
        }

        Button yroom = SceneBuilder.makeButton();
        yroom.setText("Go to your old room");
        yroom.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(yourRoom.getPane())));

        Button training = SceneBuilder.makeButton();
        training.setText("To the trainingsroom");
        training.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(trainroom.getPane())));


        erste.getChildren().addAll(yroom, training);

        HBox dritte = SceneBuilder.makeButtonrow();

        Button zurueck = SceneBuilder.makeButton();
        zurueck.setText("Back to Whitebridge");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.getPane())));


        dritte.getChildren().addAll(zurueck);

        pane = SceneBuilder.buildGameScene(erste, null, dritte, text);

    }


    public BorderPane getPane() {
        makePane();
        return pane;
    }
}
