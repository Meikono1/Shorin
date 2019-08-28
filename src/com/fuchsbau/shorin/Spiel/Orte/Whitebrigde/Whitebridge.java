package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde;

import com.fuchsbau.shorin.Charakters.Dave;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Orte.Platz;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Whitebridge extends Platz {

    private Pane pane;
    private int ort = 1;
    private int whitebridgestage = 0;


    public Whitebridge(String name, String beschreibung) {
        super(name, beschreibung);

    }

    private void makePane() {

        TextFlow spieltext = SceneBuilder.mainFlow();

        StringBuilder build = new StringBuilder();

        build.append("You`re in in the city of Whitebridge, the first city outside of Sudbury. \n\n");
        if (whitebridgestage == 0) {
            build.append("This is your home town, where you grew up an orphan. Your parents died in the Great War, because of this you want nothing more than to take revenge and you joined the army.\n");
        }
        build.append("Your hut is next to the barracks. Next to the town centre is a library. You can also go into the tavern to inquire about rumors concerning the surroundings.\n");
        build.append("If you have the money you may visit the local shop.\n\n");


        Text a = SceneBuilder.makeText();

        HBox erste = SceneBuilder.makeButtonrow();

        switch ((int) Math.floor(Math.random() * 5)) {
            case 0:
                build.append("Some guards are walking around.");
                break;
            case 1:
                build.append("Your old friend Dave is walking around the town centre.");
                Button dave = SceneBuilder.makeButton();
                dave.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(new Dave(ort, Game.spieler.dave).getPane())));
                dave.setText("Greet Dave");
                erste.getChildren().add(dave);

                break;
            case 2:
                build.append("You hear some farmers arguing about a piece of meat.");
                break;
            case 3:
                build.append("A drunk man is screaming around. Shortly later some fellow guards take him away.");
                break;
            case 4:
                build.append("Nothing special is happening right now.");
                break;

        }

        a.setText(build.toString());

        HBox dritte = SceneBuilder.makeButtonrow();

        Button library = SceneBuilder.makeButton();
        library.setText("Library");
        library.setOnMouseClicked(event -> {
            Main.getStage().setScene(new Scene(new Library().getPane()));
        });

        Button shop = SceneBuilder.makeButton();
        shop.setText("Shop");
        // TODO: Make Shop

        Button entrance = SceneBuilder.makeButton();
        entrance.setText("Main Entrance");
        // TODO: Make Entrance

        Button barracks = SceneBuilder.makeButton();
        barracks.setText("Barracks");
        // TODO Make Barracks

        Button inn = SceneBuilder.makeButton();
        inn.setText("Inn");
        inn.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(new Inn().getPane())));


        dritte.getChildren().addAll(entrance, shop, inn, library, barracks);
        spieltext.getChildren().addAll(a);

        pane = SceneBuilder.buildGameScene(erste, null, dritte, spieltext);

    }


    public Pane getPane() {
        makePane();
        return pane;

    }
}
