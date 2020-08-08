package com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Barracks;

import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Training implements Saveble {

    private Scene scene;


    private void makeScene() {

        TextFlow flow = SceneBuilder.mainFlow();

        Text a = SceneBuilder.makeText();
        a.setText("This is the trainingsroom, you can do a testfight against some soldier");
        // TODO: 06.09.2019 make testfight

        flow.getChildren().addAll(a);


        Button zurueck = SceneBuilder.makeButton();
        zurueck.setText("Back to the barracks");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.getScene(0)));

        HBox dritte = SceneBuilder.makeButtonrow();

        dritte.getChildren().addAll(zurueck);


        scene = new Scene(SceneBuilder.buildGameScene(null, null, dritte, flow));
    }

    @Override
    public Scene getScene(int stage) {

        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }
}
