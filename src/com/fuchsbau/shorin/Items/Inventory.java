package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.List;

public class Inventory {

    private Scene scene;
    private List <Item> liste;


    public Inventory (){

    }

    private void makeScene() {


        HBox erste = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton();
        back.setText("Back");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().spieler.getAktuell()));


        erste.getChildren().add(back);

        scene = new Scene(SceneBuilder.buildInventory(erste));

    }

    public Scene getScene() {
        makeScene();
        return scene;
    }
}
