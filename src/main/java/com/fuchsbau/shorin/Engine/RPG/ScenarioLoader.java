package com.fuchsbau.shorin.Engine.RPG;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class ScenarioLoader {
    private final static Logger logger = FileLogger.getLogger();
    private static final ObjectMapper M = new ObjectMapper();

    public static Path baseDir() {
        Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(wd.resolve("Gameconfig/Scenarios"))) return wd;

        try {
            Path p = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(p) ? p : p.getParent();
        } catch (Exception e) {
            return wd;
        }
    }

    public static URL resolve(String name) {
        Path ext = baseDir().resolve("Gameconfig/Scenarios").resolve(name);

        if (Files.exists(ext)) {
            try {
                return ext.toUri().toURL();
            } catch (Exception ignored) {
            }
        }

        return Main.class.getResource("/Gameconfig/Scenarios/" + name);
    }

    public static List<ScenarioDefinition> load(URL url) {
        try (InputStream in = url.openStream()) {
            return M.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load scenarios from URL: " + url, e);
        }
    }
}
