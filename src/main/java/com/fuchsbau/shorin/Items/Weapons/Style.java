package com.fuchsbau.shorin.Items.Weapons;

import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

enum Style {

    einhand(1, "one handed") {

    },
    zweihand(2, "two handed") {
    };

    Style(int i, String text) {
        int id = i;
        Text beschreibung = SceneBuilder.makeText(text);


    }


}
