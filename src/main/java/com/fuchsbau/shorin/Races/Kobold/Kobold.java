package com.fuchsbau.shorin.Races.Kobold;

import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Races.Base.*;

public class Kobold extends Race {
    public Kobold(String raceName, String name, byte speed, Attributes baseAttributes, int maxHealth, Size size, LifeStages lifeStage, Reproduction reproduction, Appearance appearance) {
        super(raceName, name, speed, baseAttributes, maxHealth, size, lifeStage, reproduction, appearance);
    }
}
