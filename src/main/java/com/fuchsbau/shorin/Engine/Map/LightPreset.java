package com.fuchsbau.shorin.Engine.Map;

public enum LightPreset {
    CANDLE("Candle", 5, 15, 1.0f),
    TORCH("Torch", 20, 40, 1.0f),
    LANTERN("Lantern", 30, 60, 1.0f);

    public final String label;
    public final int brightFt;
    public final int dimFt;
    public final float intensity;  // multiplier

    LightPreset(String label, int brightFt, int dimFt, float intensity) {
        this.label = label;
        this.brightFt = brightFt;
        this.dimFt = dimFt;
        this.intensity = intensity;
    }

    public int brightTiles() {
        return Math.max(1, brightFt / 5);
    }

    public int dimTiles() {
        return Math.max(1, dimFt / 5);
    }
}