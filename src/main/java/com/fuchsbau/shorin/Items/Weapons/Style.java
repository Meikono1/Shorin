package com.fuchsbau.shorin.Items.Weapons;

import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.text.Text;

enum Style {

    oneHanded(1, "one handed") {

    },
    twohanded(2, "two handed") {
    };

    Style(int i, String text) {
        int id = i;
        Text description = SceneBuilder.getSceneBuilder().makeText(text);
    }
}
