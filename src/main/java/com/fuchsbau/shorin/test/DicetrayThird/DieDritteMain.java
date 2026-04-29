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
        trayController.attachBottomRight(root, 0.80, 220, 150, 4020, 3000);
        trayController.start();

        Button oneD20 = new Button("1x D20");
        oneD20.setOnAction(e -> trayController.throwOneD20());

        Button combo = new Button("2x D6 + 1x D8");
        combo.setOnAction(e -> trayController.throwTwoD6OneD8());

        Button standard = new Button("Standard-Set (1x D20 + 2x D6 + 1x D8)");
        standard.setOnAction(e -> trayController.throwStandardSet());

        Button oneD2 = new Button("1x D2 (Coin)");
        oneD2.setOnAction(e -> trayController.throwOneD2());

        // Volle Typen-Reihe – zum Austesten aller Shapes
        Button allTypes = new Button("Alle Typen (D2/D4/D6/D8/D10/D12/D20)");
        allTypes.setOnAction(e -> trayController.throwDice(
                "d2", 1, "d4", 1, "d6", 1, "d8", 1, "d10", 1, "d12", 1, "d20", 1
        ));

        HBox controls = new HBox(10, oneD20, combo, standard, oneD2, allTypes);
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
