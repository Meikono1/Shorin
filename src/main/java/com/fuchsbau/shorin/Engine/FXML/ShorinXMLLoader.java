package com.fuchsbau.shorin.Engine.FXML;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ShorinXMLLoader {
    private static final Logger logger = FileLogger.getLogger();

    private ShorinXMLLoader() {
    }

    public static Path baseDir() {
        Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(wd.resolve("fxml"))) return wd;

        try {
            Path p = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(p) ? p : p.getParent();
        } catch (Exception e) {
            return wd;
        }
    }

    public static URL resolve(String name) {
        Path ext = baseDir().resolve("fxml").resolve(name);

        if (Files.exists(ext)) {
            try {
                return ext.toUri().toURL();
            } catch (Exception ignored) {
            }
        }

        return Main.class.getResource("/fxml/" + name);
    }

    public static FXMLLoader loader(String name) {
        URL url = resolve(name);
        if (url == null) {
            logger.severe("FXML not found: " + name);
        }
        return new FXMLLoader(url);
    }
}
