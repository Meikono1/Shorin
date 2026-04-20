package com.fuchsbau.shorin.test;

import com.fuchsbau.shorin.Engine.Dice.DiceDefinitions;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DicePhysicsTestApp extends Application {

    @Override
    public void start(Stage stage) {
        DicePhysicsTestPane pane = new DicePhysicsTestPane(DiceDefinitions.createAll());

        Scene scene = new Scene((Parent) pane.build(), 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Dice Physics Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}