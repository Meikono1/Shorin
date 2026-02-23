package com.fuchsbau.shorin.Engine.RPG;

import javafx.scene.Scene;

public interface Saveble{

    Scene getScene(int stage);

    void reset();
}
