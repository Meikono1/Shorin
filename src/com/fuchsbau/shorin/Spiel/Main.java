package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import javafx.application.Application;
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
        Main.stage.setResizable(true);
        stage.setHeight(GameOptionen.height);
        stage.setWidth(GameOptionen.width);
        stage.setMinWidth(GameOptionen.width);
        stage.setMinHeight(GameOptionen.height);
        stage.setTitle("Shorin");
        stage.setScene(new Hauptbildschirm().getScene(0));
        stage.show();
    }

}
