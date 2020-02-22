package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Potion implements Item {
    private Text beschreibung = SceneBuilder.makeText();

    public Potion(String text) {
        beschreibung.setText(text);
    }


    @Override
    public Text getBeschreibung() {
        return beschreibung;
    }

    @Override
    public void setBeschreibung(Text beschreibung) {
        this.beschreibung = beschreibung;
    }
}
