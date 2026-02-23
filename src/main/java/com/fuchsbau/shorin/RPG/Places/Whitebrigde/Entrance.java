package com.fuchsbau.shorin.RPG.Places.Whitebrigde;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class Entrance implements Saveble {
    // TODO: 23.09.2019  make entrance
    private Scene scene;

    private void makeScene() {
        Button back = SceneBuilder.getSceneBuilder().makeButton(3, "Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(null));
    }

    @Override
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
