package com.fuchsbau.shorin.Items.Gear.LowerBody;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class ClothPants extends Armor {
    public ClothPants() {
        super(1, 1, "Cloth Pants", Slot.pants);
    }

    @Override
    public String toString() {
        return "Clothpants";
    }
}
