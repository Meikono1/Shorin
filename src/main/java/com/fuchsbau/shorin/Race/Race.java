package com.fuchsbau.shorin.Race;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.text.Text;

public class Race {
    private final String name;
    private final String description;
    private final Text ingamedescription = SceneBuilder.getSceneBuilder().makeText();

    public Race(String name, String description) {
        this.name = name;
        this.description = description;
        ingamedescription.setText(name);
        ingamedescription.setFill(GameOption.cityColor);
    }


    public Text getOrtText() {
        Text ret = SceneBuilder.getSceneBuilder().makeText();
        ret.setText(name);
        ret.setFill(GameOption.raceColor);
        return ret;
    }
}
