package com.fuchsbau.shorin.RPG.Intro;

import com.fuchsbau.shorin.Engine.Images.BackgroundMap;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.PerformanceTimer;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.Styler.CSSLoader;
import com.fuchsbau.shorin.Engine.Styler.TextStyler;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.MainScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextFlow;

import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Images.ImagePaths.SHORIN_CLEAN_MAP;

public class Intro {
    private final Logger logger = FileLogger.getLogger();
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();
    private final String introText = """
            The world of Shorin is inhabited by multiple races | cultures and different types of dominion.
            While the world lives and advances in its own pace, you are watching over the mortal lives, going on their deeds.
            
            You can see yourself as an Spirit.. an immortal, godlike being. Watching and influencing the lives of many.
            No matter where you look, the world is large enough that a single live may not cause that big of a difference. But there is an experience waiting for you too take.
            
            Decide where you want to start.
            Either search for s poor unfortunate soul, stumble into the midriff of disaster. Only for you too take over and experience the downfall and life doing their deed.
            Or take on the body of a new vessel, reaching the shores of Shorin. Make yourself a name | Bring yourself to a exciting downfall.
            
            But don't get too attached... live is fleeting, even when yours is not.
            """;

    public Scene getScene() {
        return makeScene();
    }

    private Scene makeScene() {
        logger.info("Spieler startet neues Spiel");
        PerformanceTimer timer = new PerformanceTimer();
        timer.mark("Starte Intro");

        // Background
        ImageView bg = new BackgroundMap().getBackgroundImage(SHORIN_CLEAN_MAP, 1, 0.9);

        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.55);
        bg.setEffect(adjust);

        logger.info("Generiere Textpanel");
        // Textpanel
        Label title = SceneBuilder.createHeaderLabel("Intro");

        TextFlow lore = TextStyler.addRestyledText(sceneBuilder.mainFlow(), introText);

        ScrollPane loreScroll = sceneBuilder.createScrollPane();
        loreScroll.setContent(lore);

        VBox textBox = new VBox(12, title, loreScroll);
        textBox.setPadding(new Insets(16));
        textBox.setPrefWidth(520);
        textBox.setMaxHeight(280);

        // Halbtransparent Panel
        BackgroundFill fill = new BackgroundFill(
                Paint.valueOf("rgba(0,0,0,0.55)"),
                new CornerRadii(12),
                Insets.EMPTY
        );
        textBox.setBackground(new Background(fill));


        logger.info("Generiere Buttons");
        // Buttons
        Button startClassic = sceneBuilder.createMenuButton("Start: Free Character");
        startClassic.setOnAction(e -> {
            Main.getStage().setTitle("Shorin");
            //Main.getStage().setScene(PlaceholderLocationSelector.getScene());
        });

        Button startNomad = sceneBuilder.createMenuButton("Start: Scenario Selector");
        startNomad.setOnAction(e -> {
            Main.getStage().setTitle("Shorin");
            WorldStartLocationSelector selector = new WorldStartLocationSelector();
            Main.getStage().setScene(selector.getScene(0));
            selector.recalculatePositions();
        });
        System.getProperty("java.io.tmpdir");

        Button back = sceneBuilder.createMenuButton("Back to Menu");
        back.setOnAction(e -> {
            Main.getStage().setTitle("Shorin");
            Main.getStage().setScene(new MainScreen().getScene(0));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(10, startClassic, startNomad, spacer, back);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, textBox, buttons);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.BOTTOM_LEFT);

        BorderPane overlay = new BorderPane();
        overlay.setBottom(card);

        StackPane root = new StackPane(bg, overlay);

        Scene s = new Scene(root, GameOptions.width, GameOptions.height);
        String cssUrl = CSSLoader.resolveUserOrBackupCSS();
        if (cssUrl != null) {
            s.getStylesheets().add(cssUrl);
        } else {
            logger.warning("CSS not found: css/main.css");
        }

        timer.mark("Ende Intro");
        logger.info(timer.report());
        return s;
    }
}
