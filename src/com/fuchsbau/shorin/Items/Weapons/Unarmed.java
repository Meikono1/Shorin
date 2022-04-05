package com.fuchsbau.shorin.Items.Weapons;

import com.fuchsbau.shorin.Items.Material;

public class Unarmed extends Weapon {
    public Unarmed() {
        super(2, 1, 1, Material.stein, "Unarmed");

    }

    public Unarmed(int schaden, int zustand, int qualitaet, Material material, String text) {
        super(schaden, zustand, qualitaet, material, text);
    }


    @Override
    public String toString() {
        return "Unarmed";
    }
}
