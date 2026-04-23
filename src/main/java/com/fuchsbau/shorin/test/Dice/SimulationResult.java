package com.fuchsbau.shorin.test.Dice;

import java.util.List;

public class SimulationResult {
    public List<Integer> ids;
    public List<float[]> quaternions;
    public List<float[]> positions;
    public CollideData[] detectedCollides;
    public List<Boolean> deads;
    public int iterationsNeeded;

    public SimulationResult(List<Integer> ids, List<float[]> quaternions, List<float[]> positions,
                            CollideData[] detectedCollides, List<Boolean> deads, int iterationsNeeded) {
        this.ids = ids;
        this.quaternions = quaternions;
        this.positions = positions;
        this.detectedCollides = detectedCollides;
        this.deads = deads;
        this.iterationsNeeded = iterationsNeeded;
    }
}