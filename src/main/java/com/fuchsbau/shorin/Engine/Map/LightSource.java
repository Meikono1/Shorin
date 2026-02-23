package com.fuchsbau.shorin.Engine.Map;

public class LightSource {
    public final int row, col;
    public final int brightTiles;
    public final int dimTiles;
    public final float intensity;

    public LightSource(int row, int col, int brightTiles,int dimTiles, float intensity) {
        this.row = row;
        this.col = col;
        this.brightTiles = brightTiles;
        this.dimTiles = dimTiles;
        this.intensity = intensity;
    }
}