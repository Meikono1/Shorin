package com.fuchsbau.shorin.Engine.Map.Core;

import com.fuchsbau.shorin.Engine.Editor.Module.NpcModule;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightSource;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.GameMap;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.MutableGameMap;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallSegment;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallType;
import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Engine.System.NpcBuild;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Editor.Module.NpcModule.loadNpcsfromDisk;

/**
 * MapSaverLoader – Binärformat (.shorin) für alle Map-Typen.
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │  Dateistruktur .shorin                                       │
 * │                                                              │
 * │  [4 bytes] Magic: 'S','H','O','R'                            │
 * │  [1 byte]  Version (aktuell: 1)                              │
 * │  [1 byte]  MapType: 0=WorldMap, 1=LocalMap, 2=Battlemap      │
 * │  [4 bytes] rows                                              │
 * │  [4 bytes] cols                                              │
 * │  [rows × cols × 4 bytes] Tile-flags                          │
 * │                                                              │
 * │  --- nur WorldMap (type=0) ---                               │
 * │  [4 bytes] locationCount                                     │
 * │    per Location:                                             │
 * │      [4 bytes] row                                           │
 * │      [4 bytes] col                                           │
 * │      [4 bytes] tileRadius  (belegt N×N Tiles)                │
 * │      [UTF]     name                                          │
 * │      [UTF]     linkedFile  (relativer Pfad zur SubMap)       │
 * │                                                              │
 * │  --- nur Battlemap (type=2) ---                              │
 * │  [4 bytes] lightCount                                        │
 * │    per LightSource:                                          │
 * │      [8 bytes] x (double)                                    │
 * │      [8 bytes] y (double)                                    │
 * │      [4 bytes] brightTiles (int)                             │
 * │      [4 bytes] dimTiles    (int)                             │
 * │      [4 bytes] intensity   (float)                           │
 * └──────────────────────────────────────────────────────────────┘
 */
public class MapSaverLoader {

    private static final Logger logger = FileLogger.getLogger();

    private static final byte[] MAGIC = {'S', 'H', 'O', 'R'};
    private static final byte VERSION = 1;

    public static final byte TYPE_WORLD = 0;
    public static final byte TYPE_LOCAL = 1;
    public static final byte TYPE_BATTLE = 2;

    // Location-Record (WorldMap)

    /**
     * Repräsentiert einen platzierten Ort auf der WorldMap.
     * Belegt tileRadius × tileRadius Tiles um (row, col) herum.
     */
    public static class LocationMarker {
        public int row;
        public int col;
        public int tileRadius;
        public String name;
        public String linkedFile;

        public LocationMarker(int row, int col, int tileRadius, String name, String linkedFile) {
            this.row = row;
            this.col = col;
            this.tileRadius = tileRadius;
            this.name = name;
            this.linkedFile = linkedFile;
        }
    }

    // SAVE

    /**
     * Speichert eine WorldMap mit optionalen Location-Markern.
     */
    public void saveWorldMap(File file, GameMap map,
                             List<LocationMarker> locations) throws IOException {
        try (DataOutputStream out = openForWrite(file)) {
            writeHeader(out, TYPE_WORLD, map.getRows(), map.getCols());
            writeTiles(out, map);
            writeLocations(out, locations);
        }
    }

    /**
     * Speichert eine LocalMap (kein Lighting, keine Locations).
     */
    public void saveLocalMap(File file, GameMap map) throws IOException {
        try (DataOutputStream out = openForWrite(file)) {
            writeHeader(out, TYPE_LOCAL, map.getRows(), map.getCols());
            writeTiles(out, map);
        }
    }

    /**
     * Speichert eine Battlemap mit Lighting.
     */
    public void saveBattleMap(File file, GameMap map,
                              List<LightSource> lights) throws IOException {
        try (DataOutputStream out = openForWrite(file)) {
            writeHeader(out, TYPE_BATTLE, map.getRows(), map.getCols());
            writeTiles(out, map);
            writeBackground(out, map.backgroundPath);
            writeLights(out, lights);
            writeWalls(out, map.getWalls());
            writeTokens(out, map.getTokens());
        }
    }

    // LOAD
    public static class LoadResult {
        public byte mapType;
        public MutableGameMap map;
        public List<LocationMarker> locations = new ArrayList<>();
        public List<LightSource> lights = new ArrayList<>();
        public List<WallSegment> walls = new ArrayList<>();
        public List<Token> tokens = new ArrayList<>();
    }

    /**
     * Lädt eine beliebige .shorin Datei und gibt ein LoadResult zurück.
     * Der Aufrufer prüft mapType um zu wissen was geladen wurde.
     */
    public LoadResult load(File file) throws IOException {
        try (DataInputStream in = openForRead(file)) {
            LoadResult result = new LoadResult();

            // Header validieren
            byte[] magic = new byte[4];
            in.readFully(magic);
            if (magic[0] != 'S' || magic[1] != 'H' || magic[2] != 'O' || magic[3] != 'R') {
                throw new IOException("Ungültiges Dateiformat (kein .shorin Magic)");
            }

            byte version = in.readByte();
            if (version > VERSION) {
                throw new IOException("Dateiversion " + version + " nicht unterstützt (max: " + VERSION + ")");
            }

            result.mapType = in.readByte();
            int rows = in.readInt();
            int cols = in.readInt();

            result.map = new MutableGameMap(rows, cols);
            readTiles(in, result.map, rows, cols);

            switch (result.mapType) {
                case TYPE_WORLD -> result.locations = readLocations(in);
                case TYPE_BATTLE -> {
                    result.map.backgroundPath = readBackgroundpath(in);
                    result.lights = readLights(in);
                    result.walls = readWalls(in);
                    result.tokens = readTokens(in);
                }
            }

            return result;
        }
    }

    // WRITE HELPERS
    private DataOutputStream openForWrite(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        return new DataOutputStream(new BufferedOutputStream(
                Files.newOutputStream(file.toPath()), 64 * 1024));
    }

    private void writeHeader(DataOutputStream out, byte type,
                             int rows, int cols) throws IOException {
        out.write(MAGIC);
        out.writeByte(VERSION);
        out.writeByte(type);
        out.writeInt(rows);
        out.writeInt(cols);
    }

    private void writeTiles(DataOutputStream out, GameMap map) throws IOException {
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                out.writeInt(map.getTile(r, c).flags);
            }
        }
    }

    private void writeBackground(DataOutputStream out, String backgroundPath) throws IOException {
        out.writeUTF(backgroundPath != null ? backgroundPath : "");
    }

    private void writeLocations(DataOutputStream out,
                                List<LocationMarker> locations) throws IOException {
        if (locations == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(locations.size());
        for (LocationMarker loc : locations) {
            out.writeInt(loc.row);
            out.writeInt(loc.col);
            out.writeInt(loc.tileRadius);
            out.writeUTF(loc.name != null ? loc.name : "");
            out.writeUTF(loc.linkedFile != null ? loc.linkedFile : "");
        }
    }

    private void writeLights(DataOutputStream out,
                             List<LightSource> lights) throws IOException {
        if (lights == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(lights.size());
        for (LightSource ls : lights) {
            out.writeDouble(ls.x);
            out.writeDouble(ls.y);
            out.writeInt(ls.brightTiles);
            out.writeInt(ls.dimTiles);
            out.writeFloat(ls.intensity);
        }
    }

    // READ HELPERS
    private DataInputStream openForRead(File file) throws IOException {
        return new DataInputStream(new BufferedInputStream(
                Files.newInputStream(file.toPath()), 64 * 1024));
    }

    private void readTiles(DataInputStream in, MutableGameMap map,
                           int rows, int cols) throws IOException {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map.getTile(r, c).flags = in.readInt();
            }
        }
    }

    private List<LocationMarker> readLocations(DataInputStream in) throws IOException {
        int count = in.readInt();
        List<LocationMarker> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int row = in.readInt();
            int col = in.readInt();
            int tileRadius = in.readInt();
            String name = in.readUTF();
            String linkedFile = in.readUTF();
            list.add(new LocationMarker(row, col, tileRadius, name, linkedFile));
        }
        return list;
    }

    private List<LightSource> readLights(DataInputStream in) throws IOException {
        int count = in.readInt();
        List<LightSource> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            int brightTiles = in.readInt();
            int dimTiles = in.readInt();
            float intensity = in.readFloat();
            list.add(new LightSource(x, y, brightTiles, dimTiles, intensity));
        }
        return list;
    }

    private void writeWalls(DataOutputStream out, List<WallSegment> walls) throws IOException {
        if (walls == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(walls.size());
        for (WallSegment w : walls) {
            out.writeDouble(w.x1);
            out.writeDouble(w.y1);
            out.writeDouble(w.x2);
            out.writeDouble(w.y2);
            out.writeUTF(w.type.name());
            out.writeBoolean(w.open);
        }
    }

    private List<WallSegment> readWalls(DataInputStream in) throws IOException {
        int count = in.readInt();
        List<WallSegment> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double x1 = in.readDouble();
            double y1 = in.readDouble();
            double x2 = in.readDouble();
            double y2 = in.readDouble();
            WallType type = WallType.valueOf(in.readUTF());
            boolean open = in.readBoolean();
            WallSegment seg = new WallSegment(x1, y1, x2, y2, type);
            seg.open = open;
            list.add(seg);
        }
        return list;
    }

    private String readBackgroundpath(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    private void writeTokens(DataOutputStream out, List<Token> tokens) throws IOException {
        if (tokens == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(tokens.size());
        for (Token t : tokens) {
            out.writeInt(t.row);
            out.writeInt(t.col);
            out.writeUTF(t.name != null ? t.name : "");
        }
    }

    private List<Token> readTokens(DataInputStream in) throws IOException {
        int count = in.readInt();
        List<Token> list = new ArrayList<>(count);
        List<NpcBuild> npcModules = loadNpcsfromDisk();
        for (int i = 0; i < count; i++) {
            int row = in.readInt();
            int col = in.readInt();
            String name = in.readUTF();
            for (NpcBuild build : npcModules) {
                if (name.equals(build.name)) {
                    list.add(new Token(row, col, build));
                    break;
                }
            }
        }
        return list;
    }
}