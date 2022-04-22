package com.fuchsbau.shorin.Items.Gear.UpperBody;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class NoChest extends Armor {
    public NoChest() {
        super(0, 0, 100, "Bare Chest", Slot.chest);
    }

    @Override
    public String toString() {
        return "NoChest";
    }
}
