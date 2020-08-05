package com.fuchsbau.shorin.Items.Gear;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Armor implements Item {

    private String beschreibung;
    private Slot slot;

    public Armor(int armor, int qualitaet, String text, Slot slot) {
        this.slot = slot;
        beschreibung = text;
    }

    @Override
    public Text getBeschreibung() {
        return SceneBuilder.makeText(beschreibung);
    }

    @Override
    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;

    }

    @Override
    public String getuseText() {
        return null;
    }

    @Override
    public void itemUse() {

    }

    @Override
    public void dequip() {

    }
}
