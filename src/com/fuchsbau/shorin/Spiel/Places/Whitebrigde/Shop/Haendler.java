package com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Shop;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Materialen;
import com.fuchsbau.shorin.Items.Potion;
import com.fuchsbau.shorin.Items.Waffen.Breitschwert;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class Haendler implements Saveble {
    private Scene scene;
    private List<Item> liste = new ArrayList<Item>();


    private void makeScene() {

        HBox erste = SceneBuilder.makeButtonrow();

        //TODO Items hinzufügen
        Breitschwert breitschwert = new Breitschwert(1, 1, 1, Materialen.eisen, "Iron Broadsword");
        Button broadbuy = SceneBuilder.makeButton(erste);

        broadbuy.setText("Buy Broadsword");
        broadbuy.setOnMouseClicked(mouseEvent -> {
            Game.getInstance().inventory.addItem(new Breitschwert(1, 1, 1, Materialen.eisen, "Iron Broadsword"));
        });
        liste.add(breitschwert);

        Potion heal = new Potion("Heiltrank");
        Button healbuy = SceneBuilder.makeButton(erste);
        healbuy.setText("Buy Healpotion");
        healbuy.setOnMouseClicked(mouseEvent -> {
            Game.getInstance().inventory.addItem(new Potion("Heiltrank"));
        });
        liste.add(heal);

        erste.getChildren().addAll(broadbuy, healbuy);

        HBox zweite = SceneBuilder.makeButtonrow();

        Button back = SceneBuilder.makeButton(zweite);

        back.setText("back to the shop");
        back.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Shop().getScene(0));
        });


        zweite.getChildren().add(back);

        scene = new Scene(SceneBuilder.buildShop(erste, zweite, SceneBuilder.makeScrollpane(), liste));


    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }
}
