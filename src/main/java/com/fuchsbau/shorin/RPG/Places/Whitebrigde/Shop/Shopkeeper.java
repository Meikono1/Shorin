package com.fuchsbau.shorin.RPG.Places.Whitebrigde.Shop;


import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Scene;

public class Shopkeeper implements Saveble {
    private Scene scene;

    private void makeScene() {
    }

    @Override
    public Scene getScene(int stage) {
        SceneBuilder.getSceneBuilder().resetButtonrows();
        makeScene();
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
