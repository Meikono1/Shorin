package com.fuchsbau.shorin.Engine.System;

public class SlotEntry {
    public SlotType type = SlotType.CLASS_FEAT;
    public String selected = "";

    public SlotEntry() {
    }

    public SlotEntry(SlotType type) {
        this.type = type;
    }
}