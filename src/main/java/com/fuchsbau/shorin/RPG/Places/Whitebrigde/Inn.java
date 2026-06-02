package com.fuchsbau.shorin.RPG.Places.Whitebrigde;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.RPG.Places.Place;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Inn extends Place implements Saveble {

    private Scene scene;
    private final Barkeeper barkeeper = new Barkeeper();

    public Inn(String name, String description) {
        super(name, description);
    }

    private void makeScene() {
    }

    @Override
    public Scene getScene(int stage) {
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }


    private static class Barkeeper implements Saveble {
        Scene scene;

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


}
