package com.fuchsbau.shorin.Strategy.Home;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import com.fuchsbau.shorin.Strategy.Variables.StrategyInstance;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();
        Text a = StrategyInstance.getInstance().getForestText();
        Text b = Game.getInstance().kaguyaForest.getOrtText();
        Text c = SceneBuilder.getSceneBuilder().makeText(", west of ");
        Text d = Game.getInstance().greenValley.getOrtText();
        Text e = SceneBuilder.getSceneBuilder().makeText(", and finally reached a secluded place to start your own home. What is the name of this male Human ?");

        flow.getChildren().addAll(a, b, c, d, e);
        return flow;
    }

    private void stageoneButtons() {
        Button name = SceneBuilder.getSceneBuilder().makeButton(1,"Wooding");
        SceneBuilder.getSceneBuilder().addElement(name, 1);

        name.setOnMouseClicked(event -> {
            StrategyInstance.getInstance().logging(2);
            Main.getStage().setScene(getScene(1));
        });
    }

    private TextFlow errormessage(int stage) {
        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();
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
