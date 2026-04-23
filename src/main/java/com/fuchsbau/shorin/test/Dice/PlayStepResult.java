package com.fuchsbau.shorin.test.Dice;

import java.util.List;

public class PlayStepResult {
    public List<Integer> ids;
    public float[] quaternions;
    public float[] positions;
    public boolean worldAsleep;

    public PlayStepResult(List<Integer> ids, float[] quaternions, float[] positions, boolean worldAsleep) {
        this.ids = ids;
        this.quaternions = quaternions;
        this.positions = positions;
        this.worldAsleep = worldAsleep;
    }
}