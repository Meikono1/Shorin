package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Waffen.Faust;

public class Char {

    private int health;
    Item Waffe = new Faust();


    public Char(int health) {
        this.health = health;
    }


    public void setWaffe(Item waffe) {
        Waffe = waffe;
    }

    public Item getWaffe() {
        return Waffe;
    }

    public int getHealth() {
        return health;
    }

}
