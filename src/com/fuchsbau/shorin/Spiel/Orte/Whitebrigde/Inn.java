package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Inn {

    private Scene scene;
    private Barkeeper barkeeper = new Barkeeper();

    private void makeScene() {


        HBox dritte = SceneBuilder.makeButtonrow();

        TextFlow flow = SceneBuilder.mainFlow();


        Text intro = SceneBuilder.makeText();
        intro.setText("You're in the Whitebrige Tavern. \nYou can talk to the barkeeper or buy a drink");


        Button back = SceneBuilder.makeButton();

        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene()));

        flow.getChildren().addAll(intro);

        //TODO Make Barkeeper
        Button bar = SceneBuilder.makeButton();
        bar.setText("Talk to barkeeper");
        bar.setOnMouseClicked(event -> Main.getStage().setScene(barkeeper.getScene()));

        dritte.getChildren().add(back);

        //Todo let buy drink
        scene = new Scene(SceneBuilder.buildGameScene(null, null, dritte, flow));

    }


    public Scene getScene() {
        makeScene();
        Game.getInstance().spieler.setAktuell(scene);
        return scene;
    }


    private class Barkeeper {

        Scene scene;

        private void makeScene() {

        }


        private Scene getScene() {
            makeScene();
            Game.getInstance().spieler.setAktuell(scene);
            return scene;
        }

    }


}
