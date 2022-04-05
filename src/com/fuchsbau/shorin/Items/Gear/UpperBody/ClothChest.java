package com.fuchsbau.shorin.Items.Gear.UpperBody;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class ClothChest extends Armor {

    public ClothChest() {
        super(1, 1, "Clotharmor", Slot.chest);
    }

    @Override
    public String toString() {
        return "Clothchest";
    }
}
