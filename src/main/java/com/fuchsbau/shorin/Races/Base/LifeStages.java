package com.fuchsbau.shorin.Races.Base;

/***
 * Idee: Jeder hat ein alter, welches mit der Geburt bei 0 startet.
 * Soweit so einfach, diese chars werden älter und wachsen, bedeutet es gibt eine Startgröße und eine Endgröße
 * Auch sind alle ab einen Bestimmten alter tätig um zu lernen und der Familie zu helfen.
 *
 * Die Welt lebt und ist realistisch.
 *
 * Es gibt ein alter ab welchem eine rasse ausgewachsen sind.
 * Aber auch ein alter ab wann jemand als erwachsen gilt und der Spieler mit interagieren darf.
 *
 * Eine Lebenserwartung kommt dazu, welche aussagt ab wann der Char dem ende zugeht.
 * Lebenserwartung ist != maximales alter.
 *
 */

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
