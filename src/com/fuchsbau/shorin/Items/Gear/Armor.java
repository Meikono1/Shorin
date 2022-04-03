package com.fuchsbau.shorin.Items.Gear;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Armor implements Item {

    int delete = 0;
    int armor;
    private String beschreibung;
    private Slot slot;

    public Armor(int armor, int qualitaet, String text, Slot slot) {
        this.armor = armor;
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

    @Override
    public int compareTo(Item o) {
        if (!o.getClass().equals(this.getClass())) {
            return -1;
        }
        Armor item = (Armor) o;

        if (this.beschreibung.equals(item.beschreibung) && item.slot == this.slot && this.armor == item.armor) {
            return 0;
        }
        return -1;
    }
}
