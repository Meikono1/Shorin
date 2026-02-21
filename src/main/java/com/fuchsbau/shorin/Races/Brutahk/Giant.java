package com.fuchsbau.shorin.Races.Brutahk;

import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Races.Base.*;

public class Giant extends Race {
    public Giant(String raceName, String name, byte speed, Attributes baseAttributes, int maxHealth, Size size, LifeStages lifeStage, Reproduction reproduction, Appearance appearance) {
        super(raceName, name, speed, baseAttributes, maxHealth, size, lifeStage, reproduction, appearance);
    }
}
