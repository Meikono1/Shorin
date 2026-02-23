package com.fuchsbau.shorin.RPG.Places.Whitebrigde;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Library implements Saveble {
    private Scene scene;

    private void makeScene() {
        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();

        Text descrip = SceneBuilder.getSceneBuilder().makeText("You enter the Whitebridge library. \nHere you can read books about the human history and the races of Shorin.");
        flow.getChildren().addAll(descrip);

        Button back = SceneBuilder.getSceneBuilder().makeButton(3, "Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);
        //TODO make Book Kitsune, Orc, Norse, Dryads, Gnome, Human history, Great war

        BorderPane pane = SceneBuilder.getSceneBuilder().buildGameScene(flow);
        scene = new Scene(pane);
    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }

}
