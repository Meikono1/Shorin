package com.fuchsbau.shorin.Races.Base;

import com.fuchsbau.shorin.Engine.Race.Size;

public interface RaceInterface {
    String raceName();

    String displayName();

    LifeStages lifeStage();

    Reproduction reproduction();

    Appearance appearance();

    Attributes baseAttributes();

    String shortDescription();

    String describeTypicalChildhood();

    String describeAdultRole();

    Size getSize();
}
