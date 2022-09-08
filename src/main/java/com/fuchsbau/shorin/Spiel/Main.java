package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Optionen.GameOption;
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
        stage.setHeight(GameOption.height);
        stage.setWidth(GameOption.width);
        stage.setTitle("Shorin");
        stage.setScene(new Mainscreen().getScene(0));
        stage.show();
    }

}
