package com.fuchsbau.shorin.RPG.Places.Whitebrigde.Barracks;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class YourRoom implements Saveble {

    private Scene scene;
    /***
     *
     * @param stage
     * 0 = Startscreen
     * 1 = Sit on Bed
     */
    private void makeScene(int stage) {
        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();

        if (stage == 0) {
            Text a = SceneBuilder.getSceneBuilder().makeText("You're in one of the many sleeping quarters inside the Barracks of ");
            Text b = Game.getInstance().whitebridge.getOrtText();
            Text c = SceneBuilder.getSceneBuilder().makeText(".\n\nIn this room are four bunk beds with a stone table in front of every bed. They are very heavy tables and you can only put a bread or yufka on it. \nThere are also some racks for clothes, armor and weapons right at the entrance and a tried rag just before the entrance. \nThe upper bunk bed in the left corner is yours.");

            flow.getChildren().addAll(a, b, c);
        } else if (stage == 1) {

            Text a = SceneBuilder.getSceneBuilder().makeText();//@Todo check text
            a.setText("You lived in this room since you joined the army.\n\nWhen you joined the ");
            Text b = Game.getInstance().greysmanace.getName();
            Text c = SceneBuilder.getSceneBuilder().makeText(", you were assigned to this bed. Your commander is ");
            Text ca = Game.getInstance().joshua.getText();
            Text d = SceneBuilder.getSceneBuilder().makeText(", your stepfather.\nYou dont know what happened to your old home but its probably in possession of another family working and destroying memories.\nMaybe you should just forget everything about your past, right now you have a mission to fulfill.");

            flow.getChildren().addAll(a, b, c, ca, d);
        }

        if (stage == 0) {
            Button bed = SceneBuilder.getSceneBuilder().makeButton(1, "Sit on your old bed");
            bed.setOnMouseClicked(event -> Main.getStage().setScene(getScene(1)));
            SceneBuilder.getSceneBuilder().addButton(bed, 1);
        }

        Button back = SceneBuilder.getSceneBuilder().makeButton(3, "Back to the Barracks");
        back.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().whitebridge.barracks.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(back, 3);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(flow));
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
