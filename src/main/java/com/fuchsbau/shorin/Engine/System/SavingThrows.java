package com.fuchsbau.shorin.Engine.System;

public enum SavingThrows {
    FORTITUDE(AbilityScores.CON), REFLEX(AbilityScores.DEX), WILL(AbilityScores.WIS);

    private final AbilityScores score;

    SavingThrows(AbilityScores score) {
        this.score = score;
    }

    public AbilityScores getScore() {
        return score;
    }

    public String getName() {
        return this.name();
    }
}
