package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Waffe implements Item {

    private Text beschreibung = SceneBuilder.makeText();
    private Materialen material;
    private int qualitaet;
    private int schaden;
    private int zustand;

    public Waffe(int schaden, int zustand, int qualitaet, Materialen material, String text) {

        this.material = material;
        this.qualitaet = qualitaet;
        this.schaden = schaden;
        this.zustand = zustand;

        beschreibung = SceneBuilder.makeText();
        beschreibung.setText(material.getBeschreibung() + "  " + text);

    }


    @Override
    public Text getBeschreibung() {
        return beschreibung;
    }

    @Override
    public void setBeschreibung(Text beschreibung) {
        this.beschreibung = beschreibung;
    }

    @Override
    public String getuseText() {
        return "Equip";
    }

    @Override
    public void itemUse() {
        Game.getInstance().inventory.equip(this);
    }

    @Override
    public void dequip() {
        Game.getInstance().inventory.dequip(this);
    }

    @Override
    public String toString() {
        return "Waffe{" +
                "beschreibung=" + beschreibung.getText() +
                ", material=" + material +
                ", qualitaet=" + qualitaet +
                ", schaden=" + schaden +
                ", zustand=" + zustand +
                '}';
    }
}
