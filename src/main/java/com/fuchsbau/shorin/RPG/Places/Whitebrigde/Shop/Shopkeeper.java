package com.fuchsbau.shorin.RPG.Places.Whitebrigde.Shop;

import com.fuchsbau.shorin.Items.HealingPotion;
import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Material;
import com.fuchsbau.shorin.Items.Weapons.Broadsword;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class Shopkeeper implements Saveble {
    private Scene scene;
    private final List<Item> items = new ArrayList<>();

    private void makeScene() {
        //TODO Items hinzufügen
        Broadsword broadsword = new Broadsword(1, 100, 1, Material.iron, "Iron Broadsword");
        Button broadbuy = SceneBuilder.getSceneBuilder().makeButton(1, "Buy Broadsword");
        broadbuy.setOnMouseClicked(mouseEvent -> {
            Game.getInstance().inventory.addItem(new Broadsword(1, 100, 1, Material.iron, "Iron Broadsword"));
        });
        items.add(broadsword);
        SceneBuilder.getSceneBuilder().addButton(broadbuy, 1);

        HealingPotion heal = new HealingPotion("Heiltrank");
        Button healbuy = SceneBuilder.getSceneBuilder().makeButton(1, "Buy Healpotion");
        healbuy.setOnMouseClicked(mouseEvent -> {
            Game.getInstance().inventory.addItem(new HealingPotion("Heiltrank"));
        });
        items.add(heal);
        SceneBuilder.getSceneBuilder().addButton(healbuy, 1);

        Button back = SceneBuilder.getSceneBuilder().makeButton(2, "back to the shop");
        back.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Shop().getScene(0));
        });
        SceneBuilder.getSceneBuilder().addButton(back, 2);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildShop(SceneBuilder.getSceneBuilder().makeScrollpane(), items));
    }

    @Override
    public Scene getScene(int stage) {
        SceneBuilder.getSceneBuilder().resetButtonrows();
        makeScene();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
