package com.fuchsbau.shorin.Strategy.Home;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Outside implements Saveble {

    private Scene scene;

    private void makeScene(int stage) {
        TextFlow gametext = creation(stage);
        scene = new Scene(SceneBuilder.getSceneBuilder().buildBuilderScene(gametext));
    }

    private TextFlow creation(int stage) {
        TextFlow flow;

        switch (stage) {
            case 1: {
                stageoneButtons();
                flow = stageoneflow();
                break;
            }
            default:
                flow = errormessage(stage);
        }

        return flow;
    }


    private TextFlow stageoneflow() {
        TextFlow flow = new TextFlow();
        Text a = SceneBuilder.getSceneBuilder().makeText("You're a male Human who just arrived on Shorin with the goal of creating your own destiny. You traveled along the edge of the ");
        Text b = Game.getInstance().kaguyaForest.getOrtText();
        Text c = SceneBuilder.getSceneBuilder().makeText(", west of ");
        Text d = Game.getInstance().greenValley.getOrtText();
        Text e = SceneBuilder.getSceneBuilder().makeText(", and finally reached a secluded place to start your own home. What is the name of this male Human ?");

        flow.getChildren().addAll(a, b, c, d, e);
        return flow;
    }

    private void stageoneButtons() {
        Label name = SceneBuilder.getSceneBuilder().makeLabel("Name :");
        SceneBuilder.getSceneBuilder().addElement(name, 1);

        TextField eingabe = SceneBuilder.getSceneBuilder().createTextfield();
        SceneBuilder.getSceneBuilder().addElement(eingabe, 1);

        Button accept = SceneBuilder.getSceneBuilder().makeButton(1, "Accept");
        SceneBuilder.getSceneBuilder().addElement(accept, 1);

        accept.setOnMouseClicked(event -> {
            if (eingabe.getText().equals("")) {
                Game.getInstance().spieler.setName("Jan");
            } else {
                Game.getInstance().spieler.setName(eingabe.getText());
            }
            Main.getStage().setScene(getScene(2));
        });
    }

    private TextFlow errormessage(int stage) {
        TextFlow flow = new TextFlow();
        Text a = SceneBuilder.getSceneBuilder().makeText("Error in Outside; Stage: " + stage);
        flow.getChildren().add(a);
        return flow;
    }

    @Override
    public Scene getScene(int stage) {
        SceneBuilder.getSceneBuilder().resetButtonrows();
        makeScene(stage);
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
