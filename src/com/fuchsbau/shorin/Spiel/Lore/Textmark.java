package com.fuchsbau.shorin.Spiel.Lore;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.SceneBuilder;
import javafx.scene.text.Text;

public class Textmark {

    private Text name = SceneBuilder.makeText();

    public Textmark(String name) {
        this.name.setText(name);
        this.name.setFill(GameOptionen.timestamp);

    }

    public Text getName() {
        return name;
    }
}
