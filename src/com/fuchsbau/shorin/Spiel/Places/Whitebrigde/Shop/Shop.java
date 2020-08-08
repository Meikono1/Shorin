package com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Shop;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Shop implements Saveble {

    // TODO: 02.09.2019  Make shop
    private Scene scene;

    private void makeScene() {

        HBox erste = SceneBuilder.makeButtonrow();

        Button shopping = SceneBuilder.makeButton();
        shopping.setText("Buy Items");
        shopping.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Haendler().getScene(0));
        });

        erste.getChildren().add(shopping);

        HBox dritte = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton();
        back.setText("Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));

        dritte.getChildren().addAll(back);

        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, null));

    }

    @Override
    public Scene getScene(int stage) {

        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }
}
