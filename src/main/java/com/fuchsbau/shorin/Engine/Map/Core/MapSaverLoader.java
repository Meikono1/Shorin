package com.fuchsbau.shorin.Engine.Map.Core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MapSaverLoader {

    public void saveMap(File file, GameMap gameMap, LightingSystem lightingSystem) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath())) {
            // Header
            w.write(gameMap.getRows() + " " + gameMap.getCols());
            w.newLine();

            // Grid
            for (int r = 0; r < gameMap.getRows(); r++) {
                for (int c = 0; c < gameMap.getCols(); c++) {
                    w.write(Integer.toString(gameMap.getTile(r, c).flags));
                    if (c < gameMap.getCols() - 1) w.write(" ");
                }
                w.newLine();
            }


        }
    }

    public void saveWorldMap(File out, MutableGameMap gameMap, LightingSystem lightingSystem) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(out.toPath())) {
            // Header
            w.write(gameMap.getRows() + " " + gameMap.getCols());
            w.newLine();

            // Grid
            for (int r = 0; r < gameMap.getRows(); r++) {
                for (int c = 0; c < gameMap.getCols(); c++) {
                    w.write(Integer.toString(gameMap.getTile(r, c).flags));
                    if (c < gameMap.getCols() - 1) w.write(" ");
                }
                w.newLine();
            }


        }
    }
}
