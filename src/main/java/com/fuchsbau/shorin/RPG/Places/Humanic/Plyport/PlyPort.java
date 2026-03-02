package com.fuchsbau.shorin.RPG.Places.Humanic.Plyport;

import com.fuchsbau.shorin.Engine.RPG.PlayerScreen;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.Scene;

public class PlyPort implements Saveble {
    private final FileLogger logger = FileLogger.getInstance();
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();

    public PlayerScreen playerScreen = new PlayerScreen();

    @Override
    public Scene getScene(int stage) {
        return playerScreen.getScene(0);

    }

    @Override
    public void reset() {

    }
}
