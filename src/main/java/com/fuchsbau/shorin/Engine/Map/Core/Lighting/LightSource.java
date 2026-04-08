package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

public class LightSource {
    public double x, y;
    public int brightTiles;
    public int dimTiles;
    public String label = "";


    // Lichtfarbe — weiß
    public float intensity;
    public double colorR = 1.0;
    public double colorG = 1.0;
    public double colorB = 1.0;

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


    public void setColor(double r, double g, double b) {
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }
}