package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private Scene scene;
    private List<Item> items;
    private List<Item> equip;


    public Inventory() {
        items = new ArrayList<>();
        equip = new ArrayList<>();
        //TODO hinzufügen und entfernen von Items in Equipment

    }

    private void makeScene() {


        HBox erste = SceneBuilder.makeButtonrow();
        Button back = SceneBuilder.makeButton();
        back.setText("Back");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().spieler.getAktuell()));

        //TODO Informationen button, Bücher, Rassen, etc

        erste.getChildren().add(back);

        scene = new Scene(SceneBuilder.makePlayerInventory(erste, items, equip));

    }

    public void addItem(Item item) {
        items.add(item);
    }

    public Scene getScene() {
        makeScene();
        return scene;
    }
}
