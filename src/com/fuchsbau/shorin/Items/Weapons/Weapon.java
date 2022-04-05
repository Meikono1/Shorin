package com.fuchsbau.shorin.Items.Weapons;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Material;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

public class Weapon implements Item {

    private String beschreibung;
    private final Material material;
    private final int qualitaet;
    private final int schaden;
    private final int zustand;

    public Weapon(int schaden, int zustand, int qualitaet, Material material, String text) {
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
        Game.getInstance().inventory.dequipWeapon(this);
    }

    @Override
    public boolean isBase() {
        return this.getClass().equals(Unarmed.class);
    }

    @Override
    public int compareTo(Item o) {
        if (!o.getClass().equals(this.getClass())) {
            return -1;
        }
        Weapon item = (Weapon) o;

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
