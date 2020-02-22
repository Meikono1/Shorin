package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Waffe implements Item {

    private Text beschreibung = SceneBuilder.makeText();
    private Materialen material;
    private int qualitaet;
    private int schaden;
    private int zustand;

    public Waffe(int schaden, int zustand, int qualitaet, Materialen material) {

        this.material = material;
        this.qualitaet = qualitaet;
        this.schaden = schaden;
        this.zustand = zustand;

        beschreibung = SceneBuilder.makeText();
        beschreibung.setText(material + " broadsword");

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
