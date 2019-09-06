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

public class Joshua {
    private BorderPane pane;
    // TODO: 02.09.2019 make yoshua


    private void makePane() {

        TextFlow text = SceneBuilder.mainFlow();

        Text a = SceneBuilder.makeText();
        a.setText("Hey, my Boy. How you doing ? \nI hope this mission is not to hard for you. I mean its the first time that you make such a long journey.");

        text.getChildren().addAll(a);


        HBox dritte = SceneBuilder.makeButtonrow();

        Button zurueck = SceneBuilder.makeButton();
        zurueck.setText("Back to the barracks");
        zurueck.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(Game.getInstance().whitebridge.barracks.getPane())));

        dritte.getChildren().addAll(zurueck);

        pane = SceneBuilder.buildGameScene(null, null, dritte, text);


    }

    public BorderPane getPane() {
        makePane();

        return pane;
    }

}
