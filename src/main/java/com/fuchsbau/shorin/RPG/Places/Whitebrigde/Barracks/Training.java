package com.fuchsbau.shorin.RPG.Places.Whitebrigde.Barracks;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Training implements Saveble {

    private Scene scene;

    private void makeScene() {
        SceneBuilder.getSceneBuilder().resetButtonrows();

        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();

        Text a = SceneBuilder.getSceneBuilder().makeText();
        a.setText("This is the trainingsroom, you can do a testfight against some soldier");
        // TODO: 06.09.2019 make testfight

        flow.getChildren().addAll(a);

        Button back = SceneBuilder.getSceneBuilder().makeButton(3, "Back to the barracks");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(flow));
    }

    @Override
    public Scene getScene(int stage) {

        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
