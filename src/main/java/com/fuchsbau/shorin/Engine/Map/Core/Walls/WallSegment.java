package com.fuchsbau.shorin.Engine.Map.Core.Walls;

public class WallSegment {
    public double x1, y1, x2, y2;
    public WallType type = WallType.WALL;
    public boolean open = false; // für Doors

    public WallSegment() {
    }

    public WallSegment(double x1, double y1, double x2, double y2, WallType type) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.type = type;
    }

    public boolean isLightBlocker() {
        if (type == WallType.DOOR || type == WallType.SECRET_DOOR) return !open;
        return type.blocksLight();
    }

    public boolean isMovementBlocker() {
        if (type == WallType.DOOR || type == WallType.SECRET_DOOR) return !open;
        return type.blocksMovement();
    }
}