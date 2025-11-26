package com.fuchsbau.shorin.RPG.Places.Whitebrigde;

import com.fuchsbau.shorin.Items.MaxHealth;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Inn implements Saveble {

    private Scene scene;
    private final Barkeeper barkeeper = new Barkeeper();

    private void makeScene() {
        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();

        Text intro = SceneBuilder.getSceneBuilder().makeText();
        intro.setText("You're in the Whitebrige Tavern. \nYou can talk to the barkeeper or buy a drink");
        flow.getChildren().addAll(intro);

        Button back = SceneBuilder.getSceneBuilder().makeButton(3, "Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);


        //TODO Make Barkeeper
        Button bar = SceneBuilder.getSceneBuilder().makeButton(1, "Talk to barkeeper");
        bar.setOnMouseClicked(event -> Main.getStage().setScene(barkeeper.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(bar, 1);

        Button buy = SceneBuilder.getSceneBuilder().makeButton(1, "Buy Beer");
        buy.setOnMouseClicked(mouseEvent -> {
            MaxHealth bear = new MaxHealth("Beer");
            Game.getInstance().inventory.addItem(bear);
        });
        SceneBuilder.getSceneBuilder().addButton(buy, 1);

        //TODO let buy drink
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


    private static class Barkeeper implements Saveble {
        Scene scene;

        private void makeScene() {

        }

        @Override
        public Scene getScene(int stage) {
            SceneBuilder.getSceneBuilder().resetButtonrows();
            makeScene();
            Game.getInstance().spieler.setAktuell(this, stage);
            return scene;
        }

        @Override
        public void reset() {
            this.scene = null;
        }

    }


}
