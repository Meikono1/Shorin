package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Characters.Humans.Dave;
import com.fuchsbau.shorin.Characters.Player;
import com.fuchsbau.shorin.Items.Inventory;
import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.Spiel.Lore.Armies.Greysmanace;
import com.fuchsbau.shorin.Spiel.Lore.Time.BirthofMagic;
import com.fuchsbau.shorin.Spiel.Lore.Time.GreatWar;
import com.fuchsbau.shorin.Spiel.Places.Platz;
import com.fuchsbau.shorin.Spiel.Places.Sudbury.Sudbury;
import com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Barracks.Joshua;
import com.fuchsbau.shorin.Spiel.Places.Whitebrigde.Whitebridge;

public class Game {
    private static final Game ourInstance = new Game();
    public GameOptionen optionen = new GameOptionen();
    public Inventory inventory = new Inventory();
    public Player spieler = new Player();
    public Joshua joshua = new Joshua();
    public Dave dave = new Dave();
    public Whitebridge whitebridge = new Whitebridge("Whitebridge", "The City of Whitebridge is located near the Capital Sudbury and the Coty where you are born. This City contains a Library, Barracks and an Inn.");
    public Platz unbridledland = new Platz("Unbridled Land", "Land of the kitsune");
    public Platz shallowmill = new Platz("Shallow-Mill", "A Village in the north, located in a small deepening");
    public BirthofMagic birthofMagic = new BirthofMagic("Birth of Magic");
    public GreatWar greatWar = new GreatWar("Great War");
    public Greysmanace greysmanace = new Greysmanace("Grey's Manace");
    public Sudbury sudbury = new Sudbury("Sudbury", "The Human capital. The King Rogg and his Family lives here in his Castle. Laws are made in the Council by the king," +
            " 5 Human agents and 2 representatives of each race. \n The main entrance leads to Whitebridge.");


    private Game() {

    }

    public static Game getInstance() {
        return ourInstance;
    }

    public void update() {
        joshua.update();
    }


}
