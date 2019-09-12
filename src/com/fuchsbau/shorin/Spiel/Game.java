package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Charakters.Humans.Dave;
import com.fuchsbau.shorin.Charakters.Humans.Player;
import com.fuchsbau.shorin.Spiel.Lore.BirthofMagic;
import com.fuchsbau.shorin.Spiel.Lore.GreatWar;
import com.fuchsbau.shorin.Spiel.Orte.Platz;
import com.fuchsbau.shorin.Spiel.Orte.Sudbury.Sudbury;
import com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Barracks.Joshua;
import com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Whitebridge;

public class Game {
    private static Game ourInstance = new Game();
    public Player spieler = new Player();
    public Joshua joshua = new Joshua();
    public Dave dave = new Dave();
    public Whitebridge whitebridge = new Whitebridge("Whitebridge", "The City of Whitebridge is located near the Capital Sudbury and the Coty where you are born. This City contains a Library, Barracks and an Inn.");
    public Platz unbridledland = new Platz("Unbridled Land", "Land of the kitsune");
    public Platz shallowmill = new Platz("Shallow-Mill", "A Village in the north, located in a small deepening");
    public BirthofMagic birthofMagic = new BirthofMagic("Birth of Magic");
    public GreatWar greatWar = new GreatWar("Great War");
    public Sudbury sudbury = new Sudbury("Sudbury", "The Human capital. The King Rogg and his Family lives here in his Castle. Laws are made in the Council by the king," +
            " 5 Huamn agents and 2 representatives of each race. \n The main entrance leads to Whitebridge.");


    private Game() {


    }

    public static Game getInstance() {
        return ourInstance;
    }

}
