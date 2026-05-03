package com.fuchsbau.shorin.Engine.System.Combat;

import com.fuchsbau.shorin.Engine.System.Character.AbilityScore;

public enum SavingThrows {
    FORTITUDE(AbilityScore.CON), REFLEX(AbilityScore.DEX), WILL(AbilityScore.WIS);

    private final AbilityScore score;

    SavingThrows(AbilityScore score) {
        this.score = score;
    }

    public AbilityScore getScore() {
        return score;
    }

    public String getName() {
        return this.name();
    }
}
