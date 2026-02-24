package com.fuchsbau.shorin.Engine.Map.Core;

import com.fuchsbau.shorin.Engine.Map.Token;

import java.util.ArrayList;
import java.util.List;

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
    public void addOrReplaceLight(int row, int col, int brightTiles, int dimtiles, float intensity) {
        lights.removeIf(l -> l.row == row && l.col == col);
        lights.add(new LightSource(row, col, brightTiles, dimtiles, intensity));
    }

    public boolean removeLightAt(int row, int col) {
        int before = lights.size();
        lights.removeIf(l -> l.row == row && l.col == col);
        return lights.size() != before;
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
