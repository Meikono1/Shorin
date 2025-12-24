package com.fuchsbau.shorin.Items.Weapons;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Material;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.text.Text;

public class Weapon implements Item {

    private String descrition;
    private final Material material;
    private final int quality;
    private final int demage;
    private final int condition;

    public Weapon(int demage, int condition, int quality, Material material, String descrition) {
        this.material = material;
        this.quality = quality;
        this.demage = demage;
        this.condition = condition;
        this.descrition = descrition;
    }


    @Override
    public Text getText() {
        return SceneBuilder.getSceneBuilder().makeText(descrition);

    }

    @Override
    public void setText(String text) {
        this.descrition = text;
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

        if (this.descrition.equals(item.descrition)
                && item.material == this.material
                && item.demage == this.demage
                && item.condition == this.condition
                && this.quality == item.quality) {
            return 0;
        }
        return -1;
    }
}
