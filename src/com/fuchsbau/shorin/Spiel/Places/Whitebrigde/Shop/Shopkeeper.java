package com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Shop;

import com.fuchsbau.shorin.Items.HealingPotion;
import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Material;
import com.fuchsbau.shorin.Items.Weapons.Broadsword;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class Shopkeeper implements Saveble {
    private Scene scene;
    private final List<Item> liste = new ArrayList<>();


    private void makeScene() {

        HBox erste = SceneBuilder.makeButtonrow();

        //TODO Items hinzufügen
        Broadsword broadsword = new Broadsword(1, 1, 1, Material.eisen, "Iron Broadsword");
        Button broadbuy = SceneBuilder.makeButton(erste);

        broadbuy.setText("Buy Broadsword");
        broadbuy.setOnMouseClicked(mouseEvent -> {
            Game.getInstance().inventory.addItem(new Broadsword(1, 1, 1, Material.eisen, "Iron Broadsword"));
        });
        liste.add(broadsword);

        HealingPotion heal = new HealingPotion("Heiltrank");
        Button healbuy = SceneBuilder.makeButton(erste);
        healbuy.setText("Buy Healpotion");
        healbuy.setOnMouseClicked(mouseEvent -> {
            Game.getInstance().inventory.addItem(new HealingPotion("Heiltrank"));
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

    @Override
    public void reset() {
        this.scene = null;
    }
}
