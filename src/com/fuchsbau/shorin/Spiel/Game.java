package com.fuchsbau.shorin.Spiel;

import com.fuchsbau.shorin.Charakters.Player;

public class Game {
    public static Player spieler = new Player();
    private static Game ourInstance = new Game();

    private Game() {
    }

    public static Game getInstance() {
        return ourInstance;
    }
}
