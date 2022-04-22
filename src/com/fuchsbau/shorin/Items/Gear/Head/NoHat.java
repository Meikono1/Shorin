package com.fuchsbau.shorin.Items.Gear.Head;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class NoHat extends Armor {
    public NoHat() {
        super(0, 0, 100, "No Head", Slot.head);
    }

    @Override
    public String toString() {
        return "NoHat";
    }
}
