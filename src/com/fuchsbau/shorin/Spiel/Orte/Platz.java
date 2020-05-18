package com.fuchsbau.shorin.Spiel.Orte;

import com.fuchsbau.shorin.Optionen.GameOptionen;
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
        ingamebeschreibung.setFill(GameOptionen.ortcolor);

    }


    public Text getOrtText() {

        Text ret = SceneBuilder.makeText();
        ret.setText(name);
        ret.setFill(GameOptionen.ortcolor);

        return ret;
    }


}
