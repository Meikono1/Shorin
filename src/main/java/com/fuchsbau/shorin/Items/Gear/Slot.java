package com.fuchsbau.shorin.Items.Gear;

import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.text.Text;

public enum Slot {

    head(1, "Head") {
    },
    chest(2, "Chest") {
    },
    arms(3, "Arm") {
    },
    pants(4, "Pants") {
    },
    boots(5, "Boots") {
    };

    Slot(int i, String text) {
        int id = i;
        Text beschreibung = SceneBuilder.getSceneBuilder().makeText(text);
    }

}
