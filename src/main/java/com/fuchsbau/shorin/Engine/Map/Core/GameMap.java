package com.fuchsbau.shorin.Engine.Map.Core;

import com.fuchsbau.shorin.Engine.Map.Token;

import java.util.ArrayList;
import java.util.List;

import static com.fuchsbau.shorin.Engine.Options.GameOptions.BASE_TILE;


public class GameMap {
    // --- Grid model ---
    protected int rows = 30;
    protected int cols = 30;

    protected Tile[][] grid = new Tile[rows][cols];
    private final List<Token> tokens = new ArrayList<>();
    private final List<LightSource> lights = new ArrayList<>();

    public GameMap(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Tile[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = Tile.empty();
            }
        }
    }

    public GameMap() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = Tile.empty();
            }
        }
    }


    // --- Grid Calculation ---
    public boolean hasLineOfSight(int r0, int c0, int r1, int c1) {
        int dx = Math.abs(c1 - c0);
        int dy = Math.abs(r1 - r0);

        int sx = c0 < c1 ? 1 : -1;
        int sy = r0 < r1 ? 1 : -1;

        int err = dx - dy;

        int x = c0;
        int y = r0;

        while (true) {
            // Ziel erreicht
            if (x == c1 && y == r1) return true;

            int e2 = 2 * err;

            int nx = x;
            int ny = y;

            boolean stepX = false;
            boolean stepY = false;

            if (e2 > -dy) {
                err -= dy;
                nx += sx;
                stepX = true;
            }
            if (e2 < dx) {
                err += dx;
                ny += sy;
                stepY = true;
            }

            // Diagonal-Step => Corner-Block check
            if (stepX && stepY) {
                // Die beiden orthogonalen Nachbarn an der Ecke:
                // (y, nx) und (ny, x)
                if (inBounds(y, nx) && inBounds(ny, x)) {
                    if (getTile(y, nx).blocksLight() && getTile(ny, x).blocksLight()) {
                        return false; // Ecke dicht
                    }
                }
            }

            x = nx;
            y = ny;

            // Blocker auf dem Tile selbst (außer Start)
            if (!(x == c0 && y == r0)) {
                if (getTile(y, x).blocksLight()) return false;
            }
        }
    }

    // --- Light ---
    public void addOrReplaceLight(double worldX,
                                  double worldY,
                                  int brightTiles,
                                  int dimTiles,
                                  float intensity) {

        double[] snapped = snapWorldToCornersOrCenter(worldX, worldY);
        double snappedX = snapped[0];
        double snappedY = snapped[1];

        final double eps = 0.0001;

        lights.removeIf(l ->
                Math.abs(l.x - snappedX) < eps &&
                        Math.abs(l.y - snappedY) < eps
        );

        lights.add(new LightSource(snappedX, snappedY, brightTiles, dimTiles, intensity));
    }

    private double[] snapWorldToCornersOrCenter(double worldX, double worldY) {
        int col = (int) Math.floor(worldX / BASE_TILE);
        int row = (int) Math.floor(worldY / BASE_TILE);

        double tileX = col * BASE_TILE;
        double tileY = row * BASE_TILE;
        double t = BASE_TILE;

        // Kandidaten: Center + 4 Ecken
        double[][] pts = new double[][]{
                {tileX + t * 0.5, tileY + t * 0.5}, // center
                {tileX, tileY},                     // NW
                {tileX + t, tileY},                 // NE
                {tileX, tileY + t},                 // SW
                {tileX + t, tileY + t}              // SE
        };

        double bestX = pts[0][0], bestY = pts[0][1];
        double bestD2 = dist2(worldX, worldY, bestX, bestY);

        for (int i = 1; i < pts.length; i++) {
            double d2 = dist2(worldX, worldY, pts[i][0], pts[i][1]);
            if (d2 < bestD2) {
                bestD2 = d2;
                bestX = pts[i][0];
                bestY = pts[i][1];
            }
        }
        return new double[]{bestX, bestY};
    }

    private double dist2(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return dx * dx + dy * dy;
    }

    public boolean removeLightNear(double mouseScreenX, double mouseScreenY,
                                   double camWorldX, double camWorldY, double zoom,
                                   double tolerancePx) {

        int bestIdx = -1;
        double bestDist2 = tolerancePx * tolerancePx;

        for (int i = 0; i < lights.size(); i++) {
            LightSource ls = lights.get(i);

            // LightSource ist in world px
            double worldX = ls.x;
            double worldY = ls.y;

            // world -> screen
            double sx = (worldX - camWorldX) * zoom;
            double sy = (worldY - camWorldY) * zoom;

            double dx = sx - mouseScreenX;
            double dy = sy - mouseScreenY;
            double d2 = dx * dx + dy * dy;

            if (d2 <= bestDist2) {
                bestDist2 = d2;
                bestIdx = i;
            }
        }

        if (bestIdx >= 0) {
            lights.remove(bestIdx);
            return true;
        }
        return false;
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return rows;
    }

    public Tile getTile(int row, int col) {
        return grid[row][col];
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<LightSource> getLights() {
        return lights;
    }
}
