package com.fuchsbau.shorin.Items.Gear.Head;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class ClothHelm extends Armor {
    public ClothHelm() {
        super(1, 1, 100, "Cloth hat", Slot.head);
    }

    @Override
    public String toString() {
        return "Cloth Helm";
    }
}
