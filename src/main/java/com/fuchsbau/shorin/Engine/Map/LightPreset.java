package com.fuchsbau.shorin.Engine.Map;

public enum LightPreset {
    CANDLE("Candle", 5, 15, 1.0f),
    TORCH("Torch", 20, 40, 1.0f),
    LANTERN("Lantern", 30, 60, 1.0f),
    SUNLIGHT("Sonnen-Tuerlicht", 5, 15, 1.0f, true);

    public final String label;
    public final int brightFt;
    public final int dimFt;
    public boolean sonnenLicht = false;
    public final float intensity;  // multiplier

    LightPreset(String label, int brightFt, int dimFt, float intensity) {
        this.label = label;
        this.brightFt = brightFt;
        this.dimFt = dimFt;
        this.intensity = intensity;
    }

    LightPreset(String label, int brightFt, int dimFt, float intensity, boolean sonnenLicht) {
        this.label = label;
        this.brightFt = brightFt;
        this.dimFt = dimFt;
        this.intensity = intensity;
        this.sonnenLicht = sonnenLicht;
    }

    public int brightTiles() {
        return Math.max(1, brightFt / 5);
    }

    public int dimTiles() {
        return Math.max(1, dimFt / 5);
    }
}