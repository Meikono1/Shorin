package com.fuchsbau.shorin.Engine.Map.Core.Tiles;

public class MutableGameMap extends GameMap {
    public MutableGameMap() {
        rows = 30;
        cols = 30;
    }

    public MutableGameMap(int rows, int cols) {
        super(rows, cols);
    }

    // --- Grid Manipulation ---
    public void resizeGrid(int newRows, int newCols) {
        if (newRows <= 0 || newCols <= 0) return;

        Tile[][] newGrid = new Tile[newRows][newCols];

        // alles erstmal leer füllen
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                newGrid[r][c] = Tile.empty();
            }
        }

        // alten Inhalt kopieren (nur soweit möglich)
        int copyRows = Math.min(rows, newRows);
        int copyCols = Math.min(cols, newCols);

        for (int r = 0; r < copyRows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, copyCols);
        }

        grid = newGrid;
        rows = newRows;
        cols = newCols;
    }

    public void clearAll() {
        for (int c = 0; c < grid.length; c++) {
            for (int r = 0; r < grid[c].length; r++) {
                grid[c][r] = Tile.empty();
            }
        }
        this.getWalls().clear();
    }

    public void addRowTop() {
        Tile[][] newGrid = new Tile[rows + 1][cols];

        // neue erste Zeile
        for (int c = 0; c < cols; c++) newGrid[0][c] = Tile.empty();

        // alten Inhalt 1 nach unten kopieren
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r + 1], 0, cols);
        }

        grid = newGrid;
        rows++;
    }

    public void addRowBottom() {
        Tile[][] newGrid = new Tile[rows + 1][cols];

        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols);
        }
        for (int c = 0; c < cols; c++) newGrid[rows][c] = Tile.empty();

        grid = newGrid;
        rows++;
    }

    public void addColLeft() {
        Tile[][] newGrid = new Tile[rows][cols + 1];

        for (int r = 0; r < rows; r++) {
            newGrid[r][0] = Tile.empty();
            System.arraycopy(grid[r], 0, newGrid[r], 1, cols);
        }

        grid = newGrid;
        cols++;
    }

    public void addColRight() {
        Tile[][] newGrid = new Tile[rows][cols + 1];

        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols);
            newGrid[r][cols] = Tile.empty();
        }

        grid = newGrid;
        cols++;
    }

    public void removeRowTop() {
        if (rows <= 1) return;

        Tile[][] newGrid = new Tile[rows - 1][cols];
        for (int r = 1; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r - 1], 0, cols);
        }

        grid = newGrid;
        rows--;
    }

    public void removeRowBottom() {
        if (rows <= 1) return;

        Tile[][] newGrid = new Tile[rows - 1][cols];
        for (int r = 0; r < rows - 1; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols);
        }

        grid = newGrid;
        rows--;
    }

    public void removeColLeft() {
        if (cols <= 1) return;

        Tile[][] newGrid = new Tile[rows][cols - 1];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 1, newGrid[r], 0, cols - 1);
        }

        grid = newGrid;
        cols--;
    }

    public void removeColRight() {
        if (cols <= 1) return;

        Tile[][] newGrid = new Tile[rows][cols - 1];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, cols - 1);
        }

        grid = newGrid;
        cols--;
    }

}
