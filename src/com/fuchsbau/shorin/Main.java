package com.fuchsbau.shorin;

import com.fuchsbau.shorin.Spiel.Game;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    private static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }



    public static Stage getStage() {

        Game.getInstance().update();
        return stage;
    }

    @Override
    public void start(Stage stage) {

        Main.stage = stage;
        Main.stage.setResizable(false);
        stage.setTitle("Shorin");
        stage.setScene(new Scene(new Hauptbildschirm().getPane()));
        stage.show();
    }

}
