package com.fuchsbau.shorin.Items.Weapons;

import com.fuchsbau.shorin.Items.Material;


public class Broadsword extends Weapon {

    public Broadsword(int schaden, int zustand, int qualitaet, Material material, String text) {
        super(schaden, zustand, qualitaet, material, text);
    }
    @Override
    public String toString() {
        return "Broadsword";
    }


}
