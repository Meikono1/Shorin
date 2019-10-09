package com.fuchsbau.shorin.Items.Waffen;

import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

enum Art {

    einhand(1, "one handed") {

    },
    zweihand(2, "two handed") {
    };

    Art(int i, String text) {
        int id = i;
        Text beschreibung = SceneBuilder.makeText(text);


    }


}
