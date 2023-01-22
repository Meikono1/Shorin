package com.fuchsbau.shorin.RPG.Lore;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.text.Text;

public class Textmark {

    private final String name;

    public Textmark(String name) {
        this.name = name;

    }

    public Text getName() {
        Text back = SceneBuilder.getSceneBuilder().makeText(name);
        back.setFill(GameOption.timestamp);
        return back;
    }
}
