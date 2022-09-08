package com.fuchsbau.shorin.Spiel.Places.Whitebrigde;

import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.Places.Platz;
import com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Barracks.Barracks;
import com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Shop.Shop;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Whitebridge extends Platz implements Saveble {

    public Barracks barracks = new Barracks();
    private final Inn inn = new Inn();
    private final Library library = new Library();
    private final Shop shop = new Shop();
    private final Entrance entrance = new Entrance();
    private Scene scene;
    private final int ort = 1;
    private final int whitebridgestage = 0;
    /*    Stage 0 = Anfang. => geschichte wird gezeigt.
    /
     */


    public Whitebridge(String name, String beschreibung) {
        super(name, beschreibung);
    }

    private void makeScene() {

        TextFlow flow = SceneBuilder.mainFlow();

        Text a = SceneBuilder.makeText();
        a.setText("You`re in the city of ");

        Text b = this.getOrtText();

        Text c = SceneBuilder.makeText();
        c.setText(", the first city outside of ");

        Text d = Game.getInstance().sudbury.getOrtText();

        Text e = SceneBuilder.makeText();
        e.setText(". \n\n");

        flow.getChildren().addAll(a, b, c, d, e);

        if (whitebridgestage == 0) {
            Text f = SceneBuilder.makeText();
            f.setText("This is your home town, where you grew up an orphan. Your parents died in the ");

            Text g = Game.getInstance().greatWar.getName();

            Text h = SceneBuilder.makeText();
            h.setText(", because of this you want nothing more than to take revenge and you joined the army.\n");

            flow.getChildren().addAll(f, g, h);
        }

        Text i = SceneBuilder.makeText();
        i.setText("Next to the town centre is a library. You can also go into the tavern to inquire about rumours concerning the surroundings.\n" +
                "If you have the money you may visit the local shop.\n\n");


        HBox erste = SceneBuilder.makeButtonrow();
        Text h = SceneBuilder.makeText();

        switch ((int) Math.floor(Math.random() * 5)) {
            case 0:
                h.setText("Some guards are walking around.");
                break;
            case 1:
                h.setText("Your old friend Dave is walking around the town centre.");

                Button dave = SceneBuilder.makeButton(erste);
                dave.setOnMouseClicked(event -> Main.getStage().setScene(Game.getInstance().dave.getScene(ort)));
                dave.setText("Greet Dave");
                erste.getChildren().add(dave);
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


        HBox dritte = SceneBuilder.makeButtonrow();

        Button library = SceneBuilder.makeButton(dritte);
        library.setText("Library");
        library.setOnMouseClicked(event -> Main.getStage().setScene(this.library.getScene(0)));

        Button shop = SceneBuilder.makeButton(dritte);
        shop.setText("Shop");
        shop.setOnMouseClicked(event -> Main.getStage().setScene(this.shop.getScene(0)));

        Button entrance = SceneBuilder.makeButton(dritte);
        entrance.setText("Main Entrance");
        entrance.setOnMouseClicked(event -> Main.getStage().setScene(this.entrance.getScene(0)));

        Button barracks = SceneBuilder.makeButton(dritte);
        barracks.setText("Barracks");
        barracks.setOnMouseClicked(event -> Main.getStage().setScene(this.barracks.getScene(0)));

        Button inn = SceneBuilder.makeButton(dritte);
        inn.setText("Inn");
        inn.setOnMouseClicked(event -> Main.getStage().setScene(this.inn.getScene(0)));


        dritte.getChildren().addAll(entrance, shop, inn, library, barracks);

        scene = new Scene(SceneBuilder.buildGameScene(erste, null, dritte, flow));

    }

    @Override
    public Scene getScene(int stage) {
        makeScene();
        Game.getInstance().spieler.setAktuell(this, stage);
        return scene;
    }

    @Override
    public void reset() {
        this.scene = null;
    }
}
