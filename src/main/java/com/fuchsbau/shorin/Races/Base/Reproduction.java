package com.fuchsbau.shorin.Races.Base;

import java.util.Random;

public interface Reproduction {
    Boolean laysEggs();

    int minChildrenPerPregnancy();

    int maxChildrenPerPregnancy();

    int fertilityStartAge();

    int fertilityEndAge();

    int gestationInDays();

    default boolean canHaveChildren(int age) {
        return age >= fertilityStartAge() && age <= fertilityEndAge();
    }

    default int typicalChildrenPerPregnancy(Random rnd) {
        int min = minChildrenPerPregnancy();
        int max = maxChildrenPerPregnancy();
        return min + rnd.nextInt(max - min + 1);
    }
}
