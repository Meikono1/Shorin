package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

public class IndoorZone {

    // World-Koordinaten (px)
    public double x;
    public double y;
    public double width;
    public double height;
    public String label = "";

    // Performance: vorberechnete Grenzen
    public double x2; // x + width
    public double y2; // y + height

    public IndoorZone() {}

    public IndoorZone(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.x2 = x + width;
        this.y2 = y + height;
    }

    // World-Koordinaten prüfen
    public boolean contains(double wx, double wy) {
        return wx >= x && wx < x2 && wy >= y && wy < y2;
    }

    // Tile prüfen (Tile-Center wird genutzt)
    public boolean containsTile(int row, int col, double baseTile) {
        double cx = col * baseTile + baseTile * 0.5;
        double cy = row * baseTile + baseTile * 0.5;
        return contains(cx, cy);
    }

    public void recalcBounds() {
        x2 = x + width;
        y2 = y + height;
    }
}