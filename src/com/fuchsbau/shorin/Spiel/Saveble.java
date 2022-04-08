package com.fuchsbau.shorin.Spiel;

import javafx.scene.Scene;

public interface Saveble{

    Scene getScene(int stage);

    void reset();
}
