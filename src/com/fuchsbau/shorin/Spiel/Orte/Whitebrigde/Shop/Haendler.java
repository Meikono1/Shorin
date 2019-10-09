package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Shop;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Haendler {
    private Scene scene;


    private void makeScene(){


        HBox erste = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton();

        back.setText("back to the shop");
        back.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Shop().getScene());
        });


        erste.getChildren().add(back);

       scene = new Scene(SceneBuilder.buildInventory(erste,null));


    }

    public Scene getScene() {
        makeScene();
        Game.getInstance().spieler.setAktuell(scene);
        return scene;
    }
}
