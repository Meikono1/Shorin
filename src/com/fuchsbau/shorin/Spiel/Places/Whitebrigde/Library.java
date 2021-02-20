package com.fuchsbau.shorin.Spiel.Places.Whitebrigde;

import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Library implements Saveble {

    private Scene scene;

    private void makeScene() {

        HBox dritte = SceneBuilder.makeButtonrow();

        TextFlow flow = SceneBuilder.mainFlow();


        Text descrip = SceneBuilder.makeText();

        descrip.setText("You enter the Whitebridge library. \nHere you can read books about the human history and the races of Shorin.");

        flow.getChildren().addAll(descrip);

        Button back = SceneBuilder.makeButton(dritte);
        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
        //TODO make Book Kitsune, Orc, Norse, Dryads, Gnome, Human history, Great war

        dritte.getChildren().addAll(back);

        BorderPane pane = SceneBuilder.buildGameScene(null, null, dritte, flow);
        scene = new Scene(pane);
    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

}
