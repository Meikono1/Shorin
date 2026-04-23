package com.fuchsbau.shorin.test.Dice;

public class CollideData {
    public String source;
    public String type;
    public String material;
    public double strength;

    public CollideData(String source, String type, String material, double strength) {
        this.source   = source;
        this.type     = type;
        this.material = material;
        this.strength = strength;
    }
}