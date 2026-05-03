package com.fuchsbau.shorin.Engine.Styler;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import javafx.scene.Scene;
import javafx.scene.text.Font;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

public class CSSLoader {
    private static final Logger logger = FileLogger.getLogger();

    private static String appliedUrl;
    private static final String DIRECTORY_NAME = "user";
    private static final String CSS_FILE_NAME = "user-style.css";

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

    private static String resolve(String name) {
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

    public static String resolveUserOrBackupCSS() {
        String cssString = resolve(getUsserCSSString());
        if (cssString != null) {
            return cssString;
        } else return resolve("css/main.css");
    }

    public static void loadFonts() {
        Font.loadFont(
                Main.class.getResourceAsStream("/fonts/Eczar/Eczar-Regular.ttf"),
                16
        );
        Font.loadFont(
                Main.class.getResourceAsStream("/fonts/Eczar/Eczar-Bold.ttf"),
                16
        );
    }

    public static void ensureExists(String defaultCss) {
        try {
            File dir = getDirectory();
            if (!dir.exists() && !dir.mkdir()) {
                logger.warning("Konnte Styling-Ordner nicht erstellen");
                return;
            }

            Path cssPath = getCssPath();
            if (!Files.exists(cssPath)) {
                Files.writeString(
                        cssPath,
                        defaultCss,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE
                );
            }
        } catch (Exception e) {
            logger.warning("Konnte user css nicht erstellen: " + e.getMessage());
        }
    }

    public static void saveAndApply(Scene scene, String css) {
        try {
            File dir = getDirectory();
            if (!dir.exists() && !dir.mkdir()) {
                logger.warning("Konnte Styling-Ordner nicht erstellen");
                return;
            }

            Path cssPath = getCssPath();
            Files.writeString(
                    cssPath,
                    css,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            apply(scene);

        } catch (Exception e) {
            logger.warning("Konnte user css nicht bearbeiten: " + e.getMessage());
        }
    }

    public static void apply(Scene scene) {
        try {
            Path cssPath = getCssPath();

            if (!Files.exists(cssPath)) {
                logger.warning("User CSS fehlt: " + cssPath);
                return;
            }

            String url = cssPath.toUri() + "?v=" + System.nanoTime();

            if (appliedUrl != null) {
                scene.getStylesheets().remove(appliedUrl);
            }

            scene.getStylesheets().add(url);
            appliedUrl = url;

            scene.getRoot().applyCss();
            scene.getRoot().requestLayout();

        } catch (Exception e) {
            logger.warning("Konnte user css nicht hinzufügen: " + e.getMessage());
        }
    }

    private static String getUsserCSSString() {
        return DIRECTORY_NAME + "/" + CSS_FILE_NAME;
    }

    private static Path getCssPath() {
        return getCssFile().toPath();
    }

    private static File getDirectory() {
        return new File(DIRECTORY_NAME);
    }

    private static File getCssFile() {
        return new File(getDirectory(), CSS_FILE_NAME);
    }
}
