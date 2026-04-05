package com.fuchsbau.shorin.Engine.Map.Core.Walls;

public enum WallType {
    WALL,           // blockiert Bewegung + Licht + Sicht
    TERRAIN,        // blockiert Bewegung, Sicht erst wenn Strahl 2+ Terrain schneidet
    INVISIBLE,      // blockiert nur Bewegung
    ETHEREAL,       // blockiert Sicht + Sound
    DOOR,           // alles wenn zu, nichts wenn offen
    SECRET_DOOR,    // wie WALL bis Trigger aufdeckt
    WINDOW;         // blockiert Bewegung + Sicht bis 2 Tiles Distanz, nicht Licht

    public boolean blocksMovement() {
        return switch (this) {
            case WALL, TERRAIN, INVISIBLE, DOOR, SECRET_DOOR, WINDOW -> true;
            case ETHEREAL -> false;
        };
    }

    public boolean blocksLight() {
        return switch (this) {
            case WALL, DOOR, SECRET_DOOR -> true;
            case TERRAIN, INVISIBLE, ETHEREAL, WINDOW -> false;
        };
    }

    // Einfaches blocksSight — für alles außer TERRAIN und WINDOW
    public boolean blocksSight() {
        return switch (this) {
            case WALL, DOOR, SECRET_DOOR, ETHEREAL -> true;
            case TERRAIN, INVISIBLE, WINDOW -> false;
        };
    }

    // TERRAIN braucht Zähler im Raycasting — blockiert wenn terrainHits >= 2
    public boolean isTerrainSight() {
        return this == TERRAIN;
    }

    public boolean blocksSound() {
        return this == ETHEREAL;
    }
}