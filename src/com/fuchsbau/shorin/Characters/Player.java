package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Items.Inventory;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;


public class Player extends Char {


    private Scene aktuell;
    private Inventory inventory = new Inventory();

    public int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private Text name = SceneBuilder.makeText();


    public Player() {
        super(100);
    }

    public Text getName() {
        return name;
    }

    public void setName(String name) {
        this.name.setText(name);
        this.name.setFill(Paint.valueOf("#ACB069"));

        setBeschreibung("Your name is: "+name+".\nYou are an average Human with a height of 1.8 Meters. Do to your training you have a masculine Body.\n");
    }

    public void setAktuell(Scene aktuell) {
        this.aktuell = aktuell;
    }

    public Scene getAktuell() {

        return aktuell;
    }
}
