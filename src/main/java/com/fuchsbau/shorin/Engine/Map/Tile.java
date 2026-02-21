package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.Race.Size;

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
    public static final int WALL = 1; // impassable tile
    public static final int DOOR = 1 << 1; // door tile
    public static final int DOOR_OPEN = 1 << 2; // only meaningful if DOOR set
    public static final int DIFFICULT = 1 << 3; // +5 ft
    public static final int GREATER_DIFFICULT = 1 << 4; // +10 ft
    public static final int HAZARDOUS = 1 << 5; // triggers damage on move-through/enter
    public static final int NARROW_SURFACE = 1 << 6; // Balance rules
    public static final int UNEVEN_GROUND = 1 << 7; // Balance rules

    /**
     * Optional occupant currently on this tile (creature or object). Null = empty.
     */
    public Occupant occupant;

    /**
     * Optional: id/metadata hooks (e.g., hazard profile, balance DC profile). -1 means none.
     */
    public short hazardId = -1;
    public short balanceProfileId = -1;

    public Tile(int flags) {
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

    public void set(int mask, boolean value) {
        if (value) add(mask);
        else remove(mask);
    }

    public static Tile empty() {
        return new Tile(0);
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
         *  prone/incapacitated flags (affects space sharing rules).
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
