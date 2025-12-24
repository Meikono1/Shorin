package com.fuchsbau.shorin.Races.Base;

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

}
