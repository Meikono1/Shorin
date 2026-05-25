package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Engine.FXML.ShorinXMLLoader;
import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Engine.Options.StyleOptions;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.RPG.Game;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Logger.FileLogger.logCauses;
import static com.fuchsbau.shorin.Logger.FileLogger.logStack;

public class Main extends Application {
    private static final Logger logger = FileLogger.getLogger();

    private static Stage stage;

    public static void main(String[] args) {
        logger.info("[BOOTED]");

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.severe("[UNCAUGHT] Thread = " + thread.getName());
            logger.severe("[EXCEPTION] = " + throwable);
            logCauses(throwable);
            logStack(throwable.getStackTrace(), 5);
        });

        try {
            launch(args);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "[BOOT] launch failed", e);
            logCauses(e);
            throw e;
        }
    }

    public static Stage getStage() {
        Game.getInstance().update();
        return stage;
    }

    @Override
    public void start(Stage stage) {
        logger.info("Guten Morgen!");
        Platform.runLater(ImagePreLoader::warmUpAll);

        CSSLoader.loadFonts();
        CSSLoader.ensureExists(StyleOptions.buildCss());

        FXMLLoader fxmlLoader = ShorinXMLLoader.loader("Intro.fxml");
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 320, 240);
        } catch (IOException e) {
            logger.severe("Fehler beim laden = " + e);
            logCauses(e);
        }
        try {
            stage.getIcons().add(
                    ImagePreLoader.getCached(ImagePaths.SHORIN_LOGO)
            );
        } catch (Exception e) {
            logger.severe("Fehler beim hinzufügen vom Icons = " + e);
            logCauses(e);
        }

        stage.setHeight(GameOptions.height);
        stage.setWidth(GameOptions.width);
        stage.setTitle("Shorin");
        stage.setScene(scene);
        Main.stage = stage;
        stage.show();
    }


    @Override
    public void stop() throws Exception {
        FileLogger.closeLogger();
        super.stop();
    }
}
