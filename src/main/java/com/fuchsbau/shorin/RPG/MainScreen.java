package com.fuchsbau.shorin.RPG;

import com.fuchsbau.shorin.Engine.Images.BackgroundMap;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.RPG.Intro.Intro;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;

import java.util.Objects;

import static com.fuchsbau.shorin.Engine.Images.ImagePaths.SHORIN_PAPER_MAP;

public class MainScreen implements Saveble {
    private final SceneBuilder sceneBuilder = SceneBuilder.getSceneBuilder();
    private Scene scene;

    private void makeScene() {

        Image logoImg = new Image(
                Objects.requireNonNull(Main.class.getResource("/images/logo2.png")).toExternalForm(),
                120,
                0,
                true,
                false,
                true
        );

        ImageView logo = new ImageView(logoImg);

        Label versionLabel = new Label("v0.1.0");
        versionLabel.setTextFill(Paint.valueOf("#868686"));

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(10, 10, 0, 10));
        topBar.setRight(logo);
        topBar.setMouseTransparent(true);

        BorderPane bottomBar = new BorderPane();
        bottomBar.setPadding(new Insets(0, 10, 10, 10));
        BorderPane.setAlignment(versionLabel, Pos.BOTTOM_RIGHT);
        bottomBar.setRight(versionLabel);
        bottomBar.setMouseTransparent(true);

        // Buttons
        Button start = sceneBuilder.createMenuButton("Start Game");
        start.setOnAction(event -> {
            Main.getStage().setScene(new Intro().getScene());
            // Main.getStage().setScene(creation.getScene(1));
        });

        Button load = sceneBuilder.createMenuButton("Load Game");

        Button encounter = sceneBuilder.createMenuButton("Empty");
        encounter.setOnAction(event -> {
            //Main.getStage().setScene(new EncounterScreen().getScene());
        });

        Button settings = sceneBuilder.createMenuButton("Settings");
        settings.setOnAction(event -> {
            Main.getStage().setTitle("BrokenSanctuary - Options");
            Main.getStage().setScene(Game.getInstance().optionen.getScene(0));
        });

        Button rulebook = sceneBuilder.createMenuButton("Rulebook");

        Button credits = sceneBuilder.createMenuButton("Credits");

        Button quit = sceneBuilder.createMenuButton("Quit Game");
        quit.setOnAction(event -> Platform.exit());

        // Sektionen links
        VBox gameSection = new VBox(5, sceneBuilder.makeWhiteLabel("Game"), start, load, encounter);
        gameSection.setFillWidth(true);
        VBox settingsSection = new VBox(5, sceneBuilder.makeWhiteLabel("Settings"), settings, rulebook);
        settingsSection.setFillWidth(true);
        VBox infoSection = new VBox(5, sceneBuilder.makeWhiteLabel("Info"), credits);
        infoSection.setFillWidth(true);

        VBox leftMenu = new VBox(20, gameSection, settingsSection, infoSection, quit);
        leftMenu.setPadding(new Insets(20));
        leftMenu.setAlignment(Pos.TOP_LEFT);
        leftMenu.setPrefWidth(280);
        leftMenu.setBackground(GameOptions.rowHintergrundTrans40);

        // Haupt-Layout
        BorderPane pane = new BorderPane();

        pane.setLeft(leftMenu);
        pane.setBackground(Background.EMPTY);

        // Hintergrundbild
        ImageView bg = new BackgroundMap().getBackgroundImage(SHORIN_PAPER_MAP, 1.2, 0.8);

        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.8);
        bg.setEffect(adjust);

        StackPane root = new StackPane();
        root.getChildren().addAll(bg, pane, topBar, bottomBar);

        scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        Main.class.getResource("/css/main.css")
                ).toExternalForm()
        );

        Platform.runLater(ImagePreLoader::warmUpAll);
    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
