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

public class Library {

    private BorderPane pane;

    Library() {

        HBox dritte = SceneBuilder.makeButtonrow();

        TextFlow flow = SceneBuilder.mainFlow();


        Text descrip = SceneBuilder.makeText();

        descrip.setText("You enter the Whitebridge library. \nHere you can read books about the human history and the races of Shorin.");

        flow.getChildren().addAll(descrip);

        Button back = SceneBuilder.makeButton();
        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.getPane())));
        //todo make Book Kitsune, Orc, Norse, Dryads, Gnome, Human history, Great war

        dritte.getChildren().addAll(back);

        pane = SceneBuilder.buildGameScene(null, null, dritte, flow);
    }


    public BorderPane getPane() {
        return pane;
    }

}
