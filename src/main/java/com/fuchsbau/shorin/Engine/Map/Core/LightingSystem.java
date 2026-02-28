package com.fuchsbau.shorin.Engine.Map.Core;

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

        applySunlight(1800, gameMap);
        // apply all lights
        for (LightSource ls : gameMap.getLights()) {
            applyLight(ls, gameMap);
        }
    }

    private void applySunlight(int hour, GameMap gameMap) {
        //if (!isSunUp(hour)) return;

        // 1) Outside => Bright
        for (int r = 0; r < gameMap.getRows(); r++) {
            for (int c = 0; c < gameMap.getCols(); c++) {
                Tile t = gameMap.getTile(r, c);
                if (!t.has(Tile.OUTSIDE)) continue;

                t.setLightlevel(Lightlevel.BRIGHT_LIGHT);
                if (t.getBrightness() < 1.0f) t.setBrightness(1.0f);
            }
        }

        // 2) Adjacent to outside => min Dim (4-neighborhood oder 8-neighborhood; hier 4)
        for (int r = 0; r < gameMap.getRows(); r++) {
            for (int c = 0; c < gameMap.getCols(); c++) {
                if (!gameMap.getTile(r, c).has(Tile.OUTSIDE)) continue;

                for (int i = 0; i < 4; i++) {
                    int rr = r + dr[i];
                    int cc = c + dc[i];
                    if (!gameMap.inBounds(rr, cc)) continue;

                    Tile n = gameMap.getTile(rr, cc);
                    if (n.has(Tile.OUTSIDE)) continue;

                    // Optional: Blocker zwischen outside und neighbor (Tür/Wand)
                    // Für direktes Nachbarfeld reicht oft: wenn Nachbar selbst blockt, dann kein dim.
                    if (n.blocksLight()) continue;

                    if (n.getLightlevel() == Lightlevel.DARKNESS) {
                        n.setLightlevel(Lightlevel.DIM_LIGHT);
                    }
                    if (n.getBrightness() < 0.5f) n.setBrightness(0.5f);
                }
            }
        }
    }

    // Light
    private void applyLight(LightSource ls, GameMap gameMap) {
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

        // LOS start tile
        int srcR = clamp((int) Math.floor(ly), 0, gameMap.getRows() - 1);
        int srcC = clamp((int) Math.floor(lx), 0, gameMap.getCols() - 1);

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

                float b = brightnessAtDistanceTiles(distTiles, brTiles, dimTiles, ls.intensity);
                if (b <= 0f) continue;

                Tile t = gameMap.getTile(r, c);

                boolean improvesBrightness = b > t.getBrightness();
                boolean improvesLevel =
                        (lvl == Lightlevel.BRIGHT_LIGHT && t.getLightlevel() != Lightlevel.BRIGHT_LIGHT)
                                || (lvl == Lightlevel.DIM_LIGHT && t.getLightlevel() == Lightlevel.DARKNESS);

                if (!improvesBrightness && !improvesLevel) continue;

                if (!gameMap.hasLineOfSight(srcR, srcC, r, c)) continue;

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
