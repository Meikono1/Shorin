package com.fuchsbau.shorin.Spiel.Places.Whitebrigde;

import com.fuchsbau.shorin.Items.MaxHealth;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Inn implements Saveble {

    private Scene scene;
    private final Barkeeper barkeeper = new Barkeeper();

    private void makeScene() {


        HBox dritte = SceneBuilder.makeButtonrow();

        TextFlow flow = SceneBuilder.mainFlow();


        Text intro = SceneBuilder.makeText();
        intro.setText("You're in the Whitebrige Tavern. \nYou can talk to the barkeeper or buy a drink");


        Button back = SceneBuilder.makeButton(dritte);

        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));

        flow.getChildren().addAll(intro);

        //TODO Make Barkeeper
        Button bar = SceneBuilder.makeButton(dritte);
        bar.setText("Talk to barkeeper");
        bar.setOnMouseClicked(event -> Main.getStage().setScene(barkeeper.getScene(0)));

        dritte.getChildren().add(back);

        HBox erste = SceneBuilder.makeButtonrow();

        Button buy = SceneBuilder.makeButton(erste);
        buy.setText("Buy Beer");
        buy.setOnMouseClicked(mouseEvent -> {
            MaxHealth bear = new MaxHealth("Beer");
            Game.getInstance().inventory.addItem(bear);

        });
        erste.getChildren().add(buy);

        //TODO let buy drink
        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, flow));

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
