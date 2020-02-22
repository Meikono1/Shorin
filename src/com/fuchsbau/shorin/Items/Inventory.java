package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private Scene scene;
    private List<Item> liste;


    public Inventory() {
        liste = new ArrayList<>();

    }

    private void makeScene() {


        HBox erste = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton();
        back.setText("Back");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().spieler.getAktuell()));

        ScrollPane pane = SceneBuilder.makeScrollpane();

        erste.getChildren().add(back);

        scene = new Scene(SceneBuilder.makePlayerInventory(erste, pane, liste));

    }

    public  void addItem(Item item) {
        liste.add(item);

    }

    public Scene getScene() {
        makeScene();
        return scene;
    }
}
