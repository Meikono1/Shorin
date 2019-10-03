package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde;

import com.fuchsbau.shorin.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Inn {

    private BorderPane pane;
    private Barkeeper barkeeper = new Barkeeper();



    Inn() {


        HBox dritte = SceneBuilder.makeButtonrow();

        TextFlow flow = SceneBuilder.mainFlow();


        Text intro = SceneBuilder.makeText();
        intro.setText("You're in the Whitebrige Tavern. \nYou can talk to the barkeeper or buy a drink");


        Button back = SceneBuilder.makeButton();

        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.getPane())));

        flow.getChildren().addAll(intro);

        //TODO Make Barkeeper
        Button bar = SceneBuilder.makeButton();
        bar.setText("Talk to barkeeper");
        bar.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Scene(barkeeper.getPane()));
        });

        dritte.getChildren().add(back);

        //Todo let buy drink
        pane = SceneBuilder.buildGameScene(null, null, dritte, flow);

    }


    public BorderPane getPane() {
        return pane;
    }


    private class Barkeeper {

        BorderPane pane;

        private Barkeeper() {

        }


        private BorderPane getPane(){
            return pane;
        }

    }


}
