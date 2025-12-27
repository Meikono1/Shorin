package com.fuchsbau.shorin.RPG;

import com.fuchsbau.shorin.Characters.Humans.Dave;
import com.fuchsbau.shorin.Characters.Player;
import com.fuchsbau.shorin.Items.Inventory;
import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.RPG.Lore.Armies.Greysmanace;
import com.fuchsbau.shorin.RPG.Lore.Time.BirthofMagic;
import com.fuchsbau.shorin.RPG.Lore.Time.GreatWar;
import com.fuchsbau.shorin.RPG.Places.GreenValley.GreenValley;
import com.fuchsbau.shorin.RPG.Places.KaguyaForest.KaguyaForest;
import com.fuchsbau.shorin.RPG.Places.MountainGong.MountainGong;
import com.fuchsbau.shorin.RPG.Places.Place;
import com.fuchsbau.shorin.RPG.Places.Sudbury.Sudbury;
import com.fuchsbau.shorin.Characters.Humans.Joshua;
import com.fuchsbau.shorin.RPG.Places.Whitebrigde.Whitebridge;

public class Game {
    private static final Game ourInstance = new Game();
    public GameOptions optionen = new GameOptions();
    public Inventory inventory = new Inventory();
    public Player spieler = new Player();
    public Joshua joshua = new Joshua();
    public Dave dave = new Dave();
    public Whitebridge whitebridge = new Whitebridge("Whitebridge", "The City of Whitebridge is located near the Capital Sudbury and the city where you are born. This City contains a Library, Barracks and an Inn.");
    public Place unbridledland = new Place("Unbridled Land", "Land of the kitsune");
    public Place shallowmill = new Place("Shallow-Mill", "A Village in the north, located in a small deepening");
    public BirthofMagic birthofMagic = new BirthofMagic("Birth of Magic");
    public GreatWar greatWar = new GreatWar("Great War");
    public Greysmanace greysmanace = new Greysmanace("Grey's Manace");
    public Sudbury sudbury = new Sudbury("Sudbury", "The Human capital. The King Rogg and his Family lives here in his Castle. Laws are made in the Council by the king," +
            " 5 Human agents and 2 representatives of each race. \n The main entrance leads to Whitebridge.");
    public MountainGong mountainGong = new MountainGong("Mountain Gong", "The Home of the Dwarfes in the west side of Shorin");
    public KaguyaForest kaguyaForest = new KaguyaForest("Kaguya Forest", "The Forest around Mountain Gong");
    public GreenValley greenValley = new GreenValley("Green Valley", "Home of mostly Humans, Peasent Livestyle");


    private Game() {

    }

    public static Game getInstance() {
        return ourInstance;
    }

    public void update() {
        joshua.update();
    }

    // TODO: 22.01.2023  Redo Save into JSON
    public String saveEverything() {
        StringBuilder builder = new StringBuilder();
        builder.append(optionen.save());
        builder.append(inventory.save());

        return builder.toString();
    }
}
