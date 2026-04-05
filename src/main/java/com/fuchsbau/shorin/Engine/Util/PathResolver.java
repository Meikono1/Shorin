package com.fuchsbau.shorin.Engine.Util;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PathResolver {

    private static final Logger logger = FileLogger.getLogger();


    private static final Path INSTALL_DIR = resolveInstallDir();

    private PathResolver() {}

    private static Path resolveInstallDir() {
        // DEV: src/main/resources
        Path resources = Paths.get("src/main/resources").toAbsolutePath();
        if (Files.isDirectory(resources)) {
            logger.info("PathResolver DEV-Modus: " + resources);
            return resources;
        }

        // SHIP: neben EXE/JAR
        Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        logger.info("PathResolver SHIP-Modus (user.dir): " + wd);
        return wd;
    }

    public static URL resolve(String relativePath) {
        // SHIP
        Path external = INSTALL_DIR.resolve(relativePath);
        if (Files.exists(external)) {
            try {
                logger.fine("Extern gefunden: " + external);
                return external.toUri().toURL();
            } catch (Exception e) {
                logger.warning("URL Konvertierung fehlgeschlagen: " + external);
            }
        }

        // DEV
        URL cp = Main.class.getResource("/" + relativePath);
        if (cp != null) {
            logger.fine("Classpath gefunden: " + relativePath);
            return cp;
        }

        logger.warning("Nicht gefunden: " + relativePath);
        return null;
    }

    public static String resolveString(String relativePath) {
        URL url = resolve(relativePath);
        return url != null ? url.toExternalForm() : null;
    }

    public static Path resolveWritable(String relativePath) {
        return INSTALL_DIR.resolve(relativePath);
    }
}