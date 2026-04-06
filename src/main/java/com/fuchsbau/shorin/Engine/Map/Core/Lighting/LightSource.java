package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

public class LightSource {
    public double x, y;
    public int brightTiles;
    public int dimTiles;
    public float intensity;
    public String label = "";

    public boolean sunlight = false;

    public LightSource() {
    }

    public LightSource(double x, double y, int brightTiles, int dimTiles, float intensity, boolean sunlight) {
        this.x = x;
        this.y = y;
        this.brightTiles = brightTiles;
        this.dimTiles = dimTiles;
        this.intensity = intensity;
        this.sunlight = sunlight;
    }
}