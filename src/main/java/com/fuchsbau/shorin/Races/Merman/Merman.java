package com.fuchsbau.shorin.Races.Merman;

import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Races.Base.*;

public class Merman extends Race {
    public Merman(String raceName, String name, byte speed, Attributes baseAttributes, int maxHealth, Size size, LifeStages lifeStage, Reproduction reproduction, Appearance appearance) {
        super(raceName, name, speed, baseAttributes, maxHealth, size, lifeStage, reproduction, appearance);
    }
}
