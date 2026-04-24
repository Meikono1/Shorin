package com.fuchsbau.shorin.test.DicetrayThird;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DieDritteMain extends Application {

    @Override
    public void start(Stage stage) {
        AnchorPane root = new AnchorPane();

        DiceTrayController trayController = new DiceTrayController();
        trayController.attachBottomRight(root, 0.20, 220, 150, 420, 300);
        trayController.start();

        Button oneDie = new Button("1x D6");
        oneDie.setOnAction(e -> trayController.throwOneD6());

        Button twoDice = new Button("2x D6");
        twoDice.setOnAction(e -> trayController.throwTwoD6());

        HBox controls = new HBox(10, oneDie, twoDice);
        controls.setStyle("-fx-padding: 12;");

        AnchorPane.setTopAnchor(controls, 10.0);
        AnchorPane.setLeftAnchor(controls, 10.0);

        root.getChildren().add(controls);

        Scene scene = new Scene(root, 1200, 800, true);
        stage.setTitle("DiceTray Controller Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
