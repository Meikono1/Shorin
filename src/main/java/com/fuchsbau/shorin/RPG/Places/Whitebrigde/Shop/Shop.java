package com.fuchsbau.shorin.RPG.Places.Whitebrigde.Shop;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.RPG.Places.Place;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class Shop extends Place implements Saveble {

    // TODO: 02.09.2019  Make shop
    private Scene scene;

    public Shop(String name, String description) {
        super(name, description);
    }

    private void makeScene() {
        Button shopping = SceneBuilder.getSceneBuilder().makeButton(1, "Buy Items");
        shopping.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Shopkeeper().getScene(0));
        });
        SceneBuilder.getSceneBuilder().addButton(shopping, 1);

        Button back = SceneBuilder.getSceneBuilder().makeButton(3,"Back to Whitebridge");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene( null));
    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
