package com.fuchsbau.shorin.Engine.Dice;

public record DiceTray(
        double x,
        double y,
        double width,
        double height
) {
    public double minX() {
        return x;
    }

    public double maxX() {
        return x + width;
    }

    public double minY() {
        return y;
    }

    public double maxY() {
        return y + height;
    }
}