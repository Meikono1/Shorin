package com.fuchsbau.shorin.Spiel.Places;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Platz {

    private String name;
    private String beschreibung;
    private Text ingamebeschreibung = SceneBuilder.makeText();

    public Platz(String name, String beschreibung) {

        this.name = name;
        this.beschreibung = beschreibung;
        ingamebeschreibung.setText(name);
        ingamebeschreibung.setFill(GameOption.ortcolor);

    }


    public Text getOrtText() {

        Text ret = SceneBuilder.makeText();
        ret.setText(name);
        ret.setFill(GameOption.ortcolor);

        return ret;
    }


}
