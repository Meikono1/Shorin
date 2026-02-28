package com.fuchsbau.shorin.Engine.Map.Core;

public class LightSource {
    public final double x;
    public final double y;
    public final int brightTiles;
    public final int dimTiles;
    public final float intensity;

    public LightSource(double x, double y, int brightTiles,int dimTiles, float intensity) {
        this.x = x;
        this.y = y;
        this.brightTiles = brightTiles;
        this.dimTiles = dimTiles;
        this.intensity = intensity;
    }
}