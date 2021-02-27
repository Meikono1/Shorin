package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Spiel.Game;

public class MaxHealth extends Potion{
    public MaxHealth(String text) {
        super(text);
    }

    @Override
    public void itemUse() {

        Game.getInstance().spieler.increasemaxHealth(5);

        Game.getInstance().inventory.remove(this);
    }
}
