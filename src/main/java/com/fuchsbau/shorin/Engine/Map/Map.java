package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.Map.Core.Tile;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Map {
    private final Logger logger = FileLogger.getLogger();
    // --- meta ---
    public final String mapId;
    public final Path mapFile;

    private final List<Token> tokens = new ArrayList<>();

    // --- layers ---
    private Tile[][] tiles;             // terrain flags + occupant ptrs (occupants typically managed elsewhere)
    private int cols, rows;
    private float[][] light;            // 0..1, optional

    // --- background ---
    private String backgroundName;
    private Image backgroundImage;

    // --- View / camera ---
    private static final double BASE_TILE = 24.0; // world px
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    public Map(String mapId, File mapFile) {
        this.mapId = Objects.requireNonNull(mapId);
        this.mapFile = Objects.requireNonNull(mapFile.toPath());
        try {
            loadMap(mapFile, this);
        } catch (IOException e) {
            logger.severe("File not found: " + mapFile.toPath());
        }
    }

    public void loadMap(File file, Map map) throws IOException {
        try (BufferedReader r = Files.newBufferedReader(file.toPath())) {

            String[] header = r.readLine().split(" ");
            int rows = Integer.parseInt(header[0]);
            int cols = Integer.parseInt(header[1]);

            Tile[][] grid = new Tile[rows][cols];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    grid[i][j] = new Tile(0);

            String bgLine = r.readLine();
            if (bgLine != null && bgLine.startsWith("BG=")) {
                //backgroundImagePath = bgLine.substring(3);
                //loadBackgroundFromResources(backgroundImagePath);
            }

            for (int rIdx = 0; rIdx < rows; rIdx++) {
                String[] parts = r.readLine().split(" ");
                for (int cIdx = 0; cIdx < cols; cIdx++) {
                    grid[rIdx][cIdx].flags = Integer.parseInt(parts[cIdx]);
                }
            }
            map.setTiles(grid, cols, rows);
        }
    }

    public void setTiles(Tile[][] grid, int cols, int rows) {
        this.tiles = grid;
        this.cols = cols;
        this.rows = rows;
    }
}
