package com.fuchsbau.shorin.RPG.Places.Humanic.Plyport;

import com.fuchsbau.shorin.Engine.Map.MapModel;
import com.fuchsbau.shorin.Engine.RPG.PlayerScreen;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import javafx.scene.Scene;

public class PlyPort implements Saveble {

    public PlayerScreen playerScreen = new PlayerScreen(new MapModel());

    @Override
    public Scene getScene(int stage) {
        return playerScreen.getScene(0);

    }

    @Override
    public void reset() {

    }
}
