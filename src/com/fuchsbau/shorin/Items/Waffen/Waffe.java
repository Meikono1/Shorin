package com.fuchsbau.shorin.Items.Waffen;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Materialen;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Waffe implements Item {

    private String beschreibung;
    private final Materialen material;
    private final int qualitaet;
    private final int schaden;
    private final int zustand;

    public Waffe(int schaden, int zustand, int qualitaet, Materialen material, String text) {

        this.material = material;
        this.qualitaet = qualitaet;
        this.schaden = schaden;
        this.zustand = zustand;

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
                "beschreibung=" + beschreibung +
                ", material=" + material +
                ", qualitaet=" + qualitaet +
                ", schaden=" + schaden +
                ", zustand=" + zustand +
                '}';
    }

    @Override
    public int compareTo(Item o) {
        if (!o.getClass().equals(this.getClass())) {
            return -1;
        }
        Waffe item = (Waffe) o;

        if (this.beschreibung.equals(item.beschreibung)
                && item.material == this.material
                && item.schaden == this.schaden
                && item.zustand == this.zustand
                && this.qualitaet == item.qualitaet) {
            return 0;
        }
        return -1;
    }
}
