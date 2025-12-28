package com.fuchsbau.shorin.Engine.Styler;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class CSSLoader {
    private static final Logger logger = FileLogger.getLogger();

    private CSSLoader() {
    }

    public static Path baseDir() {
        Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(wd.resolve("css"))) return wd;

        try {
            Path p = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(p) ? p : p.getParent();
        } catch (Exception e) {
            return wd;
        }
    }

    public static String resolve(String name) {
        Path ext = baseDir().resolve(name);

        if (Files.exists(ext)) {
            try {
                return ext.toUri().toString();
            } catch (Exception ignored) {
            }
        }
        logger.info("Datei existiert nicht: " + ext);

        URL cp = Main.class.getResource("/css/" + name);
        return cp != null ? cp.toExternalForm() : null;
    }
}
