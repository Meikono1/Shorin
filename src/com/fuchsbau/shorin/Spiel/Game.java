package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Charakters.Player;
import com.fuchsbau.shorin.Spiel.Orte.Platz;
import com.fuchsbau.shorin.Spiel.Orte.Whitebrigde.Whitebridge;

public class Game {
    public static Player spieler = new Player();
    private static Game ourInstance = new Game();
    public Whitebridge whitebridge = new Whitebridge("Whitebridge", "The City of Whitebridge is located near the Capital Sudbury and the Coty where you are born. This City contains a Library, Barracks and an Inn.");
    public Platz unbriddledland = new Platz("Unbridled Land", "Land of the kitsune");

    private Game() {

    }

    public static Game getInstance() {
        return ourInstance;
    }

}
