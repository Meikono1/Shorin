package com.fuchsbau.shorin.RPG.Places;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.text.Text;

public class Place {

    private final String name;
    private String description;
    private final Text ingamedescription = SceneBuilder.getSceneBuilder().makeText();

    public Place(String name, String description) {
        this.name = name;
        this.description = description;
        ingamedescription.setText(name);
        ingamedescription.setFill(GameOptions.cityColor);
    }


    public Text getOrtText() {
        Text ret = SceneBuilder.getSceneBuilder().makeText();
        ret.setText(name);
        ret.setFill(GameOptions.cityColor);
        return ret;
    }
}
