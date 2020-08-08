package com.fuchsbau.shorin.Spiel.Places.Whitebrigde;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Entrance implements Saveble {
    // TODO: 23.09.2019  make entrance
    private Scene scene;

    private void makeScene() {

        HBox dritte = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton();
        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));

        dritte.getChildren().addAll(back);

        scene = new Scene(SceneBuilder.buildGameScene(null, null, dritte, null));

    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }
}
