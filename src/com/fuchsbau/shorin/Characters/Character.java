package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Weapons.Unarmed;
import javafx.scene.text.TextFlow;

public class Character {

    private int health;
    private String beschreibung;
    Item Weapon = new Unarmed();


    public Character(int health) {
        this.health = health;
        beschreibung = "None";
    }


    public void setWeapon(Item weapon) {
        Weapon = weapon;
    }

    public Item getWeapon() {
        return Weapon;
    }

    public int getHealth() {
        return health;
    }

    public TextFlow makeBeschreibung(TextFlow pane) {
        return pane;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void heal(int i) {
        health += i;
    }
}
