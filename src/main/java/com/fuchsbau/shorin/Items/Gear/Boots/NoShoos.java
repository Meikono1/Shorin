package com.fuchsbau.shorin.Items.Gear.Boots;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class NoShoos extends Armor {
    public NoShoos() {
        super(0, 0, 100, "Bare Foot", Slot.boots);
    }

    @Override
    public String toString() {
        return "NoShoos";
    }
}
