package com.fuchsbau.shorin.Engine.Styler;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import javafx.scene.Scene;
import javafx.scene.text.Font;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class UserCSSHandler {
    private static final String DIRECTORY_NAME = "Styling";
    private static final String CSS_FILE_NAME = "user-style.css";

    private static String appliedUrl;

    private static File getDirectory() {
        return new File(DIRECTORY_NAME);
    }

    private static File getCssFile() {
        return new File(getDirectory(), CSS_FILE_NAME);
    }

    private static Path getCssPath() {
        return getCssFile().toPath();
    }

    public static void loadFonts(){
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
                FileLogger.getLogger().warning("Konnte Styling-Ordner nicht erstellen");
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
            FileLogger.getLogger().warning("Konnte user css nicht erstellen: " + e.getMessage());
        }
    }

    public static void saveAndApply(Scene scene, String css) {
        try {
            File dir = getDirectory();
            if (!dir.exists() && !dir.mkdir()) {
                FileLogger.getLogger().warning("Konnte Styling-Ordner nicht erstellen");
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
            FileLogger.getLogger().warning("Konnte user css nicht bearbeiten: " + e.getMessage());
        }
    }

    public static void apply(Scene scene) {
        try {
            Path cssPath = getCssPath();

            if (!Files.exists(cssPath)) {
                FileLogger.getLogger().warning("User CSS fehlt: " + cssPath);
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
            FileLogger.getLogger().warning("Konnte user css nicht hinzufügen: " + e.getMessage());
        }
    }
}
