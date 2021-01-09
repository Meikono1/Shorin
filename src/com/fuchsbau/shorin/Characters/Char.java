package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Waffen.Faust;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextFlow;

public class Char {

    private int health;
    private String beschreibung;
    Item Waffe = new Faust();


    public Char(int health) {
        this.health = health;
        beschreibung = "None";
    }


    public void setWaffe(Item waffe) {
        Waffe = waffe;
    }

    public Item getWaffe() {
        return Waffe;
    }

    public int getHealth() {
        return health;
    }

    public TextFlow makeBeschreibung(TextFlow pane) {

        return pane;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void heal(int i) {
        health += i;
    }
}
