package com.fuchsbau.shorin.Engine.Images;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum ImagePaths {
    MISSING("images/missing.png"),
    SHORIN_PAPER_MAP("images/welcomePage/ShorinMap.png"),
    SHORIN_CLEAN_MAP("images/world/CleanWorld.png"),
    SHORIN_LOGO_128("images/logo128.png", ImageConfig.ratio(128, 128)),
    SHORIN_LOGO("images/logo.png"),
    MAP_TOWER("images/MapIcons/Tower.png", ImageConfig.fixed(80,80)),
    MAP_FLAG("images/MapIcons/Flag.png", ImageConfig.fixed(80,80)),
    MAP_SNAKE("images/MapIcons/Snake.png", ImageConfig.fixed(80,80)),
    MAP_STAR("images/MapIcons/FourStar.png", ImageConfig.fixed(80,80)),
    MAP_CAVE("images/MapIcons/Cave.png", ImageConfig.fixed(80,80)),
    MAP_HUT("images/MapIcons/Hut.png", ImageConfig.fixed(80,80)),
    MAP_OVERLAY_CULTURES("images/world/WorldOverlayCultures.png");

    private final String path;
    private ImageConfig config;

    ImagePaths(String path) {
        this.path = path;
    }

    ImagePaths(String path, ImageConfig config) {
        this.path = path;
        this.config = config;
    }

    public static Path baseDir() {
        Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(wd.resolve("images"))) {
            return wd;
        }

        try {
            Path p = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(p) ? p : p.getParent();
        } catch (Exception e) {
            FileLogger.getLogger().warning("Bild nicht gefunden");
            return wd;
        }
    }

    public String resolveUrlOrNull() {
        Path external = baseDir().resolve(path);
        if (Files.exists(external)) {
            return external.toUri().toString();
        }

        var res = Main.class.getResource(classpath());
        return res != null ? res.toExternalForm() : null;
    }

    public ImageConfig getConfig() {
        return config;
    }

    public String relative() {
        return path;
    }

    public String classpath() {
        return "/" + path;
    }
}
