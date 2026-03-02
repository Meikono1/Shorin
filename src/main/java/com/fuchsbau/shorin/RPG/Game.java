package com.fuchsbau.shorin.RPG;

import com.fuchsbau.shorin.Characters.Player;
import com.fuchsbau.shorin.Items.Inventory;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.RPG.Places.Humanic.Sudbury.Sudbury;
import com.fuchsbau.shorin.RPG.Places.Place;
import com.fuchsbau.shorin.RPG.Places.Whitebrigde.Whitebridge;

public class Game {
    private static final Game ourInstance = new Game();
    public GameOptions optionen = new GameOptions();
    public Inventory inventory = new Inventory();
    public Player spieler = new Player();
    public Whitebridge whitebridge = new Whitebridge("Whitebridge", "The City of Whitebridge is located near the Capital Sudbury and the city where you are born. This City contains a Library, Barracks and an Inn.");
    public Place unbridledland = new Place("Unbridled Land", "Land of the kitsune");
    public Place shallowmill = new Place("Shallow-Mill", "A Village in the north, located in a small deepening");
    public Sudbury sudbury = new Sudbury("Sudbury", "The Human capital. The King Rogg and his Family lives here in his Castle. Laws are made in the Council by the king," +
            " 5 Human agents and 2 representatives of each race. \n The main entrance leads to Whitebridge.");

    private Game() {

    }

    public static Game getInstance() {
        return ourInstance;
    }

    public void update(){
    }

    // TODO: 22.01.2023  Redo Save into JSON
    public String saveEverything() {
        StringBuilder builder = new StringBuilder();
        builder.append(optionen.save());
        builder.append(inventory.save());

        return builder.toString();
    }
}
