package com.fuchsbau.shorin.RPG.Places.Whitebrigde;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.RPG.Places.Place;
import com.fuchsbau.shorin.RPG.Places.Whitebrigde.Shop.Shop;
import com.fuchsbau.shorin.Engine.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Whitebridge extends Place implements Saveble {

    private final Inn inn = new Inn("", "");
    private final Library library = new Library("", "");
    private final Shop shop = new Shop("", "");
    private final Entrance entrance = new Entrance("", "");
    private Scene scene;
    private final int ort = 1;
    private final int whitebridgestage = 0;
    /*    Stage 0 = Anfang. => geschichte wird gezeigt.
    /
     */

    public Whitebridge(String name, String beschreibung) {
        super(name, beschreibung);
        addSubPlace(inn);
        addSubPlace(library);
        addSubPlace(shop);
        addSubPlace(entrance);
    }

    private void makeScene() {
        TextFlow flow = SceneBuilder.getSceneBuilder().mainFlow();

        Text a = SceneBuilder.getSceneBuilder().makeText("You`re in the city of ");
        Text b = this.getOrtText();
        Text c = SceneBuilder.getSceneBuilder().makeText(", the first city outside of ");
        Text e = SceneBuilder.getSceneBuilder().makeText(". \n\n");

        flow.getChildren().addAll(a, b, c, e);

        if (whitebridgestage == 0) {
            Text f = SceneBuilder.getSceneBuilder().makeText("This is your home town, where you grew up an orphan. Your parents died in the ");
            Text h = SceneBuilder.getSceneBuilder().makeText(", because of this you want nothing more than to take revenge and you joined the army.\n");

            flow.getChildren().addAll(f, h);
        }

        Text i = SceneBuilder.getSceneBuilder().makeText(
                "Next to the town centre is a library. You can also go into the tavern to inquire about rumours concerning the surroundings.\n" +
                        "If you have the money you may visit the local shop.\n\n");

        Text h = SceneBuilder.getSceneBuilder().makeText();

        switch ((int) Math.floor(Math.random() * 5)) {
            case 0:
                h.setText("Some guards are walking around.");
                break;
            case 1:
                h.setText("Your old friend Dave is walking around the town centre.");

                Button dave = SceneBuilder.getSceneBuilder().makeButton(1, "Greet Dave");
                SceneBuilder.getSceneBuilder().addButton(dave, 1);
                break;
            case 2:
                h.setText("You hear some farmers arguing about a piece of meat.");
                break;
            case 3:
                h.setText("A drunk man is screaming around. Shortly later some fellow guards take him away.");
                break;
            case 4:
                h.setText("Nothing special is happening right now.");
                break;
        }

        Button library = SceneBuilder.getSceneBuilder().makeButton(3, "Library");
        library.setOnMouseClicked(event -> Main.getStage().setScene(this.library.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(library, 3);

        Button shop = SceneBuilder.getSceneBuilder().makeButton(3, "Shop");
        shop.setOnMouseClicked(event -> Main.getStage().setScene(this.shop.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(shop, 3);

        Button entrance = SceneBuilder.getSceneBuilder().makeButton(3, "Main Entrance");
        entrance.setOnMouseClicked(event -> Main.getStage().setScene(this.entrance.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(entrance, 3);

        Button barracks = SceneBuilder.getSceneBuilder().makeButton(3, "Barracks");
        SceneBuilder.getSceneBuilder().addButton(barracks, 3);

        Button inn = SceneBuilder.getSceneBuilder().makeButton(3, "Inn");
        inn.setOnMouseClicked(event -> Main.getStage().setScene(this.inn.getScene(0)));
        SceneBuilder.getSceneBuilder().addButton(inn, 3);

        scene = new Scene(SceneBuilder.getSceneBuilder().buildGameScene(flow));
    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setCurrentScene(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
