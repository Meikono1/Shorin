package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

import com.fuchsbau.shorin.Engine.Map.Core.Tiles.GameMap;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.Tile;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallSegment;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallType;
import com.fuchsbau.shorin.Engine.RPG.GameClock;

import static com.fuchsbau.shorin.Engine.Options.GameOptions.BASE_TILE;
import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class LightingSystem {

    private final int[] dr = {-1, 1, 0, 0};
    private final int[] dc = {0, 0, -1, 1};


    public void recomputeLightmapAll(GameMap gameMap) {
        // reset
        for (int r = 0; r < gameMap.getRows(); r++) {
            for (int c = 0; c < gameMap.getCols(); c++) {
                Tile t = gameMap.getTile(r, c);
                t.setBrightness(0f);
                t.setLightlevel(Lightlevel.DARKNESS);
            }
        }

        applySunlight(gameMap);

        // apply all lights
        for (LightSource ls : gameMap.getLights()) {
            applyLight(ls, gameMap);
        }
    }

    // LightingSystem — applySunlight anpassen
    private void applySunlight(GameMap gameMap) {
        float degree = getSunDegree();
        if (degree <= 0f) return;

        for (int r = 0; r < gameMap.getRows(); r++) {
            for (int c = 0; c < gameMap.getCols(); c++) {
                Tile t = gameMap.getTile(r, c);
                if (!t.has(Tile.OUTSIDE)) continue;

                // Indoor-Zone schirmt Tageslicht ab
                if (gameMap.isIndoor(r, c, BASE_TILE)) continue;

                Lightlevel lvl = degree >= 0.5f ? Lightlevel.BRIGHT_LIGHT : Lightlevel.DIM_LIGHT;
                t.setLightlevel(lvl);
                if (t.getBrightness() < degree) t.setBrightness(degree);
            }
        }

        // Nachbarn zu OUTSIDE bekommen min DIM — ebenfalls Indoor prüfen
        for (int r = 0; r < gameMap.getRows(); r++) {
            for (int c = 0; c < gameMap.getCols(); c++) {
                if (!gameMap.getTile(r, c).has(Tile.OUTSIDE)) continue;
                if (gameMap.isIndoor(r, c, BASE_TILE)) continue;

                for (int i = 0; i < 4; i++) {
                    int rr = r + dr[i];
                    int cc = c + dc[i];
                    if (!gameMap.inBounds(rr, cc)) continue;
                    Tile n = gameMap.getTile(rr, cc);
                    if (n.has(Tile.OUTSIDE)) continue;
                    if (gameMap.isIndoor(rr, cc, BASE_TILE)) continue;
                    if (n.getLightlevel() == Lightlevel.DARKNESS) {
                        n.setLightlevel(Lightlevel.DIM_LIGHT);
                        if (n.getBrightness() < 0.5f * degree) n.setBrightness(0.5f * degree);
                    }
                }
            }
        }
    }

    // Sonnenstand als 0.0 - 1.0
    // 0.0 = Nacht, 1.0 = Mittag
    public float getSunDegree() {
        int hour = GameClock.getInstance().getHour();
        int minute = GameClock.getInstance().getMinute();
        float timeOfDay = hour + minute / 60f;

        // Sonnenaufgang 6:00, Sonnenuntergang 20:00, Mittag 13:00
        if (timeOfDay < 6f || timeOfDay >= 20f) return 0f;  // Nacht

        // Dawn 6:00-7:00 → 0.0-0.5
        if (timeOfDay < 7f) return (timeOfDay - 6f) * 0.5f;

        // Dusk 19:00-20:00 → 0.5-0.0
        if (timeOfDay >= 19f) return (20f - timeOfDay) * 0.5f;

        // Tag 7:00-19:00 → Sinus-Kurve mit Peak bei 13:00
        float normalized = (timeOfDay - 7f) / 12f; // 0.0-1.0
        return 0.5f + 0.5f * (float) Math.sin(Math.PI * normalized);
    }

    // Light
    private void applyLight(LightSource ls, GameMap gameMap) {
        float effectiveIntensity = ls.intensity;
        if (ls.sunlight) {
            float sunDeg = getSunDegree();
            if (sunDeg <= 0f) return; // Nacht — kein Sonnenlicht
            effectiveIntensity = ls.intensity * sunDeg;
        }

        int brTiles = ls.brightTiles;
        int dimTiles = ls.dimTiles;
        int rad = Math.max(1, dimTiles);

        // Tile-space
        double lx = ls.x / BASE_TILE;
        double ly = ls.y / BASE_TILE;

        // Bounding box in Tiles
        int r0 = clamp((int) Math.floor(ly - rad) - 1, 0, gameMap.getRows() - 1);
        int r1 = clamp((int) Math.ceil(ly + rad) + 1, 0, gameMap.getRows() - 1);
        int c0 = clamp((int) Math.floor(lx - rad) - 1, 0, gameMap.getCols() - 1);
        int c1 = clamp((int) Math.ceil(lx + rad) + 1, 0, gameMap.getCols() - 1);

        // Center check (tile fractions)
        boolean isCenter =
                isNear(frac(lx), 0.5, 1e-6) &&
                        isNear(frac(ly), 0.5, 1e-6);

        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {

                double dx, dy;

                if (isCenter) {
                    // PF2 wie bisher: Tile-Center
                    double tx = c + 0.5;
                    double ty = r + 0.5;
                    dx = Math.abs(tx - lx);
                    dy = Math.abs(ty - ly);
                } else {
                    // Rand/Ecke: Distanz zum Tile-Quadrat (AABB)
                    dx = distanceToInterval(lx, c, c + 1.0);
                    dy = distanceToInterval(ly, r, r + 1.0);
                }

                int distTiles = distanceTiles(dx, dy);
                if (distTiles > dimTiles) continue;

                Lightlevel lvl = (distTiles <= brTiles) ? Lightlevel.BRIGHT_LIGHT : Lightlevel.DIM_LIGHT;

                float b = brightnessAtDistanceTiles(distTiles, brTiles, dimTiles, effectiveIntensity);
                if (b <= 0f) continue;

                Tile t = gameMap.getTile(r, c);

                boolean improvesBrightness = b > t.getBrightness();
                boolean improvesLevel =
                        (lvl == Lightlevel.BRIGHT_LIGHT && t.getLightlevel() != Lightlevel.BRIGHT_LIGHT)
                                || (lvl == Lightlevel.DIM_LIGHT && t.getLightlevel() == Lightlevel.DARKNESS);

                if (!improvesBrightness && !improvesLevel) continue;

                double tileWorldX = (c + 0.5) * BASE_TILE;
                double tileWorldY = (r + 0.5) * BASE_TILE;
                if (!gameMap.hasLineOfSightWalls(ls.x, ls.y, tileWorldX, tileWorldY)) continue;

                if (improvesBrightness) t.setBrightness(b);

                if (lvl == Lightlevel.BRIGHT_LIGHT) {
                    t.setLightlevel(Lightlevel.BRIGHT_LIGHT);
                } else if (t.getLightlevel() == Lightlevel.DARKNESS) {
                    t.setLightlevel(Lightlevel.DIM_LIGHT);
                }
            }
        }
    }

    private static double distanceToInterval(double v, double a, double b) {
        if (v < a) return a - v;
        if (v > b) return v - b;
        return 0.0;
    }

    private static double frac(double v) {
        return v - Math.floor(v);
    }

    private static boolean isNear(double a, double b, double eps) {
        return Math.abs(a - b) <= eps;
    }

    private int distanceTiles(double dx, double dy) {
        double max = Math.max(dx, dy);
        double min = Math.min(dx, dy);
        return (int) Math.floor(max + Math.floor(min / 2f));
    }

    private float brightnessAtDistanceTiles(int d, int bright, int dim, float intensity) {
        if (d <= bright) {
            float t = (bright == 0) ? 1f : (d / (float) bright);
            float base = 1.0f + (0.5f - 1.0f) * t;
            return base * intensity;
        }
        if (d <= dim) {
            float t = (d - bright) / (float) (dim - bright);
            float base = 0.5f + (0.0f - 0.5f) * t;
            return base * intensity;
        }
        return 0f;
    }

    private int distanceFt(int dxTiles, int dyTiles) {
        int max = Math.max(dxTiles, dyTiles);
        int min = Math.min(dxTiles, dyTiles);
        return max + (min / 2);
    }
}
