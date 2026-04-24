package com.fuchsbau.shorin.test;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DicePhysicsTestApp extends Application {

    @Override
    public void start(Stage stage) {
        DicePhysicsRealtimeTestPane pane = new DicePhysicsRealtimeTestPane();

        Scene scene = new Scene((Parent) pane.build(), 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Dice Physics Realtime Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}