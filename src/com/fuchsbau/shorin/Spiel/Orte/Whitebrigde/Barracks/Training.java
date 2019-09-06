package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Barracks;

import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Training {

    private BorderPane pane;


    private void makePane() {

        TextFlow flow = SceneBuilder.mainFlow();

        Text a = SceneBuilder.makeText();
        a.setText("This is the trainingsroom, you can do a testfight against some soldier");
        // TODO: 06.09.2019 make testfight

        flow.getChildren().addAll(a);


        Button zurueck = SceneBuilder.makeButton();
        zurueck.setText("Back to the barracks");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.barracks.getPane())));

        HBox dritte = SceneBuilder.makeButtonrow();

        dritte.getChildren().addAll(zurueck);


        pane = SceneBuilder.buildGameScene(null, null, dritte, flow);
    }

    public BorderPane getPane() {

        makePane();
        return pane;
    }
}
