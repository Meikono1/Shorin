package com.fuchsbau.shorin.Items.Gear;

import com.fuchsbau.shorin.Items.Gear.Arms.NoArms;
import com.fuchsbau.shorin.Items.Gear.Boots.NoShoos;
import com.fuchsbau.shorin.Items.Gear.Head.NoHat;
import com.fuchsbau.shorin.Items.Gear.LowerBody.NoPants;
import com.fuchsbau.shorin.Items.Gear.UpperBody.NoChest;
import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.text.Text;

import java.util.LinkedList;

public class Armor implements Item {

    int delete = 0;
    public int armor;
    public int qualitaet;
    private String beschreibung;
    private final Slot slot;

    public Armor(int armor, int qualitaet, String text, Slot slot) {
        this.armor = armor;
        this.qualitaet = qualitaet;
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
        return "Equip";
    }

    @Override
    public void itemUse() {
        Game.getInstance().inventory.equip(this);
    }

    @Override
    public void dequip() {
        Game.getInstance().inventory.dequipArmor(this);
    }

    @Override
    public boolean isBase() {
        LinkedList<Class> classes = new LinkedList<>();
        classes.add(NoPants.class);
        classes.add(NoArms.class);
        classes.add(NoShoos.class);
        classes.add(NoHat.class);
        classes.add(NoChest.class);
        return classes.contains(this.getClass());
    }

    @Override
    public int compareTo(Item o) {
        if (!o.getClass().equals(this.getClass())) {
            return o.toString().compareTo(this.toString());
        }
        Armor item = (Armor) o;

        if (this.beschreibung.equals(item.beschreibung) && item.slot == this.slot && this.armor == item.armor) {
            return 0;
        }
        return o.toString().compareTo(this.toString());
    }

    public Slot getSlot() {
        return this.slot;
    }
}
