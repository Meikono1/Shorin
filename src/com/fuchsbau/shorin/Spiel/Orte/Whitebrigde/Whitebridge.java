package com.fuchsbau.shorin.Spiel.Orte.Whitebrigde;

import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.SceneBuilder;
import com.fuchsbau.shorin.Spiel.Orte.Platz;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Whitebridge extends Platz {

    private Pane pane;

    public Whitebridge(String name, String beschreibung) {
        super(name, beschreibung);

    }

    private void makePane() {

        TextFlow spieltext = SceneBuilder.mainFlow();

        StringBuilder build = new StringBuilder();

        build.append("You`re in in the City of Whitebridge, the first City outside Sudbury. \n\n");
        build.append("This is your home town, where you lived only knowing the Army");
        build.append("Your House is next to the Barracks. Next to the Towncenter is a Library. You can also go into the Tavern for some rumors.\n");
        build.append("If you have the money you may visit the Local Shop\n\n");


        Text a = SceneBuilder.makeText();

        HBox erste = SceneBuilder.makeButtonrow();

        switch ((int) Math.floor(Math.random() * 5)) {
            case 0:
                build.append("Some Guards are Walking around");
                break;
            case 1:
                build.append("Your old Friend Dave is walking around the towncenter.");
                Button dave = SceneBuilder.makeButton();
                dave.setText("Greet Dave");
                erste.getChildren().add(dave);
                // TODO: 22.08.2019  Make Dave
                break;
            case 2:
                build.append("You Here some Farmers argue about a piece of Meat");
                break;
            case 3:
                build.append("A Drunken man is Screaming around. Shortly later some Guards take him away.");
                break;
            case 4:
                build.append("Nothing Special is happening right now");
                break;

        }

        a.setText(build.toString());

        HBox dritte = SceneBuilder.makeButtonrow();

        Button library = SceneBuilder.makeButton();
        library.setText("Library");
        // TODO: Make Library

        Button shop = SceneBuilder.makeButton();
        shop.setText("Shop");
        // TODO: 22.08.2019 Make Shop

        Button entrance = SceneBuilder.makeButton();
        entrance.setText("Main Entrance");
        // TODO: 22.08.2019 Make Entrance
        
        Button barracks = SceneBuilder.makeButton();
        barracks.setText("Barracks");
        // TODO Make Barracks

        Button inn = SceneBuilder.makeButton();
        inn.setText("Inn");
        inn.setOnMouseClicked(event -> Main.getStage().setScene(new Scene(getPane())));
        //TODO make Inn

        dritte.getChildren().addAll(entrance, shop, inn, library, barracks);
        spieltext.getChildren().addAll(a);

        pane = SceneBuilder.buildGameScene(erste, null, dritte, spieltext);

    }


    public Pane getPane() {
        makePane();
        return pane;

    }
}
