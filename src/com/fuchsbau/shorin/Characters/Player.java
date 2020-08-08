package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;


public class Player extends Char {


    private Saveble aktuell;
    private int savedstage;

    public int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private String name;


    public Player() {
        super(100);
    }

    public Text getName() {
        Text back = SceneBuilder.makeText(name);
        back.setFill(Paint.valueOf("#ACB069"));
        return back;
    }

    public void setName(String name) {
        this.name = name;

        //@TODO name color
        setBeschreibung("Your name is: " + name + ".\nYou are an average Human with a height of 1.8 Meters. Due to your training you have a masculine Body.\n");
    }

    public void setAktuell(Saveble aktuell, int stage) {
        savedstage = stage;
        this.aktuell = aktuell;
    }

    public Scene getAktuell() {

        return aktuell.getScene(savedstage);
    }
}
