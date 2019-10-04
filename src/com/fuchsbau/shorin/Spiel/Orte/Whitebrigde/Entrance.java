package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Entrance {
    // TODO: 23.09.2019  make entrance
    private Scene scene;

    private void makeScene(){

        HBox dritte = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton();
        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene()));

        dritte.getChildren().addAll(back);

        scene = new Scene(SceneBuilder.buildGameScene(null,null,dritte,null));

    }


    public Scene getScene() {
        makeScene();
        Game.getInstance().spieler.setAktuell(scene);
        return scene;
    }
}
