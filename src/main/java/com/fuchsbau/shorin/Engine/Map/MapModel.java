package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.Map.Core.MapRenderer;
import com.fuchsbau.shorin.Engine.Map.Core.MapSaverLoader;
import com.fuchsbau.shorin.Engine.Map.Core.Lighting.LightingSystem;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.MutableGameMap;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Engine.Logger.FileLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Zentrale Map-Instanz. Genau einmal pro Kontext (PlayerScreen, EncounterModule, BattleMapModule).
 * Kein Modul baut eigene GameMap / LightingSystem / MapRenderer mehr.
 * <p>
 * Dirty-Flags steuern welche Layer neu gerendert werden müssen.
 * Setter markieren immer das kleinste nötige Flag — nie alles auf einmal.
 */
public class MapModel {

    private static final Logger logger = FileLogger.getLogger();

    // Kern
    private final MutableGameMap gameMap;
    private final LightingSystem lightingSystem;
    private final MapRenderer renderer;

    // Dirty-Flags
    private boolean mapDirty = true;  // Grid, Wände, Background
    private boolean lightDirty = true;  // LightMask, sunDeg, LightSources
    private boolean tokensDirty = true; // Token-Positionen, Selection

    public MapModel() {
        gameMap = new MutableGameMap();
        lightingSystem = new LightingSystem();
        renderer = new MapRenderer(gameMap, lightingSystem);
        logger.info("MapModel init | GameMap " + gameMap.getRows() + "x" + gameMap.getCols());
    }

    public MutableGameMap getGameMap() {
        return gameMap;
    }

    public LightingSystem getLightingSystem() {
        return lightingSystem;
    }

    public MapRenderer getRenderer() {
        return renderer;
    }

    // Dirty-Flags setzen
    public void markMapDirty() {
        mapDirty = true;
        logger.fine("mapDirty");
    }

    public void markLightDirty() {
        lightDirty = true;
        logger.fine("lightDirty");
    }

    public void markTokensDirty() {
        tokensDirty = true;
        logger.fine("tokensDirty");
    }

    public void tick() {
        if (mapDirty) {
            renderer.renderBattlemap();
            mapDirty = false;
            logger.finest("renderBattlemap");
        }
        if (lightDirty) {
            lightingSystem.recomputeLightmapAll(gameMap);
            renderer.renderLightLayer();
            lightDirty = false;
            logger.finest("renderLightLayer");
        }
        if (tokensDirty) {
            renderer.renderStrategyMap();
            tokensDirty = false;
            logger.finest("renderStrategyMap");
        }
    }

    // Karte laden — markiert alle Flags dirty
    public void loadMap(String fileName) throws IOException {
        logger.info("Lade Karte: " + fileName);
        Path file = PathResolver.resolveWritable( fileName);
        MapSaverLoader.LoadResult result = new MapSaverLoader().load(file.toFile());
        if (result == null) {
            logger.warning("Karte nicht gefunden: " + fileName);
            return;
        }

        gameMap.applyLoadResult(result);
        renderer.loadBackground(gameMap.backgroundPath);
        lightingSystem.recomputeLightmapAll(gameMap);

        markMapDirty();
        markLightDirty();
        markTokensDirty();

        logger.info("Karte geladen: " + fileName
                + " | " + result.map.getRows() + "x" + result.map.getCols()
                + " | Tokens: " + result.tokens.size()
                + " | Lichter: " + result.lights.size()
                + " | Wände: " + result.walls.size());
    }

    // Zoom setzen — Pan/Zoom markiert nur Map + Light dirty, nicht Tokens
    public void setZoom(double zoom) {
        renderer.setZoom(zoom);
        markMapDirty();
        markLightDirty();
        logger.fine("zoom → " + zoom);
    }

    // sunDeg geändert (Tag/Nacht-Tick von GameClock) → nur Light dirty
    public void onSunChanged() {
        markLightDirty();
        logger.fine("sunDeg geändert → lightDirty");
    }
}