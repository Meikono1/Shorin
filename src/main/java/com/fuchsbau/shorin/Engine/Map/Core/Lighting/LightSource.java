package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

public class LightSource {
    public double x, y;
    public int brightTiles;
    public int dimTiles;
    public float intensity;
    public String label = ""; // optional für Anzeige

    public LightSource() {
    }

    public LightSource(double x, double y, int brightTiles, int dimTiles, float intensity) {
        this.x = x;
        this.y = y;
        this.brightTiles = brightTiles;
        this.dimTiles = dimTiles;
        this.intensity = intensity;
    }
}