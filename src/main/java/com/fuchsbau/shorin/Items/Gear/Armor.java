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

    public final int armor;
    public final int qualitaet;
    private String text;
    private final Slot slot;
    public int zustand;

    public Armor(int armor, int qualitaet, int zustand, String text, Slot slot) {
        this.armor = armor;
        this.qualitaet = qualitaet;
        this.zustand = zustand;
        this.slot = slot;
        this.text = text;
    }

    @Override
    public Text getText() {
        return SceneBuilder.makeText(text);
    }

    @Override
    public void setText(String text) {
        this.text = text;
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

        if (this.text.equals(item.text) && item.slot == this.slot && this.armor == item.armor) {
            return 0;
        }
        return o.toString().compareTo(this.toString());
    }

    public Slot getSlot() {
        return this.slot;
    }
}
