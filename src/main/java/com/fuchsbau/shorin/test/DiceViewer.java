package com.fuchsbau.shorin.test;

import com.fuchsbau.shorin.Engine.Dice.DiceType;
import com.fuchsbau.shorin.Engine.Dice.DieMeshFactory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DiceViewer extends Application {

    @Override
    public void start(Stage stage) {
        Node die = DieMeshFactory.createDie(DiceType.D12, 120);

        Group dieGroup = new Group(die);

        Rotate rx = new Rotate(-20, Rotate.X_AXIS);
        Rotate ry = new Rotate(0, Rotate.Y_AXIS);
        dieGroup.getTransforms().addAll(rx, ry);

        Timeline spin = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(ry.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(8), new KeyValue(ry.angleProperty(), 360))
        );
        spin.setCycleCount(Animation.INDEFINITE);
        spin.play();

        AmbientLight ambient = new AmbientLight(Color.color(0.55, 0.55, 0.55));

        PointLight light1 = new PointLight(Color.WHITE);
        light1.setTranslateX(-400);
        light1.setTranslateY(-200);
        light1.setTranslateZ(-700);

        PointLight light2 = new PointLight(Color.WHITE);
        light2.setTranslateX(300);
        light2.setTranslateY(150);
        light2.setTranslateZ(-500);

        Group root3d = new Group(dieGroup, ambient, light1, light2);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-700);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        SubScene subScene = new SubScene(root3d, 900, 700, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(18, 18, 24));
        subScene.setCamera(camera);

        StackPane root = new StackPane(subScene);
        Scene scene = new Scene(root, 900, 700, true);

        subScene.widthProperty().bind(root.widthProperty());
        subScene.heightProperty().bind(root.heightProperty());

        stage.setScene(scene);
        stage.setTitle("Dice Viewer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
