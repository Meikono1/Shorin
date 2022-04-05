package com.fuchsbau.shorin.Items.Gear.LowerBody;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class NoPants extends Armor {
    public NoPants() {
        super(0, 0, "No Pants", Slot.pants);
    }

    @Override
    public String toString() {
        return "NoPants";
    }
}
