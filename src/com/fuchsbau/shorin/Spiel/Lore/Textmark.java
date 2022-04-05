package com.fuchsbau.shorin.Spiel.Lore;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Textmark {

    private String name;

    public Textmark(String name) {
        this.name = name;

    }

    public Text getName() {
        Text back = SceneBuilder.makeText(name);
        back.setFill(GameOption.timestamp);
        return back;
    }
}
