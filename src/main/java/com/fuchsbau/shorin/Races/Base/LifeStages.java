package com.fuchsbau.shorin.Races.Base;

public interface LifeStages {
    int ageAdult();

    int ageMiddle();

    int ageOld();

    int ageMax();

    Boolean canDieNaturally();

    float dyingChance();

    default String lifeStage(int age) {
        if (age < ageAdult()) return "child";
        if (age < ageMiddle()) return "adult";
        if (age < ageOld()) return "middle-aged";
        if (age < ageMax()) return "old";
        return "very old";
    }

}
