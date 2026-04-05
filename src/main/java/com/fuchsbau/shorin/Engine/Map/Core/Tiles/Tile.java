package com.fuchsbau.shorin.Engine.Map.Core.Tiles;

import com.fuchsbau.shorin.Engine.Map.Core.Lighting.Lightlevel;
import com.fuchsbau.shorin.Engine.Race.Size;
import javafx.scene.paint.Color;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tile = statische Terrain-/Map-Info + optionaler Occupant (dynamisch).
 */
public final class Tile {

    /**
     * Terrain/Map flags.
     */
    public int flags;

    // --- Flags (bitmask) ---
    public static final int DISABLED = 1; // impassable tile
    public static final int WALL = 1 << 1; // impassable tile
    public static final int DOOR = 1 << 2; // door tile
    public static final int DOOR_OPEN = 1 << 3; // only meaningful if DOOR set
    public static final int DIFFICULT = 1 << 4; // +5 ft
    public static final int GREATER_DIFFICULT = 1 << 5; // +10 ft
    public static final int HAZARDOUS = 1 << 6; // triggers damage on move-through/enter
    public static final int NARROW_SURFACE = 1 << 7; // Balance rules
    public static final int UNEVEN_GROUND = 1 << 8; // Balance rules
    public static final int OUTSIDE = 1 << 9; // daylight can apply


    // ---- Debug Colours (cached, keine Allokation im Render) ----
    private static final Color DEBUG_DEFAULT = Color.rgb(28, 28, 40);
    private static final Color DEBUG_WALL = Color.rgb(80, 80, 90);
    private static final Color DEBUG_DOOR = Color.rgb(120, 90, 40);
    private static final Color DEBUG_GREATER = Color.rgb(30, 120, 30);
    private static final Color DEBUG_DIFFICULT = Color.rgb(40, 90, 40);
    private static final Color DEBUG_HAZARD = Color.rgb(140, 40, 40);

    private static final int DEBUG_MASK =
            WALL | DOOR | GREATER_DIFFICULT |
                    DIFFICULT | HAZARDOUS;
    /**
     * Optional occupant currently on this tile (creature or object). Null = empty.
     */
    public Occupant occupant;

    /**
     * Optional: id/metadata hooks (e.g., hazard profile, balance DC profile). -1 means none.
     */
    public short hazardId = -1;
    public short balanceProfileId = -1;

    private Lightlevel lightlevel;
    private float brightness;

    public Tile(int flags) {
        brightness = 0;
        lightlevel = Lightlevel.DARKNESS;
        this.flags = flags;
    }

    public boolean has(int mask) {
        return (flags & mask) != 0;
    }

    public void add(int mask) {
        flags |= mask;
    }

    public void remove(int mask) {
        flags &= ~mask;
    }

    public void clearAll() {
        flags = 0;
    }

    public boolean blocksLight() {
        if (has(WALL)) return true;
        if (has(DOOR) && !has(DOOR_OPEN)) return true;
        return false;
    }

    public boolean isOutside() {
        return has(OUTSIDE);
    }

    public void set(int mask, boolean value) {
        if (value) add(mask);
        else remove(mask);
    }

    public static Tile empty() {
        return new Tile(OUTSIDE);
    }

    public boolean hasDebugTerrain() {
        return (flags & DEBUG_MASK) != 0;
    }

    public Color getDebugColour() {
        if ((flags & WALL) != 0) return DEBUG_WALL;
        if ((flags & DOOR) != 0) return DEBUG_DOOR;
        if ((flags & GREATER_DIFFICULT) != 0) return DEBUG_GREATER;
        if ((flags & DIFFICULT) != 0) return DEBUG_DIFFICULT;
        if ((flags & HAZARDOUS) != 0) return DEBUG_HAZARD;

        return DEBUG_DEFAULT;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public Lightlevel getLightlevel() {
        return lightlevel;
    }

    public void setLightlevel(Lightlevel lightlevel) {
        this.lightlevel = lightlevel;
    }

    public double getTerrainmultiplier() {
        if (this.has(GREATER_DIFFICULT)) {
            return (double) 1 / 3;
        } else if (this.has(DIFFICULT)) {
            return 2;
        } else {
            return 1;
        }
    }

    // --- Occupant (dynamic) ---
    public static final class Occupant {
        /**
         * Whether this occupant is a creature (vs. object).
         */
        public final boolean creature;
        public final Size size;

        /**
         * Factions the occupant belongs to. Used to infer "willingness".
         */
        public final Set<Faction> factions;

        /**
         * prone/incapacitated flags (affects space sharing rules).
         */
        public final boolean proneOrIncapacitated;

        /**
         * dead/unconscious flags (affects space sharing rules).
         */
        public final boolean deadOrUnconscious;

        /**
         * Tiny is special for sharing spaces.
         */
        public boolean isTiny() {
            return size == Size.TINY;
        }

        public Occupant(
                boolean creature,
                Size size,
                Set<Faction> factions,
                boolean proneOrIncapacitated,
                boolean deadOrUnconscious
        ) {
            this.creature = creature;
            this.size = size;
            this.factions = (factions == null) ? EnumSet.noneOf(Faction.class) : factions;
            this.proneOrIncapacitated = proneOrIncapacitated;
            this.deadOrUnconscious = deadOrUnconscious;
        }

        /**
         * Convenience: occupant is "friendly" relative to mover factions.
         */
        public boolean isFriendlyTo(Set<Faction> moverFactions) {
            if (moverFactions == null || moverFactions.isEmpty()) return false;
            for (Faction f : moverFactions) {
                if (factions.contains(f)) return true;
            }
            return false;
        }
    }

    /**
     * Your game-specific factions. Extend as needed.
     */
    public enum Faction {
        PLAYER,
        CITY_GUARD,
        BANDITS,
        MONSTERS
    }
}
