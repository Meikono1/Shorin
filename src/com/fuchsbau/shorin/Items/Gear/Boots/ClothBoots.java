package com.fuchsbau.shorin.Items.Gear.Boots;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class ClothBoots extends Armor {
    public ClothBoots() {
        super(1, 1, 100, "Cloth Boots", Slot.boots);
    }

    @Override
    public String toString() {
        return "ClothBoots";
    }
}
