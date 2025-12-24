package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.text.Text;

public class Potion implements Item {
    int delete = 0;
    private String beschreibung;

    public Potion(String text) {
        beschreibung = text;
    }


    @Override
    public Text getText() {
        return SceneBuilder.getSceneBuilder().makeText(beschreibung);
    }

    @Override
    public void setText(String text) {
        this.beschreibung = text;
    }

    @Override
    public String getuseText() {
        return "Drink";
    }

    @Override
    public void itemUse() {
        if (Game.getInstance().spieler.getHealth() > Game.getInstance().spieler.maxHealth() - 50) {
            Game.getInstance().spieler.heal(Game.getInstance().spieler.maxHealth() - Game.getInstance().spieler.getHealth());
        } else {
            Game.getInstance().spieler.heal(50);
        }
        Game.getInstance().inventory.remove(this);
    }

    @Override
    public void dequip() {
        //cant be dequiped
    }

    @Override
    public boolean isBase() {
        return false;
    }


    @Override
    public int compareTo(Item o) {
        return 0;
    }
}
