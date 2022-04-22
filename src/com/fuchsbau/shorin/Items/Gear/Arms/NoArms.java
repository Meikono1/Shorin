package com.fuchsbau.shorin.Items.Gear.Arms;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Slot;

public class NoArms extends Armor {
    public NoArms() {
        super(0, 1, 100, "Bare Arms", Slot.arms);
    }

    @Override
    public String toString() {
        return "NoArms";
    }
}
